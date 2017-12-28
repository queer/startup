package gg.amy.command;

import gg.amy.Bot;
import gg.amy.command.impl.CommandHelp;
import gg.amy.command.impl.CommandRename;
import gg.amy.command.impl.CommandStartup;
import gg.amy.games.Game;
import lombok.Getter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;
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
            while(true) {
                // Prune every minute
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(1L));
                } catch(final InterruptedException e) {
                    e.printStackTrace();
                }
                final AtomicInteger counter = new AtomicInteger(0);
                this.bot.getState().getStates().forEach((guild, map) -> map.forEach((user, game) -> {
                    // If it's been >= 5 minutes, prune
                    if(System.currentTimeMillis() - game.getLastInteraction() >= TimeUnit.MINUTES.toMillis(5L)) {
                        counter.incrementAndGet();
                        map.remove(user);
                    }
                }));
                if(counter.get() > 0) {
                    this.bot.getLogger().info("Pruned " + counter.get() + " games.");
                }
            }
        });
    }
    
    public ChatProcessor registerCommands() {
        commands.put("help", new CommandHelp(bot));
        commands.put("startup", new CommandStartup(bot));
        commands.put("rename", new CommandRename(bot));
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
                            bot.getLogger().info("Processing command: " + cmdName);
                            command.run(m, cmdName, finalArgString, args);
                        }
                    });
                } else {
                    final Game state = bot.getState().getState(m.getGuild(), m.getAuthor());
                    if(state != null) {
                        state.handleNextMove(m);
                    }
                }
            }
        });
    }
}
