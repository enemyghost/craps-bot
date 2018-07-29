package com.gmo.discord.craps.bot.entities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.gmo.discord.craps.bot.bet.AbstractBet;
import com.gmo.discord.craps.bot.bet.AbstractBet.BetType;
import com.gmo.discord.craps.bot.entities.CrapsGame.GameState;
import com.gmo.discord.craps.bot.store.CrapsGameStore;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import sx.blah.discord.handle.obj.IUser;

/**
 * @author tedelen
 */
public class CrapsSession {
    public enum SessionState {
        ACTIVE,
        COMPLETED
    }

    private final CrapsGameKey key;
    private final IUser shooter;
    private final Map<IUser, List<AbstractBet>> activeBets;
    private final CrapsGameStore gameStore;
    private final AtomicInteger gamesWon;

    private CrapsGame currentGame;
    private SessionState state;

    public CrapsSession(final CrapsGameKey key, final IUser shooter, final CrapsGameStore gameStore) {
        this.key = Objects.requireNonNull(key, "Null key");
        this.shooter = Objects.requireNonNull(shooter, "Null shooter");
        this.gameStore = Objects.requireNonNull(gameStore, "Null game store");

        activeBets = new HashMap<>();
        state = SessionState.ACTIVE;
        currentGame = gameStore.newGame(key, shooter);
        gamesWon = new AtomicInteger();
    }

    public CrapsGameKey getKey() {
        return key;
    }

    public IUser getShooter() {
        return shooter;
    }

    public Map<IUser, List<AbstractBet>> getActiveBets() {
        return ImmutableMap.copyOf(activeBets);
    }

    public CrapsGameStore getGameStore() {
        return gameStore;
    }

    public CrapsGame getCurrentGame() {
        return currentGame;
    }

    public SessionState getState() {
        return state;
    }

    public int gamesWon() {
        return gamesWon.get();
    }

    public Map<IUser, Long> roll(final Roll roll) {
        if (state == SessionState.COMPLETED) {
            throw new IllegalStateException("You cannot roll in a completed session, start a new session");
        }

        if (!currentGame.isActive()) {
            currentGame = gameStore.newGame(key, shooter);
        }
        final GameState previousState = currentGame.getState();

        currentGame = gameStore.updateGame(currentGame, roll);

        if (currentGame.isWon()) {
            gamesWon.incrementAndGet();
        }
        if (!currentGame.isActive()) {
            gameStore.completeGame(currentGame.getKey());
            if (previousState == GameState.POINT && currentGame.lastRoll().sevenOut()) {
                state = SessionState.COMPLETED;
            }
        }

        final Map<IUser, Long> payouts = new HashMap<>();
        for (final Map.Entry<IUser, List<AbstractBet>> userBets : activeBets.entrySet()) {
            final List<AbstractBet> resolvedBets = userBets.getValue()
                    .stream()
                    .filter(t -> t.resolved(currentGame))
                    .collect(Collectors.collectingAndThen(Collectors.toList(), ImmutableList::copyOf));

            final AtomicLong totalPayout = new AtomicLong();
            for (final AbstractBet bet : resolvedBets) {
                final long payout = bet.payout(currentGame);
                totalPayout.addAndGet(payout);
                if (bet.returnOnPayout() || payout < 0) {
                    removeBet(userBets.getKey(), bet);
                }
            }
            payouts.put(userBets.getKey(), totalPayout.get());
        }
        return payouts;
    }

    public boolean isComplete() {
        return state == SessionState.COMPLETED;
    }

    /**
     * True if there is at least one active bet on the table
     */
    public boolean hasActiveBets() {
        return activeBets.values().stream().flatMap(Collection::stream).findAny().isPresent();
    }

    /**
     * Adds the given bet to the table. If user already has a bet of the same type, adds the bet value to the given bet.
     */
    public AbstractBet addOrModifyBet(final IUser user, final AbstractBet bet) {
        final List<AbstractBet> currentBets = activeBets.computeIfAbsent(user, u -> new ArrayList<>());
        final Optional<AbstractBet> existingBet = currentBets.stream().filter(t -> t.getBetType() == bet.getBetType()).findFirst();
        final AbstractBet addedBet;
        if (existingBet.isPresent()) {
            existingBet.get().addValue(bet.getBetValue());
            addedBet = existingBet.get();
        } else {
            currentBets.add(bet);
            addedBet = bet;
        }

        removeZeroValueBets(user);
        return addedBet;
    }

    private boolean removeBet(final IUser user, final AbstractBet bet) {
        final List<AbstractBet> userBets = activeBets.getOrDefault(user, new ArrayList<>());
        return userBets.remove(bet);
    }

    private void removeZeroValueBets(final IUser user) {
        final List<AbstractBet> userBets = activeBets.getOrDefault(user, new ArrayList<>());
        userBets.stream()
                .filter(t -> t.getBetValue() == 0)
                .forEach(userBets::remove);
    }
}
