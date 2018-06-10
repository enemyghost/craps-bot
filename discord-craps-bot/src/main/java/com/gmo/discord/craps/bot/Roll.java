package com.gmo.discord.craps.bot;

import java.util.Random;

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

    @Override
    public String toString() {
        if (yo()) {
            return String.format("Yoleven! (%d %d)", value1, value2);
        } else if (craps()) {
            return String.format("%d Craps! (%d %d)", total, value1, value2);
        } else {
            final String prefix = hard() ? "Hard " : easy() ? "Easy " : "";
            return String.format("%s%d (%d %d)", prefix, total, value1, value2);
        }
    }
}
