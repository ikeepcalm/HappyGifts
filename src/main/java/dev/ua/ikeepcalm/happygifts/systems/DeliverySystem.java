package dev.ua.ikeepcalm.happygifts.systems;

import dev.ua.ikeepcalm.happygifts.HappyGifts;
import dev.ua.ikeepcalm.happygifts.gifts.Gift;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.entity.Bee;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles the delivery of gifts to players
 */
public class DeliverySystem {
    private final HappyGifts plugin;
    private final NamespacedKey giftIdKey;

    public DeliverySystem(HappyGifts plugin) {
        this.plugin = plugin;
        this.giftIdKey = new NamespacedKey(plugin, "gift_id");
    }

    /**
     * Deliver a gift to a player
     */
    public void deliverGift(Gift gift) {
        plugin.getGiftManager().storeGift(gift);

        Player recipient = Bukkit.getPlayer(gift.getRecipient());
        String senderName = Bukkit.getOfflinePlayer(gift.getSender()).getName();
        if (senderName == null) senderName = "Someone";

        if (recipient != null && recipient.isOnline()) {
            boolean useBees = plugin.getConfig().getBoolean("gifts.delivery.use-bees", true);
            int deliveryDelay = plugin.getConfig().getInt("gifts.delivery.delay", 10);

            if (useBees) {
                deliverWithBee(gift, recipient, senderName, deliveryDelay);
            } else {
                directDelivery(gift, recipient, senderName);
            }
        } else {
            Player sender = Bukkit.getPlayer(gift.getSender());
            if (sender != null && sender.isOnline()) {
                sender.sendMessage(plugin.getLanguageManager().getTextWithPlaceholders(
                        "message.gift_will_deliver_later",
                        Bukkit.getOfflinePlayer(gift.getRecipient()).getName()));
            }
        }
    }

