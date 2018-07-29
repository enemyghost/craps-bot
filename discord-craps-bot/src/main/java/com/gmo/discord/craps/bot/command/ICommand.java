package com.gmo.discord.craps.bot.command;

import com.gmo.discord.craps.bot.message.CrapsMessage;
import com.gmo.discord.craps.bot.store.CrapsSessionStore;

/**
 * @author tedelen
 */
public interface ICommand {
    boolean canHandle(final CommandInfo commandInfo);
    CrapsMessage execute(final CommandInfo commandInfo, final CrapsSessionStore sessionStore);
}
