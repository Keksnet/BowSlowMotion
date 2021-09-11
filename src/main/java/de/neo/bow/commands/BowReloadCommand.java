package de.neo.bow.commands;

import de.neo.bow.BowMotionMain;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class BowReloadCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        BowMotionMain main = BowMotionMain.getInstance();
        main.reloadConfig();
        FileConfiguration config = main.getConfig();
        String reload_msg = ChatColor.translateAlternateColorCodes('&', config.getString("messages.prefix") + " " + config.getString("messages.reload_complete"));
        sender.spigot().sendMessage(TextComponent.fromLegacyText(reload_msg));
        return false;
    }

}
