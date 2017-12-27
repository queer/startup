package gg.amy.games;

import gg.amy.Bot;
import lombok.Getter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

/**
 * @author amy
 * @since 12/18/17.
 */
public abstract class Game {
    @Getter
    private final Bot bot;
    @Getter
    private final String name;
    
    protected Game(final Bot bot, final String name) {
        this.bot = bot;
        this.name = name;
    }
    
    public abstract void initGame(MessageReceivedEvent event);
    
    public abstract void handleNextMove(MessageReceivedEvent event);
    
    public abstract void endGame(final MessageReceivedEvent event, final String title, final String field, final String msg);
    
    protected final String getTitle(final MessageReceivedEvent event) {
        return String.format("%s | %s", event.getAuthor().getName(), name);
    }
}
