package com.gmo.discord.craps.bot.command;

import java.awt.*;
import java.util.Optional;

import com.gmo.discord.craps.bot.entities.CrapsGame;
import com.gmo.discord.craps.bot.store.CrapsGameStore;
import com.gmo.discord.craps.bot.message.CrapsMessage;
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
    public CrapsMessage execute(final CommandInfo commandInfo, final CrapsGameStore gameStore) {
        if (!canHandle(commandInfo)) {
            throw new IllegalStateException("Cannot run this command, call canHandle before calling execute");
        }

        final Optional<Integer> bet = commandInfo.getIntArg(0);
        if (!bet.isPresent() || bet.get() <= 0) {
            return CrapsMessage.newBuilder()
                    .withText("Please provide a positive integer bet value. `!craps <bet>`")
                    .build();
        }
        final Optional<CrapsGame> activeGame = gameStore.getActiveGame(commandInfo.getKey());
        if (activeGame.isPresent()) {
            return CrapsMessage.newBuilder()
                    .withText("Game is already in progress, use `!roll` to roll.\n" + activeGame.get().toString())
                    .build();
        }

        final CrapsGame crapsGame = gameStore.newGame(commandInfo.getKey(), commandInfo.getUser(), bet.get());
        if (commandInfo.getArg(1).map(arg -> arg.equalsIgnoreCase("quick")).orElse(false)) {
            CrapsMessage message = null;
            while (crapsGame.isActive()) {
                message = RollCommand.INSTANCE.execute(commandInfo, gameStore);
            }

            return CrapsMessage.newBuilder(message)
                    .withReplacePrevious(false)
                    .build();
        } else {
            return CrapsMessage.newBuilder()
                    .withEmbedObject(new EmbedBuilder()
                            .withColor(Color.ORANGE)
                            .withTitle("Craps Initiated")
                            .appendDesc("New craps game started for ")
                            .appendDesc(commandInfo.getUserName())
                            .appendDesc(". Use `!roll` to roll")
                            .build())
                    .build();
        }
    }
}
