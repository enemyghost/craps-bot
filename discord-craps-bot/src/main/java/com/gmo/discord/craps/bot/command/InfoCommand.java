package com.gmo.discord.craps.bot.command;

import java.util.Optional;

import com.gmo.discord.craps.bot.entities.CrapsSession;
import com.gmo.discord.craps.bot.message.CrapsMessage;
import com.gmo.discord.craps.bot.store.CrapsSessionStore;

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
    public CrapsMessage execute(final CommandInfo commandInfo, final CrapsSessionStore gameStore) {
        final Optional<CrapsSession> activeSession = gameStore.getActiveSession(commandInfo.getKey());

        final String message = activeSession
                .map(session -> session.getCurrentGame().toString() + "\nGames won so far: " + session.gamesWon())
                .orElse("No game in progress, use `!craps <bet>`");

        return CrapsMessage.newBuilder().withText(message).build();
    }
}
