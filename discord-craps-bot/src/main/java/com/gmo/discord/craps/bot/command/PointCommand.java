package com.gmo.discord.craps.bot.command;

import java.util.Optional;
import java.util.OptionalInt;

import com.gmo.discord.craps.bot.entities.CrapsGame;
import com.gmo.discord.craps.bot.entities.CrapsSession;
import com.gmo.discord.craps.bot.store.CrapsGameStore;
import com.gmo.discord.craps.bot.message.CrapsMessage;
import com.gmo.discord.craps.bot.store.CrapsSessionStore;

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
                                final CrapsSessionStore sessionStore) {
        final String message;
        final Optional<CrapsGame> crapsGame = sessionStore.getActiveSession(commandInfo.getKey())
                .map(CrapsSession::getCurrentGame);
        if (crapsGame.isPresent()) {
            final OptionalInt point = crapsGame.get().getPoint();
            if (point.isPresent()) {
                message = "The point is " + point.getAsInt();
            } else {
                message = "There is no point set, roll the dice!";
            }
        } else {
            message = "No game in progress, use `!craps`";
        }

        return CrapsMessage.newBuilder().withText(message).build();
    }
}
