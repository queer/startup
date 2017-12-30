package gg.amy.state;

import gg.amy.Bot;
import gg.amy.games.Game;
import lombok.Getter;
import net.dv8tion.jda.core.entities.ISnowflake;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author amy
 * @since 12/26/17.
 */
@SuppressWarnings("unused")
public class GamesState {
    @Getter
    private final Bot bot;
    @Getter
    private final Map<String, Map<String, Game>> states = new ConcurrentHashMap<>();
    
    public GamesState(final Bot bot) {
        this.bot = bot;
    }
    
    public Game getState(final ISnowflake snowflake, final ISnowflake user) {
        if(!states.containsKey(snowflake.getId())) {
            states.put(snowflake.getId(), new ConcurrentHashMap<>());
            return null;
        }
        return states.get(snowflake.getId()).get(user.getId());
    }
    
    public void updateState(final ISnowflake snowflake, final ISnowflake user, final Game game) {
        if(!states.containsKey(snowflake.getId())) {
            states.put(snowflake.getId(), new ConcurrentHashMap<>());
        }
        states.get(snowflake.getId()).put(user.getId(), game);
    }
    
    public void deleteState(final ISnowflake snowflake, final ISnowflake user) {
        if(states.containsKey(snowflake.getId())) {
            if(states.get(snowflake.getId()).containsKey(user.getId())) {
                states.get(snowflake.getId()).remove(user.getId());
            }
        }
    }
    
    public boolean isActive(final String snowflake, final String user) {
        return states.containsKey(snowflake) && states.get(snowflake).containsKey(user);
    }
    
    public boolean isActive(final ISnowflake snowflake, final ISnowflake user) {
        return isActive(snowflake.getId(), user.getId());
    }
    
    public static ISnowflake getSnowflake(MessageReceivedEvent event) {
        if(event.getGuild() != null) {
            return event.getGuild();
        }
        if(event.getPrivateChannel() != null) {
            return event.getPrivateChannel();
        }
        return null;
    }
}
