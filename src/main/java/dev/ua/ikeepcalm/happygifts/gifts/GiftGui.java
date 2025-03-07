package dev.ua.ikeepcalm.happygifts.gifts;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.ua.ikeepcalm.happygifts.HappyGifts;
import dev.ua.ikeepcalm.happygifts.utils.AdventureAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class that manages all gift-related GUIs
 * Uses Adventure API for text components
 */
public class GiftGui {
    private final HappyGifts plugin;

    public GiftGui(HappyGifts plugin) {
        this.plugin = plugin;
    }

    /**
     * Helper method to convert Component to legacy string for GUI titles only
     * This is needed because ChestGui doesn't support Adventure Components for titles
     */
    private String getTitleString(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * Opens the main gift menu
     */
    public void openMainMenu(Player player) {
        String title = getTitleString(plugin.getLanguageManager().getText("gui.main.title"));
        ChestGui gui = new ChestGui(5, title);

        // Create border pane (MUST BE ADDED FIRST)
        OutlinePane borderPane = new OutlinePane(0, 0, 9, 5);
        borderPane.setRepeat(true);
        borderPane.setPriority(Pane.Priority.LOWEST);

        ItemStack borderItem = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        Component borderName = plugin.getLanguageManager().getText("gui.border.name");
        AdventureAdapter.setDisplayName(borderItem, borderName);

        borderPane.addItem(new GuiItem(borderItem, event -> event.setCancelled(true)));
        gui.addPane(borderPane);

        // Create content pane with higher priority
        StaticPane contentPane = new StaticPane(0, 0, 9, 5);
        contentPane.setPriority(Pane.Priority.HIGH);

        // Create Gift button
        ItemStack createGiftItem = new ItemStack(Material.CHEST);
        Component createGiftName = plugin.getLanguageManager().getText("gui.main.create_gift.name");
        Component createGiftLore = plugin.getLanguageManager().getText("gui.main.create_gift.lore");

        GuiItem createGiftGuiItem = AdventureAdapter.createGuiItem(
                createGiftItem,
                createGiftName,
                createGiftLore,
                event -> {
                    event.setCancelled(true);
                    // Send message using Adventure API directly
                    player.sendMessage(plugin.getLanguageManager().getText("message.create_gift_open"));
                    openGiftCreationMenu(player);
                }
        );

        contentPane.addItem(createGiftGuiItem, 2, 2);

        // Info button
        ItemStack infoItem = new ItemStack(Material.PAPER);
        Component infoName = plugin.getLanguageManager().getText("gui.main.info.name");
        Component infoLore = plugin.getLanguageManager().getText("gui.main.info.lore");

        GuiItem infoGuiItem = AdventureAdapter.createGuiItem(
                infoItem,
                infoName,
                infoLore,
                event -> event.setCancelled(true)
        );

        contentPane.addItem(infoGuiItem, 4, 2);

        // My Gifts button
        ItemStack myGiftsItem = new ItemStack(Material.WRITABLE_BOOK);
        Component myGiftsName = Component.text("COMING SOON...").color(NamedTextColor.RED); // gui.main.my_gifts.name
        Component myGiftsLore = plugin.getLanguageManager().getText("gui.main.my_gifts.lore");

        GuiItem myGiftsGuiItem = AdventureAdapter.createGuiItem(
                myGiftsItem,
                myGiftsName,
                myGiftsLore,
                event -> {
                    event.setCancelled(true);
//                    player.sendMessage(plugin.getLanguageManager().getText("message.my_gifts_open"));
//                    openMyGiftsMenu(player);
                }
        );

        contentPane.addItem(myGiftsGuiItem, 6, 2);

        gui.addPane(contentPane);
        gui.show(player);
    }

    /**
     * Opens the gift creation menu
     * Fixed non-responding buttons by properly handling inventory clicks
     * Uses Adventure API directly for text components
     */
    public void openGiftCreationMenu(Player player) {
        Gift gift = plugin.getGiftManager().getActiveGift(player.getUniqueId());
        if (gift == null) {
            gift = new Gift(player.getUniqueId());
            plugin.getGiftManager().setActiveGift(player.getUniqueId(), gift);
        }
        final Gift finalGift = gift;

        String title = getTitleString(plugin.getLanguageManager().getText("gui.create.title"));
        ChestGui gui = new ChestGui(5, title);

        // Create border pane (MUST BE ADDED FIRST)
        OutlinePane borderPane = new OutlinePane(0, 0, 9, 5);
        borderPane.setRepeat(true);
        borderPane.setPriority(Pane.Priority.LOWEST);

        ItemStack borderItem = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        Component borderName = plugin.getLanguageManager().getText("gui.create_border.name");
        AdventureAdapter.setDisplayName(borderItem, borderName);

        borderPane.addItem(new GuiItem(borderItem, e -> e.setCancelled(true)));
        gui.addPane(borderPane);

        StaticPane itemsPane = new StaticPane(1, 1, 7, 1);
        itemsPane.setPriority(Pane.Priority.NORMAL);

        for (int i = 0; i < 7; i++) {
            ItemStack slotItem;

            if (finalGift.getItems().size() > i && finalGift.getItems().get(i) != null) {
                slotItem = finalGift.getItems().get(i).clone();
            } else {
                slotItem = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                Component slotName = plugin.getLanguageManager().getText("gui.create.slot.name");
                Component slotLore = plugin.getLanguageManager().getText("gui.create.slot.lore");

                AdventureAdapter.setDisplayName(slotItem, slotName);
                AdventureAdapter.setLore(slotItem, slotLore);
            }

            final int slotIndex = i;
            itemsPane.addItem(new GuiItem(slotItem, event -> {
                if (event.isLeftClick() && event.getCursor().getType() != Material.AIR) {

                    ItemStack itemToAdd = event.getCursor().clone();
                    event.getInventory().setItem(event.getRawSlot(), itemToAdd.clone());
                    finalGift.setItem(slotIndex, itemToAdd);
                    event.setCursor(null);

                    player.sendMessage(plugin.getLanguageManager().getText("message.item_added"));

                    event.setCancelled(true);

                    player.closeInventory();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            openGiftCreationMenu(player);
                        }
                    }.runTaskLater(plugin, 20);

                } else if (event.isRightClick() && finalGift.getItems().size() > slotIndex &&
                        finalGift.getItems().get(slotIndex) != null) {

                    ItemStack removed = finalGift.getItems().get(slotIndex).clone();

                    finalGift.removeItem(slotIndex);

                    ItemStack emptySlot = new ItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE);
                    Component slotName = plugin.getLanguageManager().getText("gui.create.slot.name");
                    Component slotLore = plugin.getLanguageManager().getText("gui.create.slot.lore");

                    AdventureAdapter.setDisplayName(emptySlot, slotName);
                    AdventureAdapter.setLore(emptySlot, slotLore);

                    event.getInventory().setItem(event.getRawSlot(), emptySlot);

                    player.sendMessage(plugin.getLanguageManager().getText("message.item_removed"));
                    player.getInventory().addItem(removed);

                    player.closeInventory();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            openGiftCreationMenu(player);
                        }
                    }.runTaskLater(plugin, 20);

                    event.setCancelled(true);
                } else {
                    event.setCancelled(true);
                }
            }), i, 0);
        }

        gui.addPane(itemsPane);

        StaticPane controlsPane = new StaticPane(0, 0, 9, 5);
        controlsPane.setPriority(Pane.Priority.HIGH);

        ItemStack nameItem = new ItemStack(Material.NAME_TAG);
        Component nameButtonText = plugin.getLanguageManager().getText("gui.create.name.name");

        String currentNameText;
        if (finalGift.getName() != null) {
            currentNameText = finalGift.getName();
        } else {
            currentNameText = getTitleString(plugin.getLanguageManager().getText("gui.create.name.not_set"));
        }

        Component nameLore = plugin.getLanguageManager().getTextWithPlaceholders("gui.create.name.lore", currentNameText);

        AdventureAdapter.setDisplayName(nameItem, nameButtonText);
        AdventureAdapter.setLore(nameItem, nameLore);

        controlsPane.addItem(new GuiItem(nameItem, event -> {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(plugin.getLanguageManager().getText("message.set_name"));
            plugin.getGiftManager().setPlayerInNameInput(player.getUniqueId(), true);
        }), 2, 2);

        ItemStack recipientItem = new ItemStack(Material.PLAYER_HEAD);
        Component recipientButtonText = plugin.getLanguageManager().getText("gui.create.recipient.name");

        String currentRecipientText;
        if (finalGift.getRecipient() != null) {
            currentRecipientText = Bukkit.getOfflinePlayer(finalGift.getRecipient()).getName();
        } else {
            currentRecipientText = getTitleString(plugin.getLanguageManager().getText("gui.create.recipient.not_selected"));
        }

        Component recipientLore = plugin.getLanguageManager()
                .getTextWithPlaceholders("gui.create.recipient.lore", currentRecipientText);

        AdventureAdapter.setDisplayName(recipientItem, recipientButtonText);
        AdventureAdapter.setLore(recipientItem, recipientLore);

        controlsPane.addItem(new GuiItem(recipientItem, event -> {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageManager().getText("message.recipient_open"));
            openRecipientSelectionMenu(player);
        }), 4, 2);

        ItemStack descriptionItem = new ItemStack(Material.WRITABLE_BOOK);
        Component descriptionButtonText = plugin.getLanguageManager().getText("gui.create.description.name");

        String currentDescriptionText;
        if (finalGift.getDescription() != null) {
            if (finalGift.getDescription().length() > 20) {
                currentDescriptionText = finalGift.getDescription().substring(0, 20) + "...";
            } else {
                currentDescriptionText = finalGift.getDescription();
            }
        } else {
            currentDescriptionText = getTitleString(plugin.getLanguageManager().getText("gui.create.description.not_set"));
        }

        Component descriptionLore = plugin.getLanguageManager()
                .getTextWithPlaceholders("gui.create.description.lore", currentDescriptionText);

        AdventureAdapter.setDisplayName(descriptionItem, descriptionButtonText);
        AdventureAdapter.setLore(descriptionItem, descriptionLore);

        controlsPane.addItem(new GuiItem(descriptionItem, event -> {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(plugin.getLanguageManager().getText("message.set_description"));
            plugin.getGiftManager().setPlayerInDescriptionInput(player.getUniqueId(), true);
        }), 6, 2);

        // Finalize Button
        ItemStack finalizeItem = new ItemStack(Material.EMERALD);
        Component finalizeButtonText = plugin.getLanguageManager().getText("gui.create.finalize.name");
        Component finalizeLore = plugin.getLanguageManager().getText("gui.create.finalize.lore");

        AdventureAdapter.setDisplayName(finalizeItem, finalizeButtonText);
        AdventureAdapter.setLore(finalizeItem, finalizeLore);

        controlsPane.addItem(new GuiItem(finalizeItem, event -> {
            event.setCancelled(true);
            if (finalGift.isValid()) {
                player.closeInventory();
                plugin.getGiftManager().finalizeGift(player);
                player.sendMessage(plugin.getLanguageManager().getText("message.gift_sent"));
            } else {
                player.sendMessage(plugin.getLanguageManager().getText("message.gift_incomplete"));
            }
        }), 4, 3);

        gui.addPane(controlsPane);
        gui.show(player);
    }

    /**
     * Opens the recipient selection menu
     * Uses Adventure API directly for text components
     */
    public void openRecipientSelectionMenu(Player player) {
        String title = getTitleString(plugin.getLanguageManager().getText("gui.recipient.title"));
        ChestGui gui = new ChestGui(5, title);

        // Create border pane (MUST BE ADDED FIRST)
        OutlinePane borderPane = new OutlinePane(0, 0, 9, 5);
        borderPane.setRepeat(true);
        borderPane.setPriority(Pane.Priority.LOWEST);

        ItemStack borderItem = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        Component emptyText = Component.text(" ");
        AdventureAdapter.setDisplayName(borderItem, emptyText);

        borderPane.addItem(new GuiItem(borderItem, e -> e.setCancelled(true)));
        gui.addPane(borderPane);

        PaginatedPane playerPane = new PaginatedPane(1, 1, 7, 3);
        playerPane.setPriority(Pane.Priority.NORMAL);

        List<GuiItem> playerHeads = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.getUniqueId().equals(player.getUniqueId())) continue;

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(online);
                meta.setPlayerProfile(player.getPlayerProfile());

                Component playerNameText = plugin.getLanguageManager()
                        .getTextWithPlaceholders("gui.recipient.player.name", online.getName()).decoration(TextDecoration.ITALIC, false);
                Component playerLore = plugin.getLanguageManager().getText("gui.recipient.player.lore").decoration(TextDecoration.ITALIC, false);

                meta.displayName(playerNameText);
                List<Component> loreComponents = new ArrayList<>();
                loreComponents.add(playerLore);
                meta.lore(loreComponents);
                head.setItemMeta(meta);
            }

            final UUID targetUuid = online.getUniqueId();
            playerHeads.add(new GuiItem(head, event -> {
                event.setCancelled(true);
                Gift gift = plugin.getGiftManager().getActiveGift(player.getUniqueId());
                if (gift != null) {
                    gift.setRecipient(targetUuid);
                    player.sendMessage(plugin.getLanguageManager()
                            .getTextWithPlaceholders("message.recipient_selected", online.getName()));
                }
                openGiftCreationMenu(player);
            }));
        }

        playerPane.populateWithGuiItems(playerHeads);
        gui.addPane(playerPane);

        StaticPane controlsPane = new StaticPane(0, 0, 9, 5);
        controlsPane.setPriority(Pane.Priority.HIGH);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        Component backText = plugin.getLanguageManager().getText("gui.recipient.back.name");
        Component backLore = plugin.getLanguageManager().getText("gui.recipient.back.lore");

        AdventureAdapter.setDisplayName(backItem, backText);
        AdventureAdapter.setLore(backItem, backLore);

        controlsPane.addItem(new GuiItem(backItem, event -> {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageManager().getText("message.return_to_create"));
            openGiftCreationMenu(player);
        }), 4, 4);

        if (playerHeads.size() > 21) {
            ItemStack prevItem = new ItemStack(Material.ARROW);
            Component prevText = plugin.getLanguageManager().getText("gui.recipient.prev.name");
            AdventureAdapter.setDisplayName(prevItem, prevText);

            controlsPane.addItem(new GuiItem(prevItem, event -> {
                event.setCancelled(true);
                if (playerPane.getPage() > 0) {
                    playerPane.setPage(playerPane.getPage() - 1);
                    gui.update();
                }
            }), 3, 4);

            ItemStack nextItem = new ItemStack(Material.ARROW);
            Component nextText = plugin.getLanguageManager().getText("gui.recipient.next.name");
            AdventureAdapter.setDisplayName(nextItem, nextText);

            controlsPane.addItem(new GuiItem(nextItem, event -> {
                event.setCancelled(true);
                int maxPages = (int) Math.ceil(playerHeads.size() / 21.0);
                if (playerPane.getPage() < maxPages - 1) {
                    playerPane.setPage(playerPane.getPage() + 1);
                    gui.update();
                }
            }), 5, 4);
        }

        gui.addPane(controlsPane);
        gui.show(player);
    }

    /**
     * Opens the my gifts menu
     */
    public void openMyGiftsMenu(Player player) {
        String title = AdventureAdapter.componentToLegacy(plugin.getLanguageManager().getText("gui.mygifts.title"));
        ChestGui gui = new ChestGui(5, title);

        // Create border pane (MUST BE ADDED FIRST)
        OutlinePane borderPane = new OutlinePane(0, 0, 9, 5);
        borderPane.setRepeat(true);
        borderPane.setPriority(Pane.Priority.LOWEST);

        ItemStack borderItem = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        ItemMeta borderMeta = borderItem.getItemMeta();
        if (borderMeta != null) {
            borderMeta.displayName(Component.text(" "));
            borderItem.setItemMeta(borderMeta);
        }

        borderPane.addItem(new GuiItem(borderItem, e -> e.setCancelled(true)));
        gui.addPane(borderPane);

        StaticPane contentPane = new StaticPane(0, 0, 9, 5);
        contentPane.setPriority(Pane.Priority.HIGH);

        ItemStack infoItem = new ItemStack(Material.PAPER);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(plugin.getLanguageManager().getText("gui.mygifts.info.name"));
            infoMeta.lore(plugin.getLanguageManager().getTextList("gui.mygifts.info.lore"));
            infoItem.setItemMeta(infoMeta);
        }

        contentPane.addItem(new GuiItem(infoItem, event -> event.setCancelled(true)), 4, 2);

        ItemStack backItem = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(plugin.getLanguageManager().getText("gui.mygifts.back.name"));
            backMeta.lore(plugin.getLanguageManager().getTextList("gui.mygifts.back.lore"));
            backItem.setItemMeta(backMeta);
        }

        contentPane.addItem(new GuiItem(backItem, event -> {
            event.setCancelled(true);
            player.sendMessage(plugin.getLanguageManager().getText("message.return_to_main"));
            openMainMenu(player);
        }), 4, 3);

        gui.addPane(contentPane);
        gui.show(player);
    }
}