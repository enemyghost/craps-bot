package com.gmo.discord.craps.bot.command;

import java.awt.*;
import java.util.Optional;

import com.gmo.discord.craps.bot.bet.PassLineBet;
import com.gmo.discord.craps.bot.entities.CrapsGame;
import com.gmo.discord.craps.bot.entities.CrapsSession;
import com.gmo.discord.craps.bot.store.CrapsGameStore;
import com.gmo.discord.craps.bot.message.CrapsMessage;
import com.gmo.discord.craps.bot.store.CrapsSessionStore;
import sx.blah.discord.util.EmbedBuilder;

/**
 * {@link ICommand} that handles starting a new craps game.
 *
 * @author tedelen
 */
public class CrapsCommand implements ICommand {
    public static final CrapsCommand INSTANCE = new CrapsCommand();

    private CrapsCommand() { }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase("craps");
    }

    @Override
    public CrapsMessage execute(final CommandInfo commandInfo, final CrapsSessionStore sessionStore) {
        if (!canHandle(commandInfo)) {
            throw new IllegalStateException("Cannot run this command, call canHandle before calling execute");
        }

        final Optional<Long> bet = commandInfo.getLongArg(0);
        if (!bet.isPresent() || bet.get() <= 0) {
            return CrapsMessage.newBuilder()
                    .withText("Please provide a positive integer bet value. `!craps <bet>`")
                    .build();
        }
        final Optional<CrapsSession> activeGame = sessionStore.getActiveSession(commandInfo.getKey());
        if (activeGame.isPresent()) {
            return CrapsMessage.newBuilder(InfoCommand.INSTANCE.execute(commandInfo, sessionStore))
                    .appendText("\nGame is already in progress, use `!roll` to roll.")
                    .build();
        }

        final CrapsSession crapsSession = sessionStore.newSession(commandInfo.getKey(), commandInfo.getUser());
        crapsSession.addOrModifyBet(commandInfo.getUser(), new PassLineBet(bet.get()));
        if (commandInfo.getArg(1).map(arg -> arg.equalsIgnoreCase("quick")).orElse(false)) {
            CrapsMessage message = null;
            while (crapsSession.getCurrentGame().isActive()) {
                message = RollCommand.INSTANCE.execute(commandInfo, sessionStore);
            }
            sessionStore.completeSession(crapsSession.getKey());
            return CrapsMessage.newBuilder(message)
                    .withReplacePrevious(false)
                    .build();
        } else {
            return CrapsMessage.newBuilder()
                    .withEmbedObject(new EmbedBuilder()
                            .withColor(Color.ORANGE)
                            .withTitle("Craps Initiated")
                            .appendDesc("New craps session started for shooter ")
                            .appendDesc(commandInfo.getUserName())
                            .appendDesc(". Use `!roll` to roll")
                            .build())
                    .build();
        }
    }
}
