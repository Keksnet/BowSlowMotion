package de.neo.bow;

import de.neo.bow.commands.BowReloadCommand;
import de.neo.bow.listener.PlayerBowListener;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

    private boolean ncpSupport;

    @Override
    public void onEnable() {
        INSTANCE = this;
        arrows = new HashSet<>();
        loadArrows();
        loadConfig();
        Bukkit.getPluginManager().registerEvents(new PlayerBowListener(), this);
        PluginCommand bowRl = getCommand("bowrl");
        if(bowRl != null) {
            bowRl.setExecutor(new BowReloadCommand());
        }
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        this.ncpSupport = this.getConfig().getBoolean("options.ncp_support", true);
        getLogger().info("Enabled NoCheatPlus support");
    }

    private void loadArrows() {
        EnumSet.allOf(Material.class).stream().filter(material ->
                material.toString().toLowerCase().contains("arrow")
        ).forEach(material -> arrows.add(material));
    }

    private void loadConfig() {
        if(!getDataFolder().exists()) {
            if(!getDataFolder().mkdir()) {
                throw new RuntimeException(getDataFolder().getAbsolutePath() + " can not be created.");
            }
        }
        Path conf = Paths.get(getDataFolder().getAbsolutePath(), "config.yml");
        if(!Files.exists(conf)) {
            saveDefaultConfig();
            reloadConfig();
        }
        this.ncpSupport = this.getConfig().getBoolean("options.ncp_support", true);
        getLogger().info("Enabled NoCheatPlus support");
        InputStream defaultConf = getResource("config.yml");
        if(defaultConf == null) throw new RuntimeException("default config is missing");
        FileConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(defaultConf));
        def.getKeys(true).stream().filter(key -> !getConfig().contains(key, true))
                .forEach(key -> getConfig().set(key, def.get(key)));
        try {
            getConfig().save(conf.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isNcpSupportDisabled() {
        return !this.ncpSupport;
    }
}
