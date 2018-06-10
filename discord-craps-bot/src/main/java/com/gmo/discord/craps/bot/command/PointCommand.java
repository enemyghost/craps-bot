package com.gmo.discord.craps.bot.command;

import java.util.Optional;

import com.gmo.discord.craps.bot.entities.CrapsGame;
import com.gmo.discord.craps.bot.store.CrapsGameStore;
import com.gmo.discord.craps.bot.message.CrapsMessage;

/**
 * {@link ICommand} that prints the point if there is one for the game currently in progress.
 *
 * @author tedelen
 */
public class PointCommand implements ICommand {
    public static final PointCommand INSTANCE = new PointCommand();

    private PointCommand() { }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase("point");
    }

    @Override
    public CrapsMessage execute(final CommandInfo commandInfo,
                                final CrapsGameStore gameStore) {
        final Optional<CrapsGame> activeGame = gameStore.getActiveGame(commandInfo.getKey());
        final String message = activeGame
                .map(crapsGame -> "The point is " + crapsGame.getPoint().getAsInt())
                .orElse("No game in progress, use `!craps <bet>`");

        return CrapsMessage.newBuilder().withText(message).build();
    }
}
