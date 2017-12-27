package im.lily.games;

import im.lily.Lily;
import lombok.Getter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * @author amy
 * @since 12/18/17.
 */
public abstract class Game {
    @Getter
    private final Lily lily;
    @Getter
    private final String name;
    
    protected Game(final Lily lily, final String name) {
        this.lily = lily;
        this.name = name;
    }
    
    public abstract void initGame(MessageReceivedEvent event);
    
    public abstract void handleNextMove(MessageReceivedEvent event);
    
    protected final String getTitle(final MessageReceivedEvent event) {
        return String.format("%s | %s", event.getAuthor().getName(), name);
    }
}
