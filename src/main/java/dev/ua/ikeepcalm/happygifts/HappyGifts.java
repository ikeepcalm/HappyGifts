package dev.ua.ikeepcalm.happygifts;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.ua.ikeepcalm.happygifts.commands.GiftCommand;
import dev.ua.ikeepcalm.happygifts.events.GiftEventListener;
import dev.ua.ikeepcalm.happygifts.managers.GiftManager;
import dev.ua.ikeepcalm.happygifts.managers.LanguageManager;
import dev.ua.ikeepcalm.happygifts.systems.DeliverySystem;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class HappyGifts extends JavaPlugin {

    private GiftManager giftManager;
    private DeliverySystem deliverySystem;
    private LanguageManager languageManager;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this).silentLogs(false));
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

        getLogger().info("HappyGifts is enabling...");

        saveDefaultConfig();

        getLogger().info("Config has been saved!");

        saveResource("lang/en.yml", false);
        saveResource("lang/uk.yml", false);

        getLogger().info("Language files have been saved!");

        this.giftManager = new GiftManager(this);
        this.deliverySystem = new DeliverySystem(this);
        this.languageManager = new LanguageManager(this);

        getLogger().info("All managers have been initialized!");

        Bukkit.getPluginManager().registerEvents(new GiftEventListener(this), this);

        getLogger().info("Event listener has been registered!");

        registerCommands();

        getLogger().info("Commands have been registered!");

        getLogger().info("AgainstMyself has been enabled! Celebrations are ready!");
    }

    @Override
    public void onDisable() {
        getLogger().info("HappyGifts is disabling...");
        getLogger().info("Saving gifts...");
        if (giftManager != null) {
            giftManager.saveAllGifts();
            giftManager.saveActiveGifts();
            getLogger().info("All gifts have been saved!");
        }
        getLogger().info("HappyGifts has been disabled!");
    }

    private void registerCommands() {
        new GiftCommand(this).register();
    }

    public void log(String message) {
        getLogger().info(ChatColor.GOLD + message);
    }
}