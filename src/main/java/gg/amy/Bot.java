package gg.amy;

import gg.amy.state.GamesState;
import gg.amy.command.ChatProcessor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
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
    private final Collection<Shard> shards = new ArrayList<>();
    @Getter
    private final GamesState state = new GamesState(this);
    
    private Bot() {
        logger.setUseParentHandlers(false);
        logger.addHandler(new LogHandler());
    }
    
    public static void main(final String[] args) {
        new Bot().start();
    }
    
    private int getRecommendedShards() {
        // TODO: debug mode, remove
        return 1;
    }
    
    private String getToken() {
        return System.getenv("BOT_TOKEN");
    }
    
    private void start() {
        logger.info("Booting...");
        chatProcessor.registerCommands().start();
        logger.info("Registered commands!");
        final int recommendedShards = getRecommendedShards();
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
            Thread.sleep(2500L);
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
