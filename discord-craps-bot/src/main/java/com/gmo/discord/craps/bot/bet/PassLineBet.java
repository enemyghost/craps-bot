package com.gmo.discord.craps.bot.bet;

import com.gmo.discord.craps.bot.entities.CrapsGame;

/**
 * @author tedelen
 */
public class PassLineBet extends AbstractBet {
    public PassLineBet(final long bet) {
        super(BetType.PASS_LINE, bet);
    }

    @Override
    protected long calculatePayout(final CrapsGame crapsGame) {
        if (crapsGame.isActive()) {
            return 0;
        } else if (crapsGame.isWon()) {
            return getBetValue();
        } else {
            return getBetValue() * -1;
        }
    }
}
