package com.gmo.discord.craps.bot.store;

import java.util.Optional;

import com.gmo.discord.craps.bot.entities.CrapsGame;
import com.gmo.discord.craps.bot.entities.CrapsGameKey;
import com.gmo.discord.craps.bot.entities.Roll;
import sx.blah.discord.handle.obj.IUser;

/**
 * Store for CRUD operations on a {@link CrapsGame} for a {@link CrapsGameKey}
 * @author tedelen
 */
public interface CrapsGameStore {
    /**
     * Creates a new game for the given key.
     *
     * @param key {@link CrapsGameKey} to create a game for
     * @param user {@link IUser} who owns the game
     *
     * @return the newly created {@link CrapsGame}
     *
     * @throws IllegalStateException if there is already a game in progress with the given {@link CrapsGameKey}
     */
    CrapsGame newGame(final CrapsGameKey key, final IUser user);

    /**
     * Gets the currently active game for the given key, if one exists
     *
     * @param key {@link CrapsGameKey} to look up
     *
     * @return currently active game for the key, or {@link Optional#empty()} if none exists
     */
    Optional<CrapsGame> getActiveGame(final CrapsGameKey key);

    /**
     * Updates the given {@link CrapsGame} with the given {@link Roll} as its next roll
     *
     * @param crapsGame {@link CrapsGame} to update
     * @param nextRoll {@link Roll} to be added
     *
     * @return the updated {@link CrapsGame}
     *
     * @throws IllegalStateException if the given {@link CrapsGame} is not active
     */
    CrapsGame updateGame(final CrapsGame crapsGame, final Roll nextRoll);

    /**
     * Marks the given game as completed. All payout operations should be done before this is called. Usually the game
     * should be in a final state when this is called, but it is not required.
     *
     * @param key {@link CrapsGame} to complete.
     *
     * @return completed {@link CrapsGame}
     */
    CrapsGame completeGame(final CrapsGameKey key);
}
