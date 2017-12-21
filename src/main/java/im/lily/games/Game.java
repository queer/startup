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
    
    protected Game(final Lily lily) {
        this.lily = lily;
    }
    
    public abstract void initGame(MessageReceivedEvent event);
    
    public abstract void handleNextMove(MessageReceivedEvent event);
}
