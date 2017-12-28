package gg.amy.command.impl;

import gg.amy.Bot;
import gg.amy.command.ChatProcessor;
import gg.amy.command.Command;
import gg.amy.games.StartupSimulator;
import gg.amy.games.Game;
import lombok.Getter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author amy
 * @since 12/17/17.
 */
public class CommandStartup extends Command {
    public CommandStartup(final Bot bot) {
        super(bot, "startup", "Start or end your startup simulation.", new String[] {
                "`%PREFIX%startup`", "`%PREFIX%startup start`", "`%PREFIX%startup end`"
        });
    }
    
    @Override
    public boolean run(final MessageReceivedEvent event, final String cmdName, final String argString, final List<String> args) {
        if(args.isEmpty()) {
            final EmbedBuilder builder = new EmbedBuilder();
            final StringBuilder sb = new StringBuilder();
            for(final StartupGame game : StartupGame.values()) {
                sb.append(String.format("%s\n", game.getGameName()));
                sb.append(game.getDesc()).append('\n');
            }
            sb.append(String.format("Start your game with **%sstartup start**, or end it with **%sstartup end**", ChatProcessor.PREFIX, ChatProcessor.PREFIX));
            event.getChannel().sendMessage(builder.addField("Game info", sb.toString(), false).build()).queue();
        } else {
            if(args.get(0).equalsIgnoreCase("end")) {
                if(getBot().getState().getState(event.getGuild(), event.getAuthor()) != null) {
                    getBot().getState().deleteState(event.getGuild(), event.getAuthor());
                    event.getChannel().sendMessage(new EmbedBuilder().addField("Simulation ended", "Thanks for playing!", false).build()).queue();
                } else {
                    event.getChannel().sendMessage(new EmbedBuilder().addField("Error", "You aren't playing a game!", false).build()).queue();
                }
                return true;
            }
            if(args.get(0).equalsIgnoreCase("start")) {
                if(getBot().getState().isActive(event.getGuild(), event.getAuthor())) {
                    event.getChannel().sendMessage(new EmbedBuilder().addField("Error", "You're already playing a game!", false).build()).queue();
                    return true;
                }
                for(final StartupGame startupGame : StartupGame.values()) {
                    if(startupGame.getGameId().equalsIgnoreCase("startup")) {
                        final Class<? extends Game> c = startupGame.getGameClass();
                        try {
                            final Game game = c.getConstructor(Bot.class).newInstance(getBot());
                            getBot().getState().updateState(event.getGuild(), event.getAuthor(), game);
                            game.initGame(event);
                        } catch(final InstantiationException | NoSuchMethodException | InvocationTargetException
                                | IllegalAccessException e) {
                            e.printStackTrace();
                            event.getChannel().sendMessage("Couldn't start game!?").queue();
                        }
                        return true;
                    }
                }
                return true;
            }

        }
        return true;
    }
    
    public enum StartupGame {
        STARTUP("Startup Simulator", "startup", "A unicorn startup simulation game based on the original by Toggl.", StartupSimulator.class),;
        
        @Getter
        private final String gameName;
        
        @Getter
        private final String gameId;
        
        @Getter
        private final Class<? extends Game> gameClass;
        
        @Getter
        private final String desc;
        
        StartupGame(final String gameName, final String gameId, final String desc, final Class<? extends Game> gameClass) {
            this.gameName = gameName;
            this.gameClass = gameClass;
            this.gameId = gameId;
            this.desc = desc;
        }
    }
}
