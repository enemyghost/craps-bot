package com.gmo.discord.craps.bot.command;

import java.util.Optional;

import com.gmo.discord.craps.bot.bet.AbstractBet;
import com.gmo.discord.craps.bot.bet.AbstractBet.BetType;
import com.gmo.discord.craps.bot.bet.OddsBet;
import com.gmo.discord.craps.bot.entities.CrapsSession;
import com.gmo.discord.craps.bot.message.CrapsMessage;
import com.gmo.discord.craps.bot.store.CrapsSessionStore;
import com.google.common.base.Preconditions;
import sx.blah.discord.handle.obj.IUser;

/**
 * @author tedelen
 */
public class OddsCommand implements ICommand {
    public static final OddsCommand INFINITE_ODDS_COMMAND = new OddsCommand(Long.MAX_VALUE);

    private final long maxOddsMultiplier;

    public OddsCommand(final long maxOddsMultiplier) {
        Preconditions.checkArgument(maxOddsMultiplier > 0, "Max Odds Multiplier must be > 0");
        this.maxOddsMultiplier = maxOddsMultiplier;
    }

    @Override
    public boolean canHandle(final CommandInfo commandInfo) {
        return commandInfo.getCommand().equalsIgnoreCase("odds");
    }

    @Override
    public CrapsMessage execute(final CommandInfo commandInfo, final CrapsSessionStore sessionStore) {
        final Optional<Long> betAmount = commandInfo.getLongArg(0);
        if (!betAmount.isPresent() || betAmount.get() <= 0) {
            return CrapsMessage.newBuilder()
                    .withText("Please provide a positive integer bet value. `!craps <bet>`")
                    .build();
        }

        final long bet = betAmount.get();
        final CrapsSession session = sessionStore.getActiveSession(commandInfo.getKey()).orElse(null);
        if (session == null) {
            return CrapsMessage.newBuilder()
                    .withText("Not accepting bets, start a new session first. `!craps <bet>`")
                    .build();
        }

        final int point = session.getCurrentGame().getPoint().orElse(0);
        if (point == 0) {
            return CrapsMessage.newBuilder()
                    .withText("You cannot place an odds bet if there is no point")
                    .build();
        }

        final IUser bettor = commandInfo.getUser();
        final AbstractBet existingPassLineBet = session.getActiveBets().get(bettor).stream()
                .filter(t -> t.getBetType() == BetType.PASS_LINE)
                .findFirst()
                .orElse(null);
        if (existingPassLineBet == null) {
            return CrapsMessage.newBuilder()
                    .withText("You cannot place an odds bet if you do not have a pass line bet")
                    .build();
        }

        final AbstractBet existingOddsBet = session.addOrModifyBet(commandInfo.getUser(), new OddsBet(point, bet));

        return CrapsMessage.newBuilder()
                .withText(commandInfo.getUserName() + " now has an odds bet of size " + existingOddsBet.getBetValue())
                .build();
    }
}
