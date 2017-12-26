package im.lily.command.impl;

import im.lily.Lily;
import im.lily.command.ChatProcesser;
import im.lily.command.Command;
import im.lily.games.Game;
import im.lily.games.StartupSimulator;
import lombok.Getter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author amy
 * @since 12/17/17.
 */
public class CommandGame extends Command {
    public CommandGame(final Lily lily) {
        super(lily, "game", "See available games, or start a new one.", new String[]{
                "`%PREFIX%game`", "`%PREFIX%game <game>`", "`%PREFIX%game end`", "",
                "Ex: `%PREFIX%game`", "Ex: `%PREFIX%game startup`", "Ex: `%PREFIX%game end`"
        });
    }
    
    @Override
    public boolean run(final MessageReceivedEvent event, final String cmdName, final String argString, final List<String> args) {
        if(args.isEmpty()) {
            EmbedBuilder builder = new EmbedBuilder();
            final StringBuilder sb = new StringBuilder();
            for(final LilyGame game : LilyGame.values()) {
                sb.append(String.format("- %s (`%s`)\n", game.getGameName(), game.getGameId()));
                sb.append("  '").append(game.getDesc()).append("'\n");
            }
            sb.append(String.format("\nYou can start a game by doing `%sgame <id>`, ex. `%sgame startup`.",
                    ChatProcesser.PREFIX, ChatProcesser.PREFIX));
            event.getChannel().sendMessage(builder.addField("The following games are available:", sb.toString(), false).build()).queue();
        } else {
            if(args.get(0).equalsIgnoreCase("end")) {
                if(getLily().getChatProcesser().getActiveGames().containsKey(event.getAuthor().getId())) {
                    getLily().getChatProcesser().getActiveGames().remove(event.getAuthor().getId());
                    event.getChannel().sendMessage(new EmbedBuilder().addField("Game ended", "Thanks for playing!", false).build()).queue();
                } else {
                    event.getChannel().sendMessage(new EmbedBuilder().addField("Error", "You aren't playing a game!", false).build()).queue();
                }
                return true;
            }
            if(getLily().getChatProcesser().getActiveGames().containsKey(event.getAuthor().getId())) {
                event.getChannel().sendMessage(new EmbedBuilder().addField("Error", "You're already playing a game!", false).build()).queue();
                return true;
            }
            for(final LilyGame lilyGame : LilyGame.values()) {
                if(lilyGame.getGameId().equalsIgnoreCase(args.get(0))) {
                    final Class<? extends Game> c = lilyGame.getGameClass();
                    try {
                        final Game game = c.getConstructor(Lily.class).newInstance(getLily());
                        getLily().getChatProcesser().getActiveGames().put(event.getAuthor().getId(), game);
                        game.initGame(event);
                    } catch(final InstantiationException | NoSuchMethodException | InvocationTargetException
                            | IllegalAccessException e) {
                        e.printStackTrace();
                        event.getChannel().sendMessage("Couldn't init game!?").queue();
                    }
                    return true;
                }
            }
        }
        return true;
    }
    
    public enum LilyGame {
        STARTUP("Startup Simulator", "startup", "A unicorn startup simulation game based on the original by Toggl.", StartupSimulator.class),;
        
        @Getter
        private final String gameName;
        
        @Getter
        private final String gameId;
        
        @Getter
        private final Class<? extends Game> gameClass;
        
        @Getter
        private final String desc;
    
        LilyGame(final String gameName, final String gameId, final String desc, final Class<? extends Game> gameClass) {
            this.gameName = gameName;
            this.gameClass = gameClass;
            this.gameId = gameId;
            this.desc = desc;
        }
    }
}
