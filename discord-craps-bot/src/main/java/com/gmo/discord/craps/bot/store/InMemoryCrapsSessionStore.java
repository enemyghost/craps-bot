package com.gmo.discord.craps.bot.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.gmo.discord.craps.bot.entities.CrapsGameKey;
import com.gmo.discord.craps.bot.entities.CrapsSession;
import sx.blah.discord.handle.obj.IUser;

/**
 * @author tedelen
 */
public class InMemoryCrapsSessionStore implements CrapsSessionStore {
    private static final Map<CrapsGameKey, CrapsSession> ACTIVE_SESSIONS = new HashMap<>();

    private final CrapsGameStore gameStore;

    public InMemoryCrapsSessionStore(final CrapsGameStore gameStore) {
        this.gameStore = Objects.requireNonNull(gameStore, "Game store");
    }

    @Override
    public CrapsSession newSession(final CrapsGameKey key, final IUser shooter) {
        if (ACTIVE_SESSIONS.containsKey(key)) {
            throw new IllegalStateException("Cannot create new game when game in progress");
        }
        return ACTIVE_SESSIONS.computeIfAbsent(key, (k) -> new CrapsSession(k, shooter, gameStore));
    }

    @Override
    public Optional<CrapsSession> getActiveSession(final CrapsGameKey key) {
        return Optional.ofNullable(ACTIVE_SESSIONS.get(key));
    }

    @Override
    public CrapsSession updateSession(final CrapsSession session) {
        return ACTIVE_SESSIONS.put(session.getKey(), session);
    }

    @Override
    public CrapsSession completeSession(final CrapsGameKey key) {
        if (ACTIVE_SESSIONS.containsKey(key) && ACTIVE_SESSIONS.get(key).getCurrentGame() != null) {
            gameStore.completeGame(ACTIVE_SESSIONS.get(key).getCurrentGame().getKey());
        }
        return ACTIVE_SESSIONS.remove(key);
    }
}
