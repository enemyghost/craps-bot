package com.gmo.discord.craps.bot.message;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import sx.blah.discord.handle.obj.IGuild;

/**
 * Contains emoji IDs for each of the 6 die faces.
 *
 * This is pretty hacky, and requires the {@link IGuild} to have custom emoji called "dieX" for each die or else it will
 * default to the included :one: through :six: emoji that do not look like dice at all.
 *
 * Dice emoji should be configurable, but this will do for now.
 *
 * @author tedelen
 */
public final class DiceEmoji {
    private static final Map<Integer, String> DEFAULT_EMOJI_NAMES = new HashMap<>();
    static {
        DEFAULT_EMOJI_NAMES.put(1, ":one:");
        DEFAULT_EMOJI_NAMES.put(2, ":two:");
        DEFAULT_EMOJI_NAMES.put(3, ":three:");
        DEFAULT_EMOJI_NAMES.put(4, ":four:");
        DEFAULT_EMOJI_NAMES.put(5, ":five:");
        DEFAULT_EMOJI_NAMES.put(6, ":six:");
    }

    private DiceEmoji() { }

    /**
     * Gets the text for the emoji representing the given die face, to be used in a message to discord.
     */
    public static String getEmojiIdentifier(final IGuild guild, final int die) {
        if (die < 1 || die > 6) {
            throw new IllegalArgumentException("Can only provide emoji for die faces 1-6");
        }

        return Optional.ofNullable(guild.getEmojiByName("die" + die))
                .map(Object::toString)
                .orElse(DEFAULT_EMOJI_NAMES.get(die));
    }
}
