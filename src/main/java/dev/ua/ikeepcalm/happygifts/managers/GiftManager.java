package dev.ua.ikeepcalm.happygifts.managers;

import dev.ua.ikeepcalm.happygifts.HappyGifts;
import dev.ua.ikeepcalm.happygifts.gifts.Gift;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages all gift-related operations
 * Includes persistence for active gifts and gift history
 */
public class GiftManager implements Listener {
    private final HappyGifts plugin;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Active gifts being created
    @Getter
    private final Map<UUID, Gift> activeGifts = new HashMap<>();

    // Stored gifts waiting for delivery and history
    @Getter
    private final Map<UUID, Gift> storedGifts = new HashMap<>();

    // Player input states
    private final Set<UUID> playersInNameInput = new HashSet<>();
    private final Set<UUID> playersInDescriptionInput = new HashSet<>();

    // Config file for gifts
    private File giftsFile;
    private FileConfiguration giftsConfig;
    private final int storageTime;

    public GiftManager(HappyGifts plugin) {
        this.plugin = plugin;
        this.storageTime = plugin.getConfig().getInt("gifts.storage-time", 30);

        // Setup gifts file
        setupGiftsFile();

        // Load stored gifts
        loadStoredGifts();

        // Load active gifts
        loadActiveGifts();

        // Register events
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Schedule periodic saving to prevent data loss
        new BukkitRunnable() {
            @Override
            public void run() {
                saveAllGifts();
                saveActiveGifts();
                if (storageTime > 0) {
                    cleanupOldGifts();
                }
            }
        }.runTaskTimer(plugin, 6000L, 6000L); // Save every 5 minutes
    }

    /**
     * Clean up old gifts that are beyond the storage time
     */
    private void cleanupOldGifts() {
        if (storageTime <= 0) return; // Keep forever if set to -1

        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(storageTime);
        Iterator<Map.Entry<UUID, Gift>> iterator = storedGifts.entrySet().iterator();

        while (iterator.hasNext()) {
            Gift gift = iterator.next().getValue();
            if (gift.isOpened() && gift.getOpenedDate() != null && gift.getOpenedDate().isBefore(cutoffDate)) {
                iterator.remove();
            }
        }
    }

    /**
     * Set up the gifts file
     */
    private void setupGiftsFile() {
        // Create data folder if it doesn't exist
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        giftsFile = new File(plugin.getDataFolder(), "gifts.yml");

        if (!giftsFile.exists()) {
            try {
                giftsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create gifts.yml!");
                plugin.log(e.getMessage());
            }
        }

        giftsConfig = YamlConfiguration.loadConfiguration(giftsFile);
    }

