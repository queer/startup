package im.lily.command.impl;

import im.lily.Lily;
import im.lily.command.ChatProcesser;
import im.lily.command.Command;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * @author amy
 * @since 12/17/17.
 */
public class CommandHelp extends Command {
    public CommandHelp(final Lily lily) {
        super(lily, "help", "Shows you useful info about lily.", new String[] {
                "`-help`", "`-help <command>`", "", "Ex: `-help game`"
        });
    }
    
    @Override
    public boolean run(final MessageReceivedEvent event, final String cmdName, final String argString, final List<String> args) {
        if(!args.isEmpty()) {
            final Command cmd = getLily().getChatProcesser().getCommands().get(args.get(0).toLowerCase());
            if(cmd != null) {
                final EmbedBuilder builder = new EmbedBuilder();
                builder.setTitle("lily help").addField(ChatProcesser.PREFIX + cmd.getName(), cmd.getDesc(), false);
                final StringBuilder sb = new StringBuilder();
                for(final String s : cmd.getLongHelp()) {
                    sb.append(s).append('\n');
                }
                builder.addField("", sb.toString(), false);
                event.getChannel().sendMessage(builder.build()).queue();
            }
            return true;
        }
        final EmbedBuilder builder = new EmbedBuilder();
        final StringBuilder sb = new StringBuilder();
        getLily().getChatProcesser().getCommands().values()
                .forEach(e -> sb.append(String.format("**%s%s**: %s\n", ChatProcesser.PREFIX, e.getName(), e.getDesc())));
        builder.addField("lily help", sb.toString(), false);
        event.getChannel().sendMessage(builder.build()).queue();
        return true;
    }
}
