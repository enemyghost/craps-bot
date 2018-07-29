package com.gmo.discord.craps.bot.bet;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map;

import com.gmo.discord.craps.bot.entities.CrapsGame;
import com.google.common.collect.ImmutableMap;

/**
 * @author tedelen
 */
public class OddsBet extends AbstractBet {
    private static final Map<Integer, Double> POINT_TO_MULTIPLIER_MAP = ImmutableMap.<Integer, Double>builder()
            .put(4, 2.0)
            .put(10, 2.0)
            .put(5, 1.5)
            .put(9, 1.5)
            .put(6, 1.2)
            .put(8, 1.2)
            .build();

    private final int point;

    public OddsBet(final int point, final long bet) {
        super(BetType.ODDS, bet);
        checkArgument(point >= 4 && point <= 10 && point != 7, "Please provide a valid craps point");
        this.point = point;
    }

    @Override
    protected long calculatePayout(final CrapsGame crapsGame) {
        if (crapsGame.lastRoll().getTotal() == point) {
            return getBetValue() + (long) (POINT_TO_MULTIPLIER_MAP.get(point) * getBetValue());
        } else if (crapsGame.lastRoll().sevenOut()) {
            return getBetValue() * -1;
        }
        return 0;
    }

    @Override
    public boolean returnOnPayout() {
        return true;
    }

    public int getPoint() {
        return point;
    }
}
