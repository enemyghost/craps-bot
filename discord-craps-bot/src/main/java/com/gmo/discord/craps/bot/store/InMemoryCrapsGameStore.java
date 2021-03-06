package com.gmo.discord.craps.bot.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.gmo.discord.craps.bot.entities.CrapsGame;
import com.gmo.discord.craps.bot.entities.CrapsGameKey;
import com.gmo.discord.craps.bot.entities.Roll;
import sx.blah.discord.handle.obj.IUser;

/**
 * @author tedelen
 */
public class InMemoryCrapsGameStore implements CrapsGameStore {
    private final static Map<CrapsGameKey, CrapsGame> ACTIVE_GAMES = new HashMap<>();

    @Override
    public CrapsGame newGame(final CrapsGameKey key, final IUser user) {
        if (ACTIVE_GAMES.containsKey(key)) {
            throw new IllegalStateException("Cannot create new game when game in progress");
        }
        return ACTIVE_GAMES.computeIfAbsent(key, (k) -> new CrapsGame(k, user));
    }

    @Override
    public Optional<CrapsGame> getActiveGame(final CrapsGameKey crapsGameKey) {
        return Optional.ofNullable(ACTIVE_GAMES.get(crapsGameKey));
    }

    @Override
    public CrapsGame updateGame(final CrapsGame crapsGame, final Roll nextRoll) {
        if (crapsGame.isActive()) {
            crapsGame.addRoll(nextRoll);
        } else {
            throw new IllegalStateException("Game is not active, call getActiveGame()");
        }

        ACTIVE_GAMES.put(crapsGame.getKey(), crapsGame);
        return crapsGame;
    }

    @Override
    public CrapsGame completeGame(final CrapsGameKey crapsGameKey) {
        return ACTIVE_GAMES.remove(crapsGameKey);
    }
}
