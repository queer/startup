package gg.amy.command;

import gg.amy.Bot;
import lombok.Getter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * @author amy
 * @since 12/17/17.
 */
public abstract class Command {
    @Getter
    private final Bot bot;
    @Getter
    private final String name;
    @Getter
    private final String desc;
    @Getter
    private final String[] longHelp;
    
    public Command(final Bot bot, final String name, final String desc, final String[] longHelp) {
        this.bot = bot;
        this.name = name;
        this.desc = desc;
        this.longHelp = longHelp;
    }
    
    public abstract boolean run(MessageReceivedEvent event, String cmdName, String argString, List<String> args);
    
    public boolean isAdminCommand() {
        return false;
    }
}
