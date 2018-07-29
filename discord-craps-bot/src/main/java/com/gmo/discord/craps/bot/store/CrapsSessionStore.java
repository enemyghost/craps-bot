package com.gmo.discord.craps.bot.store;

import java.util.Objects;
import java.util.Optional;

import com.gmo.discord.craps.bot.entities.CrapsGameKey;
import com.gmo.discord.craps.bot.entities.CrapsSession;
import sx.blah.discord.handle.obj.IUser;

/**
 * @author tedelen
 */
public interface CrapsSessionStore {
    CrapsSession newSession(final CrapsGameKey key, final IUser shooter);
    Optional<CrapsSession> getActiveSession(final CrapsGameKey key);
    CrapsSession updateSession(final CrapsSession session);
    CrapsSession completeSession(final CrapsGameKey key);
}
