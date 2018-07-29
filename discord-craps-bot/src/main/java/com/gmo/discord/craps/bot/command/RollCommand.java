package com.gmo.discord.craps.bot.command;

import java.awt.*;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

import com.gmo.discord.craps.bot.entities.CrapsGame;
import com.gmo.discord.craps.bot.entities.CrapsSession;
import com.gmo.discord.craps.bot.store.CrapsGameStore;
import com.gmo.discord.craps.bot.entities.Roll;
import com.gmo.discord.craps.bot.message.CrapsMessage;
import com.gmo.discord.craps.bot.store.CrapsSessionStore;
import sx.blah.discord.handle.obj.IUser;
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
    public CrapsMessage execute(final CommandInfo commandInfo, final CrapsSessionStore sessionStore) {
        final Optional<CrapsSession> sessionOpt = sessionStore.getActiveSession(commandInfo.getKey());
        if (!sessionOpt.isPresent()) {
            return CrapsMessage.newBuilder()
                    .withText("Start a new session first. `!craps <bet>`")
                    .build();
        } else if (sessionOpt.get().getShooter().getLongID() != commandInfo.getUser().getLongID()) {
            return CrapsMessage.newBuilder()
                    .withText("You cannot roll. There is a game in progress, but it is owned by " + sessionOpt.get().getShooter().getDisplayName(commandInfo.getGuild()))
                    .build();
        }

        final CrapsSession crapsSession = sessionOpt.get();
        final Roll roll = Roll.roll();
        final Map<IUser, Long> payouts = crapsSession.roll(roll);
        final CrapsGame crapsGame = crapsSession.getCurrentGame();

        final EmbedBuilder embedBuilder = new EmbedBuilder();

        if (roll.sevenOut()) {
            embedBuilder.withTitle(crapsGame.isWon()
                    ? "Seven winner! Front line winner."
                    : "Seven out! Line away.");
        } else {
            embedBuilder.withTitle(roll.call());
        }

        if (crapsGame.isActive()) {
            embedBuilder.withColor(Color.ORANGE)
                    .appendDesc(String.format("The point is %d. Roll again!", crapsGame.getPoint().getAsInt()));
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
                embedBuilder.appendDesc("Pay the line!");
            } else {
                embedBuilder.withColor(Color.RED);
                if (roll.craps()) {
                    embedBuilder.appendDesc("Craps! You Lose. Sad!");
                }
            }
        }

        if (crapsSession.isComplete()) {
            embedBuilder.appendDesc("Session over. Pass the dice.");
            sessionStore.completeSession(crapsSession.getKey());
        }

        new LinkedList<>(crapsGame.getRollHistory())
                .descendingIterator()
                .forEachRemaining(r -> embedBuilder
                        .appendDesc("\n")
                        .appendDesc(r.displayValue(commandInfo.getGuild())));

        if (!payouts.isEmpty()) {
            embedBuilder.appendDesc("\n Bet results: ");
            payouts.entrySet().stream()
                    .map(entry -> entry.getKey().getDisplayName(commandInfo.getGuild()) + ": " + entry.getValue())
                    .forEach(embedBuilder::appendDesc);
        }

        return CrapsMessage.newBuilder()
                .withEmbedObject(embedBuilder.build())
                .withReplacePrevious(true)
                .build();
    }
}
