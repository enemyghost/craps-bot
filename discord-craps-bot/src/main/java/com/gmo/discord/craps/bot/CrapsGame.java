package com.gmo.discord.craps.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import sx.blah.discord.handle.obj.IUser;

/**
 * @author tedelen
 */
public class CrapsGame {
    public enum GameState {
        COME_OUT(false, "Come out roll."),
        POINT(false, "Point is set."),
        WON(true, "You win."),
        LOST(true, "You lose.");

        private final boolean isFinalState;
        private final String description;
        GameState(final boolean isFinalState, final String description) {
            this.isFinalState = isFinalState;
            this.description = description;
        }

        public boolean isFinalState() {
            return isFinalState;
        }

        public String description() {
            return description;
        }
    }

    private final CrapsGameKey key;
    private final List<Roll> rollHistory;
    private final int bet;
    private int point;
    private final IUser user;

    private GameState state;

    public CrapsGame(final CrapsGameKey key, final IUser user, final int bet) {
        this.key = Objects.requireNonNull(key);
        this.user = Objects.requireNonNull(user, "Null user");
        this.bet = bet;
        this.rollHistory = new ArrayList<>();
        this.state = GameState.COME_OUT;
    }

    public CrapsGameKey getKey() {
        return key;
    }

    public GameState getState() {
        return state;
    }

    public List<Roll> getRollHistory() {
        return rollHistory;
    }

    public OptionalInt getPoint() {
        return point == 0 ? OptionalInt.empty() : OptionalInt.of(point);
    }

    public boolean isActive() {
        return !state.isFinalState();
    }

    public boolean isWon() {
        return state == GameState.WON;
    }

    public boolean isLost() {
        return state == GameState.LOST;
    }

    public Roll lastRoll() {
        return rollHistory.get(rollHistory.size() - 1);
    }

    public int getBet() {
        return bet;
    }

    public IUser getUser() {
        return user;
    }

    public synchronized GameState addRoll(final Roll roll) {
        rollHistory.add(roll);
        if (state == GameState.COME_OUT) {
            if (roll.comeOutWin()) {
                state = GameState.WON;
            } else if (roll.comeOutLoss()) {
                state = GameState.LOST;
            } else {
                point = roll.getTotal();
                state = GameState.POINT;
            }
        } else if (state == GameState.POINT) {
            if (point == roll.getTotal()) {
                state = GameState.WON;
            } else if (roll.sevenOut()) {
                state = GameState.LOST;
            }
        } else {
            throw new IllegalStateException("Cannot add rolls to a finished game");
        }

        return state;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(String.format("Craps game with bet %d in state `%s`", bet, state.description()));
        if (rollHistory.isEmpty()) {
            sb.append("\nNo rolls yet. use `!roll` to roll.");
        } else {
            getPoint().ifPresent(p -> sb.append("\nThe point is ").append(p).append("."));
            sb.append("\nRoll History: ")
                    .append(rollHistory.stream()
                            .map(Roll::getTotal)
                            .map(Object::toString)
                            .collect(Collectors.joining(", ")));
        }
        return sb.toString();
    }
}
