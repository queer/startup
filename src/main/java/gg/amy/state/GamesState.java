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
    
    public Game getState(final ISnowflake guild, final ISnowflake user) {
        if(!states.containsKey(guild.getId())) {
            states.put(guild.getId(), new ConcurrentHashMap<>());
            return null;
        }
        return states.get(guild.getId()).get(user.getId());
    }
    
    public void updateState(final ISnowflake guild, final ISnowflake user, final Game game) {
        if(!states.containsKey(guild.getId())) {
            states.put(guild.getId(), new ConcurrentHashMap<>());
        }
        states.get(guild.getId()).put(user.getId(), game);
    }
    
    public void deleteState(final ISnowflake guild, final ISnowflake user) {
        if(states.containsKey(guild.getId())) {
            if(states.get(guild.getId()).containsKey(user.getId())) {
                states.get(guild.getId()).remove(user.getId());
            }
        }
    }
    
    public boolean isActive(final ISnowflake guild, final ISnowflake user) {
        return states.containsKey(guild.getId()) && states.get(guild.getId()).containsKey(user.getId());
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
