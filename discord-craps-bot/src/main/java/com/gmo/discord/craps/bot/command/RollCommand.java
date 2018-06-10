package com.gmo.discord.craps.bot.command;

import static com.gmo.discord.craps.bot.message.DiceEmoji.getEmojiIdentifier;

import java.awt.*;
import java.util.Optional;
import java.util.stream.Collectors;

import com.gmo.discord.craps.bot.entities.CrapsGame;
import com.gmo.discord.craps.bot.store.CrapsGameStore;
import com.gmo.discord.craps.bot.entities.Roll;
import com.gmo.discord.craps.bot.message.CrapsMessage;
import sx.blah.discord.util.EmbedBuilder;

/**
 * {@link ICommand} that handles rolling for an in progress game.
 *
 * @author tedelen
 */
public class RollCommand implements ICommand {
    public static final RollCommand INSTANCE = new RollCommand();

    private RollCommand() { }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase("roll");
    }

    @Override
    public CrapsMessage execute(final CommandInfo commandInfo, final CrapsGameStore gameStore) {
        final Optional<CrapsGame> gameOpt = gameStore.getActiveGame(commandInfo.getKey());
        if (!gameOpt.isPresent()) {
            return CrapsMessage.newBuilder()
                    .withText("Start a new game first. `!craps <bet>`")
                    .build();
        } else if (gameOpt.get().getUser().getLongID() != commandInfo.getUser().getLongID()) {
            return CrapsMessage.newBuilder()
                    .withText("You cannot roll. There is a game in progress, but it is owned by " + gameOpt.get().getUser().getDisplayName(commandInfo.getGuild()))
                    .build();
        }

        final Roll roll = Roll.roll();
        final CrapsGame crapsGame = gameStore.updateGame(gameOpt.get(), roll);
        final EmbedBuilder embedBuilder = new EmbedBuilder().withTitle(roll.displayValue(commandInfo.getGuild()));
        if (crapsGame.isActive()) {
            embedBuilder.withColor(Color.ORANGE)
                    .withDesc(String.format("The point is %d. Roll again!", crapsGame.getPoint().getAsInt()));
        } else {
            if (crapsGame.isWon()) {
                embedBuilder.withColor(Color.GREEN);
                if (roll.sevenOut()) {
                    embedBuilder.appendDesc("Seven winner! Front line winner.");
                } else if (roll.yo()) {
                    embedBuilder.appendDesc("Yo! Winner!");
                } else {
                    embedBuilder.appendDesc("Winner!");
                }
                embedBuilder.appendDesc(" Pay the line: ").appendDesc(Integer.toString(crapsGame.getBet() * 2));
            } else {
                embedBuilder.withColor(Color.RED);
                if (roll.craps()) {
                    embedBuilder.appendDesc(" Craps! You Lose. Sad!");
                } else {
                    embedBuilder.appendDesc(" Seven out! Line away.");
                }
            }
            gameStore.completeGame(crapsGame.getKey());
        }

        embedBuilder
                .appendDesc("\nRoll History: \n")
                .appendDesc(crapsGame.getRollHistory()
                        .stream()
                        .map(r -> String.format("%d%s %s %s",
                                r.getTotal(),
                                r.getTotal() < 10 ? " " : "", // helps with spacing if one digit number
                                getEmojiIdentifier(commandInfo.getGuild(), r.getValue1()),
                                getEmojiIdentifier(commandInfo.getGuild(), r.getValue2())))
                        .collect(Collectors.joining("\n")));

        return CrapsMessage.newBuilder()
                .withEmbedObject(embedBuilder.build())
                .withReplacePrevious(true)
                .build();
    }
}
