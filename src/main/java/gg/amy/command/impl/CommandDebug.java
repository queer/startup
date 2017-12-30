package gg.amy.command.impl;

import gg.amy.Bot;
import gg.amy.Shard;
import gg.amy.command.Command;
import gg.amy.games.Game;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author amy
 * @since 12/28/17.
 */
public class CommandDebug extends Command {
    public CommandDebug(final Bot bot) {
        super(bot, "debug", "", new String[0]);
    }
    
    @Override
    public boolean run(final MessageReceivedEvent event, final String cmdName, final String argString, final List<String> args) {
        int shards = 0;
        int guilds = 0;
        int users = 0;
        int uniques = 0;
        int textChannels = 0;
        int voiceChannels = 0;
        final AtomicInteger games = new AtomicInteger(0);
        getBot().getState().getStates().forEach((snowflake, value) -> value.forEach((user, game) -> {
            if(getBot().getState().isActive(snowflake, user)) {
                games.incrementAndGet();
            }
        }));
        final int upvotes = getBot().getWhoUpvoted().size();
        
        for(final Shard shard : getBot().getShards()) {
            shards++;
            guilds += shard.getJda().getGuildCache().size();
            users += shard.getJda().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum();
            uniques += shard.getJda().getUserCache().size();
            voiceChannels += shard.getJda().getVoiceChannelCache().size();
            textChannels += shard.getJda().getTextChannelCache().size();
        }
        @SuppressWarnings("StringBufferReplaceableByString")
        final StringBuilder sb = new StringBuilder("```\n");
        sb.append(" Shards: ").append(shards).append('\n');
        sb.append(" Guilds: ").append(guilds).append('\n');
        sb.append("  Users: ").append(users).append('\n');
        sb.append("Uniques: ").append(uniques).append('\n');
        sb.append("   Text: ").append(textChannels).append('\n');
        sb.append("  Voice: ").append(voiceChannels).append('\n');
        sb.append("  Games: ").append(games.get()).append('\n');
        sb.append("Upvotes: ").append(upvotes).append('\n');
        sb.append("```");
        
        event.getChannel().sendMessage(sb.toString()).queue();
        return false;
    }
    
    @Override
    public boolean isAdminCommand() {
        return true;
    }
}
