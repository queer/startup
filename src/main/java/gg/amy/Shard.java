package gg.amy;

import gg.amy.command.ChatProcessor;
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
    private final Bot bot;
    private final String token;
    @Getter
    private final int shardId;
    @Getter
    private final int shardLimit;
    @Getter
    private JDA jda;
    
    Shard(final Bot bot, final String token, final int shardId, final int shardLimit) {
        this.bot = bot;
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
                    .setGame(Game.of(Game.GameType.DEFAULT, ChatProcessor.PREFIX + "help"))
                    .addEventListener((EventListener) event -> {
                        if(event instanceof ReadyEvent) {
                            bot.getLogger().info("Shard " + shardId + " booted and ready to go!");
                        }
                    })
                    .addEventListener(bot.getChatProcessor())
                    .setStatus(OnlineStatus.DO_NOT_DISTURB)
                    .buildBlocking(JDA.Status.AWAITING_LOGIN_CONFIRMATION);
        } catch(final LoginException | InterruptedException | RateLimitedException e) {
            e.printStackTrace();
        }
        return this;
    }
}
