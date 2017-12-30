package gg.amy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import gg.amy.command.ChatProcessor;
import gg.amy.state.GamesState;
import lombok.Getter;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @author amy
 * @since 12/17/17.
 */
public final class Bot {
    @Getter
    private final Logger logger = Logger.getLogger("Bot");
    @Getter
    private final ChatProcessor chatProcessor = new ChatProcessor(this);
    @Getter
    private final List<Shard> shards = new ArrayList<>();
    @Getter
    private final GamesState state = new GamesState(this);
    @Getter
    private final Metrics metrics = new Metrics();
    @Getter
    private final Collection<String> whoUpvoted = new CopyOnWriteArrayList<>();
    
    private Bot() {
        logger.setUseParentHandlers(false);
        logger.addHandler(new LogHandler());
    }
    
    public static void main(final String[] args) {
        new Bot().start();
    }
    
    public boolean didUpvote(MessageReceivedEvent event) {
        return whoUpvoted.contains(event.getAuthor().getId());
    }
    
    private int getRecommendedShards() {
        if(System.getenv("BOT_DEBUG") != null
                && Boolean.parseBoolean(System.getenv("BOT_DEBUG"))) {
            getLogger().info("### DEBUG MODE: 2 SHARDS");
            return 2;
        }
        try {
            return Unirest.get("https://discordapp.com/api/gateway/bot")
                    .header("Authorization", "Bot " + getToken())
                    .header("Content-Type", "application/json")
                    .asJson().getBody().getObject().getInt("shards");
        } catch(final Exception e) {
            e.printStackTrace();
        }
        return 1;
    }
    
    private String getToken() {
        return System.getenv("BOT_TOKEN");
    }
    
    private void start() {
        logger.info("Booting...");
        final Thread thread = new Thread(() -> {
            //noinspection InfiniteLoopStatement
            while(true) {
                try {
                    final String response = Unirest.get("https://discordbots.org/api/bots/383113162670604289/votes?onlyids=true")
                            .header("Authorization", System.getenv("DBL_TOKEN"))
                            .header("Content-Type", "application/json").asString().getBody();
                    final Collection<String> snowflakes = new ArrayList<>(Arrays.asList(new ObjectMapper().readValue(response, new TypeReference<String[]>() {
                    })));
                    logger.info("Got snowflakes: " + snowflakes);
                    whoUpvoted.clear();
                    whoUpvoted.addAll(snowflakes);
                } catch(final UnirestException | IOException e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(TimeUnit.MINUTES.toMillis(1));
                } catch(final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setName("DBL upvote grabber");
        thread.start();
        logger.info("Started upvote grabber!");
        
        chatProcessor.registerCommands().start();
        logger.info("Registered commands!");
        final int recommendedShards = getRecommendedShards();
        logger.info("Booting with " + recommendedShards + " recommended shards...");
        for(int i = 0; i < recommendedShards; i++) {
            logger.info("Booting shard " + i + "...");
            final Shard shard = new Shard(this, getToken(), i, recommendedShards).connect();
            shards.add(shard);
            if(i < recommendedShards - 1) {
                try {
                    Thread.sleep(6000L);
                } catch(final InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Thread.sleep(6000L);
        } catch(final InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("Fully booted!");
    }
    
    private static class LogHandler extends Handler {
        @Override
        public void publish(final LogRecord record) {
            System.out.println(String.format("[%s][%s][%s] %s", record.getLoggerName(), record.getLevel().getName(),
                    record.getThreadID(), record.getMessage()));
        }
        
        @Override
        public void flush() {
        
        }
        
        @Override
        public void close() throws SecurityException {
        
        }
    }
}
