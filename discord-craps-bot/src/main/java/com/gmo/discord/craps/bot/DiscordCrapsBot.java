package com.gmo.discord.craps.bot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.gmo.discord.craps.bot.command.CommandInfo;
import com.gmo.discord.craps.bot.command.CrapsCommand;
import com.gmo.discord.craps.bot.command.HelpCommand;
import com.gmo.discord.craps.bot.command.ICommand;
import com.gmo.discord.craps.bot.command.InfoCommand;
import com.gmo.discord.craps.bot.command.PointCommand;
import com.gmo.discord.craps.bot.command.RollCommand;
import com.gmo.discord.craps.bot.entities.CrapsGameKey;
import com.gmo.discord.craps.bot.message.CrapsMessage;
import com.gmo.discord.craps.bot.message.DiceEmoji;
import com.gmo.discord.craps.bot.store.CrapsGameStore;
import com.gmo.discord.craps.bot.store.InMemoryCrapsGameStore;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Contains the engine code for the bot. Initiates the discord client and registers the bot listener.
 *
 * @author tedelen
 */
public class DiscordCrapsBot {
    private static final String PREFIX = "!";
    private static IDiscordClient client;

    private final CrapsGameStore gameStore;

    public static void main(String[] args) throws DiscordException, RateLimitException {
        final String token = System.getenv("CRAPS_BOT_TOKEN");
        if (token == null || token.isEmpty()) {
            throw new IllegalStateException("Could not get bot token");
        }
        System.out.println("Logging bot in...");
        client = new ClientBuilder().withToken(token).build();
        client.getDispatcher().registerListener(new DiscordCrapsBot(new InMemoryCrapsGameStore()));
        client.login();
    }

    public DiscordCrapsBot(final CrapsGameStore gameStore) {
        this.gameStore = Objects.requireNonNull(gameStore, "Null game store");
    }

    @EventSubscriber
    public void onReady(final ReadyEvent event) {
        System.out.println("Bot is now ready!");
    }

    private static final List<ICommand> COMMAND_LIST = new ArrayList<>();
    static {
        COMMAND_LIST.add(CrapsCommand.INSTANCE);
        COMMAND_LIST.add(RollCommand.INSTANCE);
        COMMAND_LIST.add(PointCommand.INSTANCE);
        COMMAND_LIST.add(InfoCommand.INSTANCE);
        COMMAND_LIST.add(HelpCommand.INSTANCE);
    }

    @EventSubscriber
    public void onMessage(final MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IMessage message = event.getMessage();
        final IUser user = message.getAuthor();
        if (user.isBot()) return;

        final IChannel channel = message.getChannel();
        final IGuild guild = message.getGuild();

        final String[] split = message.getContent().split(" ");

        if (split.length >= 1 && split[0].startsWith(PREFIX)) {
            String command = split[0].replaceFirst(PREFIX, "");
            String[] args = split.length >= 2 ? Arrays.copyOfRange(split, 1, split.length) : new String[0];

            final CommandInfo commandInfo = CommandInfo.newBuilder()
                    .withChannel(channel)
                    .withGuild(guild)
                    .withArgs(args)
                    .withCommand(command)
                    .withUser(message.getAuthor())
                    .build();
            final ICommand cmd = COMMAND_LIST.stream().filter(t-> t.canHandle(commandInfo)).findFirst().orElse(HelpCommand.INSTANCE);
            final IMessage previousMessage = channel.getMessageHistory().stream()
                    .filter(t -> t.getAuthor().equals(client.getOurUser()))
                    .findFirst()
                    .orElse(null);
            sendMessage(cmd.execute(commandInfo, gameStore), previousMessage, channel);
        }
    }

    private void sendMessage(final CrapsMessage resultMessage,
                             final IMessage previousMessage,
                             final IChannel channel) {
        resultMessage.getEmbedObject().ifPresent(embedObject -> {
            if (resultMessage.isReplacePrevious() && previousMessage != null) {
                previousMessage.edit(embedObject);
            } else {
                channel.sendMessage(embedObject);
            }
        });
        resultMessage.getText().ifPresent(text -> {
            if (resultMessage.isReplacePrevious() && previousMessage != null) {
                previousMessage.edit(text);
            } else {
                channel.sendMessage(text);
            }
        });
    }
}