    /**
     * Deliver a gift using a bee animation
     */
    private void deliverWithBee(Gift gift, Player recipient, String senderName, int deliveryDelaySeconds) {
        Location spawnLocation = recipient.getLocation().clone().add(0, 15, 0);
        Bee bee = (Bee) recipient.getWorld().spawnEntity(spawnLocation, EntityType.BEE);
        bee.customName(plugin.getLanguageManager().getTextWithPlaceholders("entity.gift_bee.name", senderName));
        bee.setCustomNameVisible(true);
        bee.setPersistent(true);
        bee.setRemoveWhenFarAway(false);
        bee.setAnger(0);
        bee.setCannotEnterHiveTicks(Integer.MAX_VALUE);
        PersistentDataContainer container = bee.getPersistentDataContainer();
        container.set(giftIdKey, PersistentDataType.STRING, gift.getGiftId().toString());
        new BukkitRunnable() {
            private int ticksRun = 0;
            private final int maxTicks = deliveryDelaySeconds * 20;

            @Override
            public void run() {
                if (!bee.isValid() || !recipient.isOnline()) {
                    cancel();
                    if (bee.isValid()) bee.remove();
                    return;
                }

                ticksRun++;

                Location targetLoc = recipient.getLocation().clone().add(0, 1.5, 0);
                Vector direction = targetLoc.toVector().subtract(bee.getLocation().toVector());

                if (direction.length() > 0.5) {
                    direction.normalize().multiply(0.2);
                    bee.setVelocity(direction);
                } else if (ticksRun >= maxTicks || direction.length() <= 0.5) {
                    recipient.getWorld().playSound(recipient.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

                    recipient.sendMessage(plugin.getLanguageManager().getTextWithPlaceholders(
                            "message.gift_delivered", senderName, gift.getName()));

                    // Mark as delivered
                    gift.markDelivered();
                    plugin.getGiftManager().updateStoredGift(gift);

                    ItemStack giftItem = createGiftItem(gift);

                    if (recipient.getInventory().firstEmpty() != -1) {
                        recipient.getInventory().addItem(giftItem);
                    } else {
                        recipient.getWorld().dropItem(recipient.getLocation(), giftItem);
                    }

                    bee.remove();

                    // Announce if enabled
                    if (plugin.getConfig().getBoolean("gifts.public-announcements", true)) {
                        Bukkit.getServer().sendMessage(plugin.getLanguageManager().getTextWithPlaceholders(
                                "message.gift_sent_announcement", senderName, recipient.getName()));
                    }

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    /**
     * Deliver a gift directly to a player
     */
    private void directDelivery(Gift gift, Player recipient, String senderName) {
        ItemStack giftItem = createGiftItem(gift);

        if (recipient.getInventory().firstEmpty() != -1) {
            recipient.getInventory().addItem(giftItem);
        } else {
            recipient.getWorld().dropItem(recipient.getLocation(), giftItem);
        }

        recipient.sendMessage(plugin.getLanguageManager().getTextWithPlaceholders(
                "message.gift_delivered", senderName, gift.getName()));

        recipient.playSound(recipient.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);

        // Mark as delivered
        gift.markDelivered();
        plugin.getGiftManager().updateStoredGift(gift);

        // Announce if enabled
        if (plugin.getConfig().getBoolean("gifts.public-announcements", true)) {
            Bukkit.getServer().sendMessage(plugin.getLanguageManager().getTextWithPlaceholders(
                    "message.gift_sent_announcement", senderName, recipient.getName()));
        }
    }

    /**
     * Create a gift item that can be opened by the player
     */
    private ItemStack createGiftItem(Gift gift) {
        ItemStack giftItem = new ItemStack(Material.CHEST);
        ItemMeta meta = giftItem.getItemMeta();

        if (meta != null) {
            Component name = plugin.getLanguageManager().getTextWithPlaceholders(
                    "item.gift.name", gift.getName());
            meta.displayName(name);

            List<Component> lore = new ArrayList<>();

            String senderName = Bukkit.getOfflinePlayer(gift.getSender()).getName();
            if (senderName == null) senderName = "Someone";
            lore.add(plugin.getLanguageManager().getTextWithPlaceholders(
                    "item.gift.lore.from", senderName));

            if (gift.getDescription() != null && !gift.getDescription().isEmpty()) {
                lore.add(plugin.getLanguageManager().getTextWithPlaceholders(
                        "item.gift.lore.message", gift.getDescription()));
            }

            lore.add(plugin.getLanguageManager().getText("item.gift.lore.instructions"));

            meta.lore(lore);
            meta.getPersistentDataContainer().set(
                    giftIdKey, PersistentDataType.STRING, gift.getGiftId().toString());

            giftItem.setItemMeta(meta);
        }

        return giftItem;
    }

    /**
     * Check for pending gifts when a player logs in
     */
    public void checkPendingGifts(Player player) {
        List<Gift> pendingGifts = plugin.getGiftManager().getStoredGiftsForPlayer(player.getUniqueId());

        if (!pendingGifts.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().getTextWithPlaceholders(
                    "message.pending_gifts", String.valueOf(pendingGifts.size())));

            for (Gift gift : pendingGifts) {
                int index = pendingGifts.indexOf(gift);
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    String senderName = Bukkit.getOfflinePlayer(gift.getSender()).getName();
                    if (senderName == null) senderName = "Someone";

                    boolean useBees = plugin.getConfig().getBoolean("gifts.delivery.use-bees", true);
                    if (useBees) {
                        deliverWithBee(gift, player, senderName, 5);
                    } else {
                        directDelivery(gift, player, senderName);
                    }
                }, 20L * (index + 1));
            }
        }
    }

    /**
     * Process when a player opens a gift
     */
    public void processGiftOpening(Player player, Gift gift) {
        if (gift != null) {
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);

            String senderName = Bukkit.getOfflinePlayer(gift.getSender()).getName();
            if (senderName == null) senderName = "Someone";

            player.sendMessage(plugin.getLanguageManager().getTextWithPlaceholders(
                    "message.gift_opened", gift.getName(), senderName));

            if (gift.getDescription() != null && !gift.getDescription().isEmpty()) {
                player.sendMessage(plugin.getLanguageManager().getTextWithPlaceholders(
                        "message.gift_message", gift.getDescription()));
            }

            // Add gift items to inventory
            for (ItemStack item : gift.getItems()) {
                if (item != null) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItem(player.getLocation(), item);
                    }
                }
            }

            // Play particles if enabled
            if (plugin.getConfig().getBoolean("gifts.delivery.particles", true)) {
                playGiftOpenParticles(player.getLocation());
            }

            // Mark as opened and update
            gift.markOpened();
            plugin.getGiftManager().updateStoredGift(gift);
        }
    }

    /**
     * Play particles when a gift is opened
     */
    private void playGiftOpenParticles(Location location) {
        location.getWorld().spawnParticle(Particle.HEART, location, 15, 0.5, 0.5, 0.5, 0.1);
        location.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, location, 20, 0.5, 0.5, 0.5, 0.1);

        // Play particles over 2 seconds
        new BukkitRunnable() {
            private int count = 0;
            @Override
            public void run() {
                if (count++ >= 4) {
                    cancel();
                    return;
                }
                location.getWorld().spawnParticle(Particle.HEART, location, 5, 0.5, 0.5, 0.5, 0.1);
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }
}