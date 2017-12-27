package gg.amy.state;

import gg.amy.Bot;
import gg.amy.games.Game;
import lombok.Getter;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author amy
 * @since 12/26/17.
 */
@SuppressWarnings({"TypeMayBeWeakened", "unused"})
public class GamesState {
    @Getter
    private final Bot bot;
    private final Map<String, Map<String, Game>> states = new ConcurrentHashMap<>();
    
    public GamesState(final Bot bot) {
        this.bot = bot;
    }
    
    public Game getState(final Guild guild, final User user) {
        if(!states.containsKey(guild.getId())) {
            states.put(guild.getId(), new ConcurrentHashMap<>());
            return null;
        }
        return states.get(guild.getId()).get(user.getId());
    }
    
    public void updateState(final Guild guild, final User user, final Game game) {
        if(!states.containsKey(guild.getId())) {
            states.put(guild.getId(), new ConcurrentHashMap<>());
        }
        states.get(guild.getId()).put(user.getId(), game);
    }
    
    public void deleteState(final Guild guild, final User user) {
        if(states.containsKey(guild.getId())) {
            if(states.get(guild.getId()).containsKey(user.getId())) {
                states.get(guild.getId()).remove(user.getId());
            }
        }
    }
    
    public boolean isActive(final Guild guild, final User user) {
        if(states.containsKey(guild.getId())) {
            return states.get(guild.getId()).containsKey(user.getId());
        }
        return false;
    }
}
