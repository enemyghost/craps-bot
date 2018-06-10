package com.gmo.discord.craps.bot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

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
 * @author tedelen
 */
public class DiscordCrapsBot {
    private static final String TOKEN = "NDU1MTIwNzI2MDg1NzMwMzI0.Df3YQg.VTfmxCvJE3nW6QTbtCMp8uihY3o";
    private static final String PREFIX = "!";
    private static IDiscordClient client;

    private final CrapsGameStore gameStore;

    // Stores the last channel that the join command was sent from
    private final Map<IGuild, IChannel> lastChannel = new HashMap<>();

    public static void main(String[] args) throws DiscordException, RateLimitException {
        System.out.println("Logging bot in...");
        client = new ClientBuilder().withToken(TOKEN).build();
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

    /**
     * TODO use this and institute command pattern.
     */
    private enum Command {
        CRAPS("craps", false),
        ROLL("roll", true),
        POINT("point", false),
        DESCRIBE("describe", false);

        final String value;
        final boolean isChecked;
        Command(final String value, final boolean isChecked) {
            this.value = value;
            this.isChecked = isChecked;
        }

        public String getValue() {
            return value;
        }

        public boolean isChecked() {
            return isChecked;
        }
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

            final CrapsGameKey key = new CrapsGameKey(channel, guild);
            if (command.equalsIgnoreCase("craps")) {
                final OptionalInt bet = getBet(args);
                if (!bet.isPresent() || bet.getAsInt() <= 0) {
                    channel.sendMessage("Please provide a positive integer bet value. `!craps <bet>`");
                    return;
                }
                if (gameStore.getActiveGame(key).isPresent()) {
                    channel.sendMessage("Game is already in progress, use `!roll` to roll.\n" + gameStore.getActiveGame(key).toString());
                } else {
                    channel.sendMessage("Starting a new game.\n" + gameStore.newGame(key, message.getAuthor(), bet.getAsInt()));
                }
            } else if (command.equalsIgnoreCase("roll")) {
                final Optional<CrapsGame> gameOpt = gameStore.getActiveGame(key);
                if (!gameOpt.isPresent()) {
                    channel.sendMessage("Start a new game first. `!craps <bet>`");
                    return;
                } else if (gameOpt.get().getUser().getLongID() != message.getAuthor().getLongID()) {
                    channel.sendMessage("You cannot roll. There is a game in progress, but it is owned by " + gameOpt.get().getUser().getDisplayName(guild));
                    return;
                }
                final Roll roll = Roll.roll();
                final CrapsGame game = gameStore.updateGame(gameOpt.get(), roll);
                final StringBuilder sb = new StringBuilder();
                if (!game.isActive()) {
                    if (game.isWon()) {
                        if (roll.sevenOut()) {
                            sb.append("Seven winner! ").append(roll.toString()).append(" Pay the line: ").append(game.getBet() * 2);
                        } else if (roll.yo()) {
                            sb.append(roll.toString()).append("Winner! Pay the line: ").append(game.getBet() * 2);
                        } else {
                            sb.append(roll.toString()).append(", winner! That's the point. Pay the line: ").append(game.getBet() * 2);
                        }
                    } else if (game.isLost()) {
                        if (roll.craps()) {
                            sb.append(roll.toString()).append("You lose! Sad!");
                        } else {
                            sb.append(roll.toString()).append("Seven out! Seven out.");
                        }
                    }
                    gameStore.completeGame(game.getKey());
                    channel.sendMessage(sb.toString());
                } else {
                    channel.sendMessage(String.format("%s. The point is %d. Roll again!", roll.toString(), game.getPoint().getAsInt()));
                }
            } else if (command.equalsIgnoreCase("point")) {
                final Optional<CrapsGame> activeGame = gameStore.getActiveGame(key);
                if (activeGame.isPresent()) {
                    activeGame.get().getPoint().ifPresent(p -> channel.sendMessage("The point is " + p));
                } else {
                    channel.sendMessage("No game in progress, use `!craps <bet>`");
                }
            } else if (command.equalsIgnoreCase("describe")) {
                final Optional<CrapsGame> activeGame = gameStore.getActiveGame(key);
                if (activeGame.isPresent()) {
                    channel.sendMessage(activeGame.toString());
                } else {
                    channel.sendMessage("No game in progress, use `!craps <bet>`");
                }
            } else if (command.equalsIgnoreCase("help")) {
                channel.sendMessage("Type `!craps <bet>` to start a new game.");
            }
        }
    }

    private OptionalInt getBet(final String[] args) {
        if (args.length == 0) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(args[0]));
        } catch (final NumberFormatException e) {
            return OptionalInt.empty();
        }
    }
}
