package im.lily;

import im.lily.command.ChatProcesser;
import im.lily.state.GamesState;
import lombok.Data;
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
public final class Lily {
    @Getter
    private final Logger logger = Logger.getLogger("lily");
    @Getter
    private final ChatProcesser chatProcesser = new ChatProcesser(this);
    @Getter
    private final Collection<Shard> shards = new ArrayList<>();
    @Getter
    private final GamesState state = new GamesState(this);
    
    private Lily() {
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler() {
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
        });
    }
    
    public static void main(final String[] args) {
        new Lily().start();
    }
    
    private int getRecommendedShards() {
        // TODO: debug mode, remove
        return 1;
    }
    
    private String getToken() {
        return System.getenv("LILY_TOKEN");
    }
    
    private void start() {
        logger.info("Booting lily...");
        chatProcesser.registerCommands();
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
        logger.info("lily is fully booted!");
    }
}
