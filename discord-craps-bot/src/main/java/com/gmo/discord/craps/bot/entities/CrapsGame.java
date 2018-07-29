package com.gmo.discord.craps.bot.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.stream.Collectors;

import sx.blah.discord.handle.obj.IUser;

/**
 * Represents a Craps Game that can be progressed using the {@link #addRoll(Roll)}} method.
 *
 * @author tedelen
 */
public class CrapsGame {
    public enum GameState {
        COME_OUT(false, "Come out roll."),
        POINT(false, "Point is set."),
        WON(true, "You win."),
        LOST(true, "You lose.");

        private final boolean isFinalState;
        private final String description;
        GameState(final boolean isFinalState, final String description) {
            this.isFinalState = isFinalState;
            this.description = description;
        }

        public boolean isFinalState() {
            return isFinalState;
        }

        public String description() {
            return description;
        }
    }

    private final CrapsGameKey key;
    private final List<Roll> rollHistory;
    private int point;
    private final IUser shooter;

    private GameState state;

    public CrapsGame(final CrapsGameKey key, final IUser shooter) {
        this.key = Objects.requireNonNull(key);
        this.shooter = Objects.requireNonNull(shooter, "Null shooter");
        this.rollHistory = new ArrayList<>();
        this.state = GameState.COME_OUT;
    }

    public CrapsGameKey getKey() {
        return key;
    }

    public GameState getState() {
        return state;
    }

    public List<Roll> getRollHistory() {
        return rollHistory;
    }

    public OptionalInt getPoint() {
        return point == 0 ? OptionalInt.empty() : OptionalInt.of(point);
    }

    public boolean isActive() {
        return !state.isFinalState();
    }

    public boolean isWon() {
        return state == GameState.WON;
    }

    public boolean isLost() {
        return state == GameState.LOST;
    }

    public Roll lastRoll() {
        return rollHistory.size() == 0 ? null : rollHistory.get(rollHistory.size() - 1);
    }

    public IUser getShooter() {
        return shooter;
    }

    public synchronized GameState addRoll(final Roll roll) {
        rollHistory.add(roll);
        if (state == GameState.COME_OUT) {
            if (roll.comeOutWin()) {
                state = GameState.WON;
            } else if (roll.comeOutLoss()) {
                state = GameState.LOST;
            } else {
                point = roll.getTotal();
                state = GameState.POINT;
            }
        } else if (state == GameState.POINT) {
            if (point == roll.getTotal()) {
                state = GameState.WON;
            } else if (roll.sevenOut()) {
                state = GameState.LOST;
            }
        } else {
            throw new IllegalStateException("Cannot add rolls to a finished game");
        }

        return state;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer(String.format("Craps game in state `%s`", state.description()));
        if (rollHistory.isEmpty()) {
            sb.append("\nNo rolls yet. use `!roll` to roll.");
        } else {
            getPoint().ifPresent(p -> sb.append("\nThe point is ").append(p).append("."));
            sb.append("\nRoll History: ")
                    .append(rollHistory.stream()
                            .map(Roll::getTotal)
                            .map(Object::toString)
                            .collect(Collectors.joining(", ")));
        }
        return sb.toString();
    }
}
