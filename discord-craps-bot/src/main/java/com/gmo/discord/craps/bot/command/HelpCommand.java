package com.gmo.discord.craps.bot.command;

import com.gmo.discord.craps.bot.store.CrapsGameStore;
import com.gmo.discord.craps.bot.message.CrapsMessage;
import com.gmo.discord.craps.bot.store.CrapsSessionStore;

/**
 * {@link ICommand} that prints help message
 *
 * @author tedelen
 */
public class HelpCommand implements ICommand {
    public static final HelpCommand INSTANCE = new HelpCommand();

    private HelpCommand() { }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase("help");
    }

    @Override
    public CrapsMessage execute(final CommandInfo commandInfo, final CrapsSessionStore sessionStore) {
        final String message = sessionStore.getActiveSession(commandInfo.getKey())
                .map((activeGame) -> "Type `!roll` to roll or `!info` to see game state")
                .orElse("Type `!craps <bet>` to start a new game.");

        return CrapsMessage.newBuilder()
                .withText(message)
                .build();
    }
}
