package gg.amy.games;

import gg.amy.Bot;
import lombok.Getter;
import lombok.Setter;
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
    
    @Getter
    @Setter
    private long lastInteraction;
    
    protected Game(final Bot bot, final String name) {
        this.bot = bot;
        this.name = name;
        lastInteraction = System.currentTimeMillis();
    }
    
    public abstract void initGame(MessageReceivedEvent event);
    
    public abstract void handleNextMove(MessageReceivedEvent event);
    
    public abstract void endGame(final MessageReceivedEvent event, final String title, final String field, final String msg);
    
    protected final String getTitle(final MessageReceivedEvent event, final String s) {
        return String.format("%s | %s", event.getAuthor().getName(), s);
    }
    
    protected final String getTitle(final MessageReceivedEvent event) {
        return String.format("%s | %s", event.getAuthor().getName(), name);
    }
}
