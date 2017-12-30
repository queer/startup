package gg.amy.command;

import gg.amy.Bot;
import gg.amy.command.impl.CommandDebug;
import gg.amy.command.impl.CommandHelp;
import gg.amy.command.impl.CommandRename;
import gg.amy.command.impl.CommandStartup;
import gg.amy.games.Game;
import gg.amy.state.GamesState;
import lombok.Getter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author amy
 * @since 12/17/17.
 */
public class ChatProcessor implements EventListener {
    public static final String PREFIX = System.getenv("BOT_PREFIX") != null ? System.getenv("BOT_PREFIX") : "-";
    
    @Getter
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    
    private final Bot bot;
    
    private final Executor pool = Executors.newCachedThreadPool();
    
    private final Thread pruneThread;
    
    public ChatProcessor(final Bot bot) {
        this.bot = bot;
        pruneThread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while(true) {
                // Prune every minute
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(1L));
                } catch(final InterruptedException e) {
                    e.printStackTrace();
                }
                final AtomicInteger pruned = new AtomicInteger(0);
                final AtomicInteger active = new AtomicInteger(0);
                this.bot.getState().getStates().forEach((guild, map) -> map.forEach((user, game) -> {
                    // If it's been >= 5 minutes, prune
                    if(System.currentTimeMillis() - game.getLastInteraction() >= TimeUnit.MINUTES.toMillis(5L)) {
                        pruned.incrementAndGet();
                        map.remove(user);
                    } else {
                        active.incrementAndGet();
                    }
                }));
                if(pruned.get() > 0) {
                    this.bot.getLogger().info("Pruned " + pruned.get() + " games.");
                }
                // Stuff other stats in here too :D
                this.bot.getMetrics().getClient().gauge("active-games", active.get());
                bot.getShards().forEach(shard -> bot.getMetrics().getClient().gauge("guilds", shard.getJda().getGuildCache().size(),
                        "shard:" + shard.getShardId()));
            }
        });
    }
    
    public ChatProcessor registerCommands() {
        commands.put("help", new CommandHelp(bot));
        commands.put("startup", new CommandStartup(bot));
        commands.put("rename", new CommandRename(bot));
        commands.put("debug", new CommandDebug(bot));
        return this;
    }
    
    public void start() {
        pruneThread.start();
    }
    
    @Override
    public void onEvent(final Event event) {
        pool.execute(() -> {
            if(event instanceof MessageReceivedEvent) {
                final MessageReceivedEvent m = (MessageReceivedEvent) event;
                final Message msg = ((MessageReceivedEvent) event).getMessage();
                final String content = msg.getContentRaw();
                if(content.startsWith(PREFIX)) {
                    final String cmd = content.substring(PREFIX.length());
                    final String[] pieces = cmd.split("\\s+", 2);
                    final String cmdName = pieces[0].toLowerCase();
                    String argString = null;
                    if(pieces.length > 1) {
                        argString = pieces[1];
                    }
                    final List<String> args = new ArrayList<>();
                    if(argString != null) {
                        args.addAll(Arrays.asList(argString.split("\\s+")));
                    }
                    final String finalArgString = argString;
                    Optional.ofNullable(commands.get(cmdName)).ifPresent(command -> {
                        if(command.isAdminCommand()) {
                            if(m.getAuthor().getId().equalsIgnoreCase("128316294742147072")) {
                                bot.getLogger().info("Processing admin command: " + cmdName);
                                command.run(m, cmdName, finalArgString, args);
                            }
                        } else {
                            //bot.getLogger().info("Processing command: " + cmdName);
                            command.run(m, cmdName, finalArgString, args);
                        }
                    });
                } else {
                    final Game state = bot.getState().getState(GamesState.getSnowflake(m), m.getAuthor());
                    if(state != null) {
                        state.handleNextMove(m);
                    }
                }
            } else if(event instanceof GuildJoinEvent) {
                // TODO: Send join message?
                final GuildJoinEvent g = (GuildJoinEvent) event;
                final Guild guild = g.getGuild();
                bot.getLogger().info("Joined '" + guild.getName() + "' " + guild.getId() + " +" + guild.getMembers().size());
                bot.getMetrics().getClient().gauge("guilds", g.getJDA().getGuildCache().size(), "shard:" + g.getJDA().getShardInfo().getShardId());
            } else if(event instanceof GuildLeaveEvent) {
                final GuildLeaveEvent g = (GuildLeaveEvent) event;
                final Guild guild = g.getGuild();
                bot.getLogger().info("Joined '" + guild.getName() + "' " + guild.getId() + " +" + guild.getMembers().size());
                bot.getMetrics().getClient().gauge("guilds", g.getJDA().getGuildCache().size(), "shard:" + g.getJDA().getShardInfo().getShardId());
            }
        });
    }
}
