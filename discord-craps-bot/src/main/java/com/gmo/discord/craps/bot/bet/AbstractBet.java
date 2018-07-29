package com.gmo.discord.craps.bot.bet;

import java.util.Objects;

import com.gmo.discord.craps.bot.entities.CrapsGame;

/**
 * @author tedelen
 */
public abstract class AbstractBet {
    public enum BetType {
        PASS_LINE,
        ODDS
    }

    private final BetType betType;
    private long betValue;
    private boolean on;

    AbstractBet(final BetType betType, final long initialValue) {
        this.betType = Objects.requireNonNull(betType, "Null bet type");
        if (initialValue <= 0) {
            throw new IllegalArgumentException("Bet must be positive");
        }
        this.betValue = initialValue;
        this.on = true;
    }

    public BetType getBetType() {
        return betType;
    }

    public long getBetValue() {
        return betValue;
    }

    public long addValue(long additional) {
        betValue = Math.max(0, betValue + additional);
        return betValue;
    }

    public void on() {
        this.on = true;
    }

    public void off() {
        this.on = false;
    }

    public boolean isOn() {
        return on;
    }

    public long payout(final CrapsGame crapsGame) {
        if (on) {
            return calculatePayout(crapsGame);
        } else {
            return 0;
        }
    }

    public boolean resolved(final CrapsGame crapsGame) {
        return payout(crapsGame) != 0;
    }

    protected abstract long calculatePayout(final CrapsGame crapsGame);

    public boolean returnOnPayout() {
        return false;
    }
}
