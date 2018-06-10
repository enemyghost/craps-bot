package com.gmo.discord.craps.bot.entities;

import java.util.Objects;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Represents a Key for which only one active {@link CrapsGame} can exist at a time.
 *
 * @author tedelen
 */
public class CrapsGameKey {
    private final IChannel channel;
    private final IGuild guild;

    public CrapsGameKey(final IChannel channel, final IGuild guild) {
        this.channel = Objects.requireNonNull(channel, "Null channel");
        this.guild = Objects.requireNonNull(guild, "Null guild");
    }

    public IChannel getChannel() {
        return channel;
    }

    public IGuild getGuild() {
        return guild;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrapsGameKey that = (CrapsGameKey) o;
        return Objects.equals(channel, that.channel) &&
                Objects.equals(guild, that.guild);
    }

    @Override
    public int hashCode() {
        return Objects.hash(channel, guild);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("CrapsGameKey{");
        sb.append("channel=").append(channel);
        sb.append(", guild=").append(guild);
        sb.append('}');
        return sb.toString();
    }
}