    /**
     * Load stored gifts from file with enhanced history data
     */
    @SuppressWarnings("unchecked")
    private void loadStoredGifts() {
        storedGifts.clear();

        ConfigurationSection giftsSection = giftsConfig.getConfigurationSection("gifts");

        if (giftsSection != null) {
            for (String giftIdStr : giftsSection.getKeys(false)) {
                try {
                    UUID giftId = UUID.fromString(giftIdStr);
                    ConfigurationSection giftSection = giftsSection.getConfigurationSection(giftIdStr);

                    if (giftSection != null) {
                        UUID sender = UUID.fromString(giftSection.getString("sender"));
                        UUID recipient = UUID.fromString(giftSection.getString("recipient"));
                        String name = giftSection.getString("name");
                        String description = giftSection.getString("description");
                        boolean delivered = giftSection.getBoolean("delivered", false);
                        boolean opened = giftSection.getBoolean("opened", false);
                        List<ItemStack> items = (List<ItemStack>) giftSection.getList("items", new ArrayList<>());

                        // Parse dates
                        LocalDateTime creationDate = null;
                        LocalDateTime deliveryDate = null;
                        LocalDateTime openedDate = null;

                        if (giftSection.contains("creationDate")) {
                            creationDate = LocalDateTime.parse(giftSection.getString("creationDate"), DATE_FORMAT);
                        }

                        if (giftSection.contains("deliveryDate")) {
                            deliveryDate = LocalDateTime.parse(giftSection.getString("deliveryDate"), DATE_FORMAT);
                        }

                        if (giftSection.contains("openedDate")) {
                            openedDate = LocalDateTime.parse(giftSection.getString("openedDate"), DATE_FORMAT);
                        }

                        Gift gift = new Gift(giftId, sender, recipient, name, description, items,
                                delivered, creationDate, deliveryDate, openedDate, opened);
                        storedGifts.put(giftId, gift);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load gift: " + giftIdStr);
                    plugin.log(e.getMessage());
                }
            }
        }

        plugin.getLogger().info("Loaded " + storedGifts.size() + " stored gifts.");
    }

    /**
     * Load active gifts from file
     */
    @SuppressWarnings("unchecked")
    private void loadActiveGifts() {
        activeGifts.clear();

        ConfigurationSection activeGiftsSection = giftsConfig.getConfigurationSection("activegifts");

        if (activeGiftsSection != null) {
            for (String playerIdStr : activeGiftsSection.getKeys(false)) {
                try {
                    UUID playerId = UUID.fromString(playerIdStr);
                    ConfigurationSection giftSection = activeGiftsSection.getConfigurationSection(playerIdStr);

                    if (giftSection != null) {
                        UUID giftId = UUID.fromString(giftSection.getString("giftId"));
                        UUID sender = UUID.fromString(giftSection.getString("sender"));
                        String name = giftSection.getString("name");
                        String description = giftSection.getString("description");

                        UUID recipient = null;
                        if (giftSection.isString("recipient")) {
                            recipient = UUID.fromString(giftSection.getString("recipient"));
                        }

                        List<ItemStack> items = (List<ItemStack>) giftSection.getList("items", new ArrayList<>());

                        // Parse creation date
                        LocalDateTime creationDate = null;
                        if (giftSection.contains("creationDate")) {
                            creationDate = LocalDateTime.parse(giftSection.getString("creationDate"), DATE_FORMAT);
                        } else {
                            creationDate = LocalDateTime.now();
                        }

                        // Create the gift
                        Gift gift = new Gift(giftId, sender, recipient, name, description, items,
                                false, creationDate, null, null, false);

                        activeGifts.put(playerId, gift);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load active gift for player: " + playerIdStr);
                    plugin.log(e.getMessage());
                }
            }
        }

        plugin.getLogger().info("Loaded " + activeGifts.size() + " active gifts.");
    }

    /**
     * Save all gifts to file with enhanced history data
     */
    public void saveAllGifts() {
        // Clear existing gifts section
        giftsConfig.set("gifts", null);

        // Create section for gifts
        ConfigurationSection giftsSection = giftsConfig.createSection("gifts");

        // Save each gift
        for (Map.Entry<UUID, Gift> entry : storedGifts.entrySet()) {
            UUID giftId = entry.getKey();
            Gift gift = entry.getValue();

            ConfigurationSection giftSection = giftsSection.createSection(giftId.toString());
            giftSection.set("sender", gift.getSender().toString());
            giftSection.set("recipient", gift.getRecipient().toString());
            giftSection.set("name", gift.getName());
            giftSection.set("description", gift.getDescription());
            giftSection.set("delivered", gift.isDelivered());
            giftSection.set("opened", gift.isOpened());
            giftSection.set("items", gift.getItems());

            // Save dates
            if (gift.getCreationDate() != null) {
                giftSection.set("creationDate", gift.getCreationDate().format(DATE_FORMAT));
            }

            if (gift.getDeliveryDate() != null) {
                giftSection.set("deliveryDate", gift.getDeliveryDate().format(DATE_FORMAT));
            }

            if (gift.getOpenedDate() != null) {
                giftSection.set("openedDate", gift.getOpenedDate().format(DATE_FORMAT));
            }
        }

        // Save to file
        try {
            giftsConfig.save(giftsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save gifts.yml!");
            plugin.log(e.getMessage());
        }
    }

    /**
     * Save active gifts to file with creation date
     */
    public void saveActiveGifts() {
        // Clear existing active gifts section
        giftsConfig.set("activegifts", null);

        // Create section for active gifts
        ConfigurationSection activeGiftsSection = giftsConfig.createSection("activegifts");

        // Save each active gift
        for (Map.Entry<UUID, Gift> entry : activeGifts.entrySet()) {
            UUID playerId = entry.getKey();
            Gift gift = entry.getValue();

            ConfigurationSection giftSection = activeGiftsSection.createSection(playerId.toString());
            giftSection.set("giftId", gift.getGiftId().toString());
            giftSection.set("sender", gift.getSender().toString());
            if (gift.getRecipient() != null) {
                giftSection.set("recipient", gift.getRecipient().toString());
            }
            giftSection.set("name", gift.getName());
            giftSection.set("description", gift.getDescription());
            giftSection.set("items", gift.getItems());

            // Save creation date
            if (gift.getCreationDate() != null) {
                giftSection.set("creationDate", gift.getCreationDate().format(DATE_FORMAT));
            }
        }

        // Save to file
        try {
            giftsConfig.save(giftsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save active gifts to gifts.yml!");
            plugin.log(e.getMessage());
        }
    }

    /**
     * Get the active gift a player is creating
     */
    public Gift getActiveGift(UUID playerUuid) {
        return activeGifts.get(playerUuid);
    }

    /**
     * Set a player's active gift
     */
    public void setActiveGift(UUID playerUuid, Gift gift) {
        activeGifts.put(playerUuid, gift);
        saveActiveGifts(); // Save whenever a gift is modified
    }

    /**
     * Store a gift for later delivery or tracking
     */
    public void storeGift(Gift gift) {
        if (gift != null && gift.isValid()) {
            storedGifts.put(gift.getGiftId(), gift);
            saveAllGifts();
        }
    }

    /**
     * Update a stored gift
     */
    public void updateStoredGift(Gift gift) {
        if (gift != null) {
            storedGifts.put(gift.getGiftId(), gift);
            saveAllGifts();
        }
    }

    /**
     * Get a stored gift by its ID
     */
    public Gift getStoredGift(UUID giftId) {
        return storedGifts.get(giftId);
    }

    /**
     * Get all gifts for a specific recipient that haven't been delivered yet
     */
    public List<Gift> getStoredGiftsForPlayer(UUID recipientId) {
        List<Gift> result = new ArrayList<>();

        for (Gift gift : storedGifts.values()) {
            if (gift.getRecipient().equals(recipientId) && !gift.isDelivered()) {
                result.add(gift);
            }
        }

        return result;
    }

    /**
     * Get all gifts sent by a player
     */
    public List<Gift> getSentGifts(UUID playerUuid) {
        return storedGifts.values().stream()
                .filter(gift -> gift.getSender().equals(playerUuid))
                .sorted(Comparator.comparing(Gift::getCreationDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get all gifts received by a player
     */
    public List<Gift> getReceivedGifts(UUID playerUuid) {
        return storedGifts.values().stream()
                .filter(gift -> gift.getRecipient().equals(playerUuid))
                .sorted(Comparator.comparing(Gift::getCreationDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Get a page of gifts (for pagination)
     */
    public List<Gift> getGiftsPage(List<Gift> gifts, int page, int itemsPerPage) {
        int start = page * itemsPerPage;
        int end = Math.min(start + itemsPerPage, gifts.size());

        if (start >= gifts.size()) {
            return new ArrayList<>();
        }

        return gifts.subList(start, end);
    }

    /**
     * Set whether a player is in name input mode
     */
    public void setPlayerInNameInput(UUID playerUuid, boolean inNameInput) {
        if (inNameInput) {
            playersInNameInput.add(playerUuid);
        } else {
            playersInNameInput.remove(playerUuid);
        }
    }

    /**
     * Check if a player is in name input mode
     */
    public boolean isPlayerInNameInput(UUID playerUuid) {
        return playersInNameInput.contains(playerUuid);
    }

    /**
     * Set whether a player is in description input mode
     */
    public void setPlayerInDescriptionInput(UUID playerUuid, boolean inDescriptionInput) {
        if (inDescriptionInput) {
            playersInDescriptionInput.add(playerUuid);
        } else {
            playersInDescriptionInput.remove(playerUuid);
        }
    }

    /**
     * Check if a player is in description input mode
     */
    public boolean isPlayerInDescriptionInput(UUID playerUuid) {
        return playersInDescriptionInput.contains(playerUuid);
    }

    /**
     * Finalize a gift and prepare for delivery
     */
    public void finalizeGift(Player player) {
        UUID playerId = player.getUniqueId();
        Gift gift = activeGifts.get(playerId);

        if (gift != null && gift.isValid()) {
            // Clear active gift
            activeGifts.remove(playerId);
            saveActiveGifts(); // Save the active gifts state

            // Store the gift for tracking
            storeGift(gift);

            // Deliver the gift
            plugin.getDeliverySystem().deliverGift(gift);
        } else {
            player.sendMessage(plugin.getLanguageManager().getText("message.gift_incomplete"));
        }
    }
}