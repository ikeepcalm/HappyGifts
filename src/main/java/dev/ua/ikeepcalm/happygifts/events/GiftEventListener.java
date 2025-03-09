package dev.ua.ikeepcalm.happygifts.events;

import dev.ua.ikeepcalm.happygifts.HappyGifts;
import dev.ua.ikeepcalm.happygifts.gifts.Gift;
import dev.ua.ikeepcalm.happygifts.gifts.guis.GiftGui;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Handles all gift-related events
 */
public class GiftEventListener implements Listener {

    private final HappyGifts plugin;
    private final NamespacedKey giftKey;

    public GiftEventListener(HappyGifts plugin) {
        this.plugin = plugin;
        this.giftKey = new NamespacedKey(plugin, "gift_id");
    }

    /**
     * Process a gift's name or description from chat input
     */
    public void handleChatInput(Player player, Component messageComponent) {
        UUID playerUuid = player.getUniqueId();
        Gift gift = plugin.getGiftManager().getActiveGift(playerUuid);

        String message = LegacyComponentSerializer.legacyAmpersand().serialize(messageComponent);

        if (gift == null) {
            return;
        }

        if (plugin.getGiftManager().isPlayerInNameInput(playerUuid)) {
            gift.setName(message);
            plugin.getGiftManager().setPlayerInNameInput(playerUuid, false);
            player.sendMessage(plugin.getLanguageManager().getText("message.name_set"));
            plugin.getGiftManager().saveActiveGifts();
            new GiftGui(plugin).openGiftCreationMenu(player);
        } else if (plugin.getGiftManager().isPlayerInDescriptionInput(playerUuid)) {
            gift.setDescription(message);
            plugin.getGiftManager().setPlayerInDescriptionInput(playerUuid, false);
            player.sendMessage(plugin.getLanguageManager().getText("message.description_set"));
            plugin.getGiftManager().saveActiveGifts();
            new GiftGui(plugin).openGiftCreationMenu(player);
        }
    }


    /**
     * Handle chat messages for gift input
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // Check if player is in gift input mode
        if (plugin.getGiftManager().isPlayerInNameInput(player.getUniqueId()) || plugin.getGiftManager().isPlayerInDescriptionInput(player.getUniqueId())) {
            // Cancel the chat event to prevent others from seeing the input
            event.setCancelled(true);

            // Process the input on the main thread
            Bukkit.getScheduler().runTask(plugin, () -> handleChatInput(player, event.message()));
        }
    }

    /**
     * Handle opening gift items
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.hasItem() && event.getItem() != null) {
            ItemStack item = event.getItem();

            if (item.getItemMeta() != null &&
                    item.getItemMeta().getPersistentDataContainer().has(giftKey, PersistentDataType.STRING)) {

                event.setCancelled(true);

                String giftIdStr = item.getItemMeta().getPersistentDataContainer()
                        .get(giftKey, PersistentDataType.STRING);

                if (giftIdStr != null) {
                    try {
                        UUID giftId = UUID.fromString(giftIdStr);
                        Gift gift = plugin.getGiftManager().getStoredGift(giftId);

                        if (gift != null) {
                            // Remove the gift item from player's hand
                            Player player = event.getPlayer();
                            player.getInventory().setItemInMainHand(null);

                            // Process gift opening
                            plugin.getDeliverySystem().processGiftOpening(player, gift);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to process gift: " + giftIdStr);
                        plugin.log(e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Check for pending gifts and active gifts when a player joins
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        // Check for pending gifts
        Bukkit.getScheduler().runTaskLater(plugin, () -> plugin.getDeliverySystem().checkPendingGifts(player), 40L);

        // Check if player had an active gift in progress
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getGiftManager().getActiveGifts().containsKey(playerId)) {
                // Notify player they had a gift in progress
                player.sendMessage(plugin.getLanguageManager().getText("message.gift_in_progress"));

                // Open the gift creation menu
                new GiftGui(plugin).openGiftCreationMenu(player);
            }
        }, 60L);
    }

    /**
     * Clean up active states when a player leaves
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getGiftManager().setPlayerInNameInput(player.getUniqueId(), false);
        plugin.getGiftManager().setPlayerInDescriptionInput(player.getUniqueId(), false);
    }
}