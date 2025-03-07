package dev.ua.ikeepcalm.happygifts.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages internationalization for the plugin
 * Supports player client locale preferences
 */
public class LanguageManager {
    private final JavaPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Map<String, YamlConfiguration> languages = new HashMap<>();

    private String defaultLanguage = "en";

    public LanguageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        loadLanguages();
    }

    /**
     * Load language settings from config
     */
    private void loadConfig() {
        // Get language settings from config
        defaultLanguage = plugin.getConfig().getString("language.default", "en");

        plugin.getLogger().info("Default language: " + defaultLanguage);
    }

    /**
     * Load all language files from the lang folder
     */
    private void loadLanguages() {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
            saveDefaultLanguageFiles(langFolder);
        }

        // Load all language files
        File[] langFiles = langFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (langFiles != null) {
            for (File file : langFiles) {
                String langCode = file.getName().replace(".yml", "");
                languages.put(langCode, YamlConfiguration.loadConfiguration(file));
                plugin.getLogger().info("Loaded language: " + langCode);
            }
        }
    }

    /**
     * Create default language files if they don't exist
     */
    private void saveDefaultLanguageFiles(File langFolder) {
        // Save default English language file
        File enFile = new File(langFolder, "en.yml");
        if (!enFile.exists()) {
            try {
                enFile.createNewFile();
                YamlConfiguration enConfig = YamlConfiguration.loadConfiguration(enFile);
                setDefaultEnglishMessages(enConfig);
                enConfig.save(enFile);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create default language file: " + e.getMessage());
            }
        }
    }

    /**
     * Set default English messages
     */
    private void setDefaultEnglishMessages(YamlConfiguration config) {
        // GUI Titles
        config.set("gui.main.title", "<light_purple>✿ Women's Day Gift System ✿");
        config.set("gui.create.title", "<gold>Create Women's Day Gift");
        config.set("gui.recipient.title", "<gold>Select Gift Recipient");
        config.set("gui.mygifts.title", "<aqua>My Gifts");

        // Border Items
        config.set("gui.border.name", "<light_purple>✿ Women's Day ✿");
        config.set("gui.create_border.name", "<light_purple>✿ Gift Creation ✿");

        // Main Menu Items
        config.set("gui.main.create_gift.name", "<gold>Create Gift");
        config.set("gui.main.create_gift.lore", "<yellow>Create a special gift for\n<yellow>Women's Day celebration!\n\n<white>Click to start crafting");
        config.set("gui.main.info.name", "<light_purple>Women's Day Info");
        config.set("gui.main.info.lore", "<yellow>International Women's Day\n<yellow>is celebrated annually on\n<yellow>March 8th to commemorate\n<yellow>women's achievements and raise\n<yellow>awareness about equality.\n\n<white>Celebrate by sending gifts!");
        config.set("gui.main.my_gifts.name", "<aqua>My Gifts");
        config.set("gui.main.my_gifts.lore", "<yellow>View gifts you've sent\n<yellow>and received\n\n<white>Click to view");

        // Creation Menu Items
        config.set("gui.create.slot.name", "<yellow>Drop Item Here");
        config.set("gui.create.slot.lore", "<gray>Drag and drop items from\n<gray>your inventory to add them");
        config.set("gui.create.name.name", "<gold>Set Gift Name");
        config.set("gui.create.name.lore", "<yellow>Current: {0}\n\n<white>Click to change");
        config.set("gui.create.name.not_set", "Not set");
        config.set("gui.create.recipient.name", "<gold>Select Recipient");
        config.set("gui.create.recipient.lore", "<yellow>Current: {0}\n\n<white>Click to choose recipient");
        config.set("gui.create.recipient.not_selected", "Not selected");
        config.set("gui.create.description.name", "<gold>Set Description");
        config.set("gui.create.description.lore", "<yellow>Current: {0}\n\n<white>Click to change");
        config.set("gui.create.description.not_set", "Not set");
        config.set("gui.create.finalize.name", "<green>Finalize Gift");
        config.set("gui.create.finalize.lore", "<yellow>Complete your gift and\n<yellow>send it to the recipient\n\n<white>Click to send gift");

        // Recipient Selection Items
        config.set("gui.recipient.player.name", "<green>{0}");
        config.set("gui.recipient.player.lore", "<yellow>Click to select as recipient");
        config.set("gui.recipient.back.name", "<red>Back");
        config.set("gui.recipient.back.lore", "<yellow>Return to gift creation");
        config.set("gui.recipient.prev.name", "<yellow>Previous Page");
        config.set("gui.recipient.next.name", "<yellow>Next Page");

        // My Gifts Menu Items
        config.set("gui.mygifts.info.name", "<gold>My Gifts");
        config.set("gui.mygifts.info.lore", "<yellow>This feature is coming soon!\n\n<gray>Check back later to see your\n<gray>sent and received gifts.");
        config.set("gui.mygifts.back.name", "<red>Back");
        config.set("gui.mygifts.back.lore", "<yellow>Return to main menu");

        // Gift Item
        config.set("item.gift.name", "<light_purple>✿ Gift: {0} ✿");
        config.set("item.gift.lore.from", "<yellow>From: <white>{0}");
        config.set("item.gift.lore.message", "<yellow>Message: <white>{0}");
        config.set("item.gift.lore.instructions", "<gray>Right-click to open");

        // Entities
        config.set("entity.gift_bee.name", "<light_purple>✿ Gift Delivery from {0} ✿");

        // GUI Navigation Messages
        config.set("message.create_gift_open", "<green>Opening gift creation menu...");
        config.set("message.my_gifts_open", "<green>Opening my gifts menu...");
        config.set("message.recipient_open", "<green>Opening recipient selection menu...");
        config.set("message.return_to_create", "<green>Returning to gift creation...");
        config.set("message.return_to_main", "<green>Returning to main menu...");

        // Gift Creation Messages
        config.set("message.item_added", "<green>Added item to gift!");
        config.set("message.item_removed", "<yellow>Removed item from gift.");
        config.set("message.set_name", "<light_purple>Please type a name for your gift in the chat:");
        config.set("message.name_set", "<green>Gift name has been set!");
        config.set("message.set_description", "<light_purple>Please type a description for your gift in the chat:");
        config.set("message.description_set", "<green>Gift description has been set!");
        config.set("message.recipient_selected", "<green>Selected {0} as recipient!");

        // Gift Delivery Messages
        config.set("message.gift_sent", "<green>Your gift has been sent and will be delivered by bees!");
        config.set("message.gift_incomplete", "<red>Please complete all gift details first!");
        config.set("message.gift_invalid", "<red>Gift is invalid. Make sure it has items, a name, and a recipient.");
        config.set("message.recipient_offline", "<red>The recipient is offline. Your gift will be delivered when they log in.");
        config.set("message.gift_will_deliver_later", "<green>Your gift will be delivered to {0} when they come online.");
        config.set("message.gift_sent_announcement", "<light_purple>✿ <green>{0} <light_purple>sent a special gift to <green>{1}<light_purple>! ✿");
        config.set("message.gift_delivered", "<light_purple>✿ <white>You received a gift '<green>{1}<white>' from <green>{0}<light_purple>! ✿");
        config.set("message.gift_received", "<light_purple>✿ <white>You received a gift '<green>{1}<white>' from <green>{0}<light_purple>! ✿");
        config.set("message.gift_message", "<yellow>Message: <white>{0}");
        config.set("message.gift_opened", "<green>You opened the gift '<gold>{0}<green>' from <gold>{1}<green>!");
        config.set("message.pending_gifts", "<light_purple>✿ <white>You have <green>{0} <white>pending gifts! <light_purple>✿");
        config.set("message.gift_in_progress", "<light_purple>✿ <white>You had a gift in progress! Opening the creation menu... <light_purple>✿");

        // Command Messages
        config.set("command.gift.create", "<light_purple>✿ <green>Creating a new Women's Day gift! Add items, set a name and description, and choose a recipient. <light_purple>✿");
        config.set("command.gift.send", "<light_purple>✿ <green>Creating a gift for {0}! Add items, set a name and description. <light_purple>✿");

        // Help Command
        config.set("command.gift.help.header", "<gold>⋆｡˚ ☁︎ ˚｡⋆｡˚☽˚｡⋆ <light_purple>Women's Day Gift System <gold>⋆｡˚ ☁︎ ˚｡⋆｡˚☽˚｡⋆");

        List<String> helpCommands = new ArrayList<>();
        helpCommands.add("<yellow>/gift <white>- Open the main gift menu");
        helpCommands.add("<yellow>/gift create <white>- Create a new gift");
        helpCommands.add("<yellow>/gift send <player> <white>- Create a gift for a specific player");
        helpCommands.add("<yellow>/gift view <white>- View your sent and received gifts");
        helpCommands.add("<yellow>/gift help <white>- Show this help message");

        config.set("command.gift.help.commands", helpCommands);
        config.set("command.gift.help.footer", "<gold>⋆｡˚ ☁︎ ˚｡⋆｡˚☽˚｡⋆⋆｡˚ ☁︎ ˚｡⋆｡˚☽˚｡⋆");
    }

    /**
     * Get translated text component
     */
    public Component getText(String key) {
        return getText(key, defaultLanguage);
    }

    /**
     * Get translated text component for a specific language
     */
    public Component getText(String key, String lang) {
        YamlConfiguration config = languages.getOrDefault(lang, languages.get(defaultLanguage));
        String text = config.getString(key, "Missing text: " + key);
        return miniMessage.deserialize(text);
    }

    /**
     * Get translated text with placeholders for a specific language
     */
    public Component getTextWithPlaceholders(String key, String... placeholders) {
        YamlConfiguration config = languages.getOrDefault(defaultLanguage, languages.get(defaultLanguage));
        String text = config.getString(key, "Missing text: " + key);

        // Replace placeholders {0}, {1}, etc. with values
        for (int i = 0; i < placeholders.length; i++) {
            // Make sure to escape any MiniMessage formatting in the placeholder value
            String value = placeholders[i];
            // Escape any MiniMessage tags in the value, if needed
            // This is important if the placeholder might contain < or > characters
            value = value.replace("<", "\\<").replace(">", "\\>");
            text = text.replace("{" + i + "}", value);
        }

        return miniMessage.deserialize(text);
    }

    /**
     * Get a list of translated text components
     */
    public List<Component> getTextList(String key) {
        return getTextList(key, defaultLanguage);
    }

    /**
     * Get a list of translated text components for a specific language
     */
    public List<Component> getTextList(String key, String lang) {
        YamlConfiguration config = languages.getOrDefault(lang, languages.get(defaultLanguage));
        List<String> textList = config.getStringList(key);
        List<Component> components = new ArrayList<>();

        for (String text : textList) {
            components.add(miniMessage.deserialize(text));
        }

        return components;
    }

    /**
     * Reload all language files
     */
    public void reload() {
        loadConfig();
        languages.clear();
        loadLanguages();
    }
}