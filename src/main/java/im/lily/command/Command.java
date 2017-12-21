package im.lily.command;

import im.lily.Lily;
import lombok.Getter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * @author amy
 * @since 12/17/17.
 */
public abstract class Command {
    @Getter
    private final Lily lily;
    @Getter
    private final String name;
    @Getter
    private final String desc;
    @Getter
    private final String[] longHelp;
    
    public Command(final Lily lily, final String name, final String desc, final String[] longHelp) {
        this.lily = lily;
        this.name = name;
        this.desc = desc;
        this.longHelp = longHelp;
    }
    
    public abstract boolean run(MessageReceivedEvent event, String cmdName, String argString, List<String> args);
}
