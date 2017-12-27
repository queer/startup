package gg.amy.command.impl;

import gg.amy.Bot;
import gg.amy.command.Command;
import net.dv8tion.jda.core.entities.Icon;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * @author amy
 * @since 12/27/17.
 */
public class CommandRename extends Command {
    public CommandRename(final Bot bot) {
        super(bot, "rename", "", new String[0]);
    }
    
    @Override
    public boolean run(final MessageReceivedEvent event, final String cmdName, final String argString, final List<String> args) {
        getBot().getShards().forEach(shard -> {
            try {
                final String url = args.remove(0);
                final BufferedImage image = ImageIO.read(new URL(url));
                final ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "png", baos);
                final byte[] bytes = baos.toByteArray();
                baos.close();
                shard.getJda().getSelfUser().getManagerUpdatable().getNameField().setValue(String.join(" ", args))
                        .getAvatarField().setValue(Icon.from(bytes)).update()
                        .queue($ -> event.getChannel().sendMessage("Renamed! :tada:").queue());
            } catch(final IOException e) {
                e.printStackTrace();
            }
        });
        return true;
    }
    
    @Override
    public boolean isAdminCommand() {
        return true;
    }
}
