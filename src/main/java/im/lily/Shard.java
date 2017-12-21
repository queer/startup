package im.lily;

import im.lily.command.ChatProcesser;
import lombok.Getter;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;

import javax.security.auth.login.LoginException;

/**
 * @author amy
 * @since 12/17/17.
 */
@SuppressWarnings("unused")
public class Shard {
    @Getter
    private final Lily lily;
    private final String token;
    @Getter
    private final int shardId;
    @Getter
    private final int shardLimit;
    @Getter
    private JDA jda;
    
    Shard(final Lily lily, final String token, final int shardId, final int shardLimit) {
        this.lily = lily;
        this.token = token;
        this.shardId = shardId;
        this.shardLimit = shardLimit;
    }
    
    @SuppressWarnings("UnnecessarilyQualifiedInnerClassAccess")
    public Shard connect() {
        try {
            jda = new JDABuilder(AccountType.BOT)
                    .setToken(token)
                    .useSharding(shardId, shardLimit)
                    .setAudioEnabled(false)
                    .setGame(Game.of(Game.GameType.DEFAULT, ChatProcesser.PREFIX + "help"))
                    .addEventListener((EventListener) event -> {
                        if(event instanceof ReadyEvent) {
                            lily.getLogger().info("lily shard " + shardId + " booted and ready to go!");
                        }
                    })
                    .addEventListener(lily.getChatProcesser())
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
        } catch(final LoginException | InterruptedException | RateLimitedException e) {
            e.printStackTrace();
        }
        return this;
    }
}
