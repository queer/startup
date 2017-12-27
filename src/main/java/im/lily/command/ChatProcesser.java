package im.lily.command;

import im.lily.Lily;
import im.lily.command.impl.CommandGame;
import im.lily.command.impl.CommandHelp;
import im.lily.games.Game;
import lombok.Getter;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.EventListener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author amy
 * @since 12/17/17.
 */
public class ChatProcesser implements EventListener {
    public static final String PREFIX = System.getenv("LILY_PREFIX") != null ? System.getenv("LILY_PREFIX") : "-";
    
    @Getter
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    
    private final Lily lily;
    
    public ChatProcesser(final Lily lily) {
        this.lily = lily;
    }
    
    public ChatProcesser registerCommands() {
        commands.put("help", new CommandHelp(lily));
        commands.put("game", new CommandGame(lily));
        return this;
    }
    
    @Override
    public void onEvent(final Event event) {
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
                    lily.getLogger().info("Processing command: " + cmdName);
                    command.run(m, cmdName, finalArgString, args);
                });
            } else {
                final Game state = lily.getState().getState(m.getGuild(), m.getAuthor());
                if(state != null) {
                    state.handleNextMove(m);
                }
            }
        }
    }
}
