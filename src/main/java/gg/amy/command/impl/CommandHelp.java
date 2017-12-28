package gg.amy.command.impl;

import gg.amy.Bot;
import gg.amy.command.ChatProcessor;
import gg.amy.command.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * @author amy
 * @since 12/17/17.
 */
public class CommandHelp extends Command {
    public CommandHelp(final Bot bot) {
        super(bot, "help", "Shows you useful info and commands.", new String[] {
                "`%PREFIX%help`", "`%PREFIX%help <command>`", "", "Ex: `%PREFIX%help game`"
        });
    }
    
    @Override
    public boolean run(final MessageReceivedEvent event, final String cmdName, final String argString, final List<String> args) {
        if(!args.isEmpty()) {
            final Command cmd = getBot().getChatProcessor().getCommands().get(args.get(0).toLowerCase());
            if(cmd != null && !cmd.isAdminCommand()) {
                final EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("Help").addField(ChatProcessor.PREFIX + cmd.getName(), cmd.getDesc(), false);
                final StringBuilder sb = new StringBuilder();
                for(final String s : cmd.getLongHelp()) {
                    sb.append(s.replace("%PREFIX%", ChatProcessor.PREFIX)).append('\n');
                }
                builder.addField("", sb.toString(), false);
                event.getChannel().sendMessage(builder.build()).queue();
            }
            return true;
        }
        final EmbedBuilder builder = new EmbedBuilder();
        final StringBuilder sb = new StringBuilder();
        getBot().getChatProcessor().getCommands().values()
                .stream().filter(e -> !e.isAdminCommand())
                .forEach(e -> sb.append(String.format("**%s%s**: %s\n", ChatProcessor.PREFIX, e.getName(), e.getDesc())));
        sb.append("\n\nSupport server: https://discord.gg/wdp8zq4");
        builder.addField("Help", sb.toString(), false);
        event.getChannel().sendMessage(builder.build()).queue();
        return true;
    }
}