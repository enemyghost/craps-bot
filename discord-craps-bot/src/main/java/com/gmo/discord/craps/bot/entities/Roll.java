package com.gmo.discord.craps.bot.entities;

import static com.gmo.discord.craps.bot.message.DiceEmoji.getEmojiIdentifier;

import java.util.Random;

import sx.blah.discord.handle.obj.IGuild;

/**
 * @author tedelen
 */
public class Roll {
    private static final Random RANDOM = new Random();

    private final int value1;
    private final int value2;
    private final int total;

    public Roll(final int value1, final int value2) {
        this.value1 = value1;
        this.value2 = value2;
        total = value1 + value2;
    }

    public static Roll roll() {
        return new Roll(RANDOM.nextInt(6) + 1, RANDOM.nextInt(6) + 1);
    }

    public int getValue1() {
        return value1;
    }

    public int getValue2() {
        return value2;
    }

    public int getTotal() {
        return total;
    }

    public boolean sevenOut() {
        return total == 7;
    }

    public boolean yo() {
        return total == 11;
    }

    public boolean craps() {
        return total == 2 || total == 3 || total == 12;
    }

    public boolean comeOutWin() {
        return sevenOut() || yo();
    }

    public boolean comeOutLoss() {
        return craps();
    }

    public boolean hard() {
        return !craps() && value1 == value2;
    }

    public boolean easy() {
        return !craps() && total % 2 == 0;
    }

    /**
     * What the croupier might say if they called this roll
     */
    public String call() {
        if (yo()) {
            return "Yo, Eleven! Yoleven!";
        } else if (craps()) {
            return String.format("%d Craps!", total);
        } else if (hard()) {
            return String.format("%d, hard %d", total, total);
        } else if (easy()) {
            return String.format("%d, eeeasy %d", total, total);
        } else if (total == 5) {
            return "Five, no field. No field five.";
        } else if (total == 9) {
            return "Nine, center field. Center field nine.";
        } else if (sevenOut()) {
            return "Seven";
        } else {
            return "";
        }
    }

    /**
     * Generates a representation of the dice containing dice emoji
     *
     * @param guild {@link IGuild} is, unfortunately, required to get an emoji representing each die value.
     */
    public String displayValue(final IGuild guild) {
        return String.format("%d%s %s %s", total, total < 10 ? " " : "", getEmojiIdentifier(guild, value1), getEmojiIdentifier(guild, value2));
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Roll{");
        sb.append("value1=").append(value1);
        sb.append(", value2=").append(value2);
        sb.append(", total=").append(total);
        sb.append('}');
        return sb.toString();
    }
}
