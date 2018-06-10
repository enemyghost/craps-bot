package com.gmo.discord.craps.bot.command;

import java.util.Optional;

import com.gmo.discord.craps.bot.entities.CrapsGame;
import com.gmo.discord.craps.bot.store.CrapsGameStore;
import com.gmo.discord.craps.bot.message.CrapsMessage;

/**
 * {@link ICommand} that prints info about a game currently in progress, if one exists.
 *
 * @author tedelen
 */
public class InfoCommand implements ICommand {
    public static final InfoCommand INSTANCE = new InfoCommand();

    private InfoCommand() { }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase("info");
    }

    @Override
    public CrapsMessage execute(final CommandInfo commandInfo, final CrapsGameStore gameStore) {
        final Optional<CrapsGame> activeGame = gameStore.getActiveGame(commandInfo.getKey());
        final String message = activeGame.map(Object::toString).orElse("No game in progress, use `!craps <bet>`");
        return CrapsMessage.newBuilder().withText(message).build();
    }
}
