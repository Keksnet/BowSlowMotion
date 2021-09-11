package de.neo.bow;

import de.neo.bow.commands.BowReloadCommand;
import de.neo.bow.listener.PlayerBowListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.HashSet;

public class BowMotionMain extends JavaPlugin {

    private static BowMotionMain INSTANCE;

    public static BowMotionMain getInstance() {
        return INSTANCE;
    }

    public static HashSet<Material> arrows;

    @Override
    public void onEnable() {
        INSTANCE = this;
        arrows = new HashSet<>();
        loadArrows();
        loadConfig();
        Bukkit.getPluginManager().registerEvents(new PlayerBowListener(), this);
        getCommand("bowrl").setExecutor(new BowReloadCommand());
    }

    private void loadArrows() {
        EnumSet.allOf(Material.class).stream().filter(material ->
                material.toString().toLowerCase().contains("arrow")
        ).forEach(material -> arrows.add(material));
    }

    private void loadConfig() {
        if(!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        Path conf = Paths.get(getDataFolder().getAbsolutePath(), "config.yml");
        if(!Files.exists(conf)) {
            saveDefaultConfig();
        }
    }
}
