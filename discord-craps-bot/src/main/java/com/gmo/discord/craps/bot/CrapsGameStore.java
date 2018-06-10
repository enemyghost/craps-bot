package com.gmo.discord.craps.bot;

import java.util.Optional;

import sx.blah.discord.handle.obj.IUser;

/**
 * @author tedelen
 */
public interface CrapsGameStore {
    CrapsGame newGame(final CrapsGameKey key, final IUser user, final int bet);
    Optional<CrapsGame> getActiveGame(final CrapsGameKey key);
    CrapsGame updateGame(final CrapsGame crapsGame, final Roll nextRoll);
    CrapsGame completeGame(final CrapsGameKey key);
}
