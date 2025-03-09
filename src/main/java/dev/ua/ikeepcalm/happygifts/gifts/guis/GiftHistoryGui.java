package dev.ua.ikeepcalm.happygifts.gifts.guis;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import com.github.stefvanschie.inventoryframework.gui.type.ChestGui;
import com.github.stefvanschie.inventoryframework.pane.OutlinePane;
import com.github.stefvanschie.inventoryframework.pane.PaginatedPane;
import com.github.stefvanschie.inventoryframework.pane.Pane;
import com.github.stefvanschie.inventoryframework.pane.StaticPane;
import dev.ua.ikeepcalm.happygifts.HappyGifts;
import dev.ua.ikeepcalm.happygifts.gifts.Gift;
import dev.ua.ikeepcalm.happygifts.utils.AdventureAdapter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class that manages gift history GUI
 */
public class GiftHistoryGui {
    private final HappyGifts plugin;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final int ITEMS_PER_PAGE = 21;

    public enum HistoryTab {
        SENT,
        RECEIVED
    }

    private HistoryTab currentTab = HistoryTab.RECEIVED;
    private int currentPage = 0;

    public GiftHistoryGui(HappyGifts plugin) {
        this.plugin = plugin;
    }

    /**
     * Convert component to legacy string for GUI titles
     */
    private String getTitle(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * Open the main gift history menu
     */
    public void openHistoryMenu(Player player) {
        openHistoryMenu(player, currentTab, currentPage);
    }

    /**
     * Open the gift history menu with specific tab and page
     */
    public void openHistoryMenu(Player player, HistoryTab tab, int page) {
        currentTab = tab;
        currentPage = page;

        String title = getTitle(plugin.getLanguageManager().getText("gui.history.title"));
        ChestGui gui = new ChestGui(6, title);

        // Border pane
        OutlinePane borderPane = new OutlinePane(0, 0, 9, 6);
        borderPane.setRepeat(true);
        borderPane.setPriority(Pane.Priority.LOWEST);

        ItemStack borderItem = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        Component borderName = plugin.getLanguageManager().getText("gui.border.name");
        AdventureAdapter.setDisplayName(borderItem, borderName);

        borderPane.addItem(new GuiItem(borderItem, event -> event.setCancelled(true)));
        gui.addPane(borderPane);

        // Tab selector
        StaticPane tabPane = new StaticPane(0, 0, 9, 1);
        tabPane.setPriority(Pane.Priority.NORMAL);

        // Received gifts tab
        ItemStack receivedItem = new ItemStack(Material.CHEST);
        Component receivedName = plugin.getLanguageManager().getText(
                tab == HistoryTab.RECEIVED ? "gui.history.tab.received.active" : "gui.history.tab.received.inactive");
        AdventureAdapter.setDisplayName(receivedItem, receivedName);

        tabPane.addItem(new GuiItem(receivedItem, event -> {
            event.setCancelled(true);
            if (tab != HistoryTab.RECEIVED) {
                openHistoryMenu(player, HistoryTab.RECEIVED, 0);
            }
        }), 2, 0);

        // Sent gifts tab
        ItemStack sentItem = new ItemStack(Material.HOPPER);
        Component sentName = plugin.getLanguageManager().getText(
                tab == HistoryTab.SENT ? "gui.history.tab.sent.active" : "gui.history.tab.sent.inactive");
        AdventureAdapter.setDisplayName(sentItem, sentName);

        tabPane.addItem(new GuiItem(sentItem, event -> {
            event.setCancelled(true);
            if (tab != HistoryTab.SENT) {
                openHistoryMenu(player, HistoryTab.SENT, 0);
            }
        }), 6, 0);

        gui.addPane(tabPane);

        // Gift list (paginated)
        PaginatedPane giftsPane = new PaginatedPane(1, 1, 7, 4);
        giftsPane.setPriority(Pane.Priority.HIGH);

        // Get gifts based on tab
        List<Gift> gifts;
        if (tab == HistoryTab.SENT) {
            gifts = plugin.getGiftManager().getSentGifts(player.getUniqueId());
        } else {
            gifts = plugin.getGiftManager().getReceivedGifts(player.getUniqueId());
        }

        // Get gifts for current page
        List<Gift> pageGifts = plugin.getGiftManager().getGiftsPage(gifts, page, ITEMS_PER_PAGE);
        List<GuiItem> giftItems = new ArrayList<>();

        for (Gift gift : pageGifts) {
            ItemStack giftItem;

            // Choose icon based on status
            if (gift.isOpened()) {
                giftItem = new ItemStack(Material.CHEST);
            } else if (gift.isDelivered()) {
                giftItem = new ItemStack(Material.CHEST_MINECART);
            } else {
                giftItem = new ItemStack(Material.MINECART);
            }

            // Create display name
            Component giftName = plugin.getLanguageManager().getTextWithPlaceholders(
                    "gui.history.gift.name", gift.getName());
            AdventureAdapter.setDisplayName(giftItem, giftName);

            // Create lore with status
            List<Component> lore = new ArrayList<>();

            // Add status
            Component statusText = plugin.getLanguageManager().getText(gift.getStatusKey());
            lore.add(statusText);

            // Add recipient/sender info
            UUID otherPersonId = (tab == HistoryTab.SENT) ? gift.getRecipient() : gift.getSender();
            String otherPersonName = Bukkit.getOfflinePlayer(otherPersonId).getName();
            if (otherPersonName == null) otherPersonName = "Someone";

            Component otherPersonText;
            if (tab == HistoryTab.SENT) {
                otherPersonText = plugin.getLanguageManager().getTextWithPlaceholders(
                        "gui.history.gift.to", otherPersonName);
            } else {
                otherPersonText = plugin.getLanguageManager().getTextWithPlaceholders(
                        "gui.history.gift.from", otherPersonName);
            }
            lore.add(otherPersonText);

            // Add dates
            if (gift.getCreationDate() != null) {
                Component dateText = plugin.getLanguageManager().getTextWithPlaceholders(
                        "gui.history.gift.created", gift.getCreationDate().format(DATE_FORMAT));
                lore.add(dateText);
            }

            if (gift.getDeliveryDate() != null) {
                Component dateText = plugin.getLanguageManager().getTextWithPlaceholders(
                        "gui.history.gift.delivered", gift.getDeliveryDate().format(DATE_FORMAT));
                lore.add(dateText);
            }

            if (gift.getOpenedDate() != null) {
                Component dateText = plugin.getLanguageManager().getTextWithPlaceholders(
                        "gui.history.gift.opened", gift.getOpenedDate().format(DATE_FORMAT));
                lore.add(dateText);
            }

            // Add description
            if (gift.getDescription() != null && !gift.getDescription().isEmpty()) {
                lore.add(Component.empty());
                Component descText = plugin.getLanguageManager().getTextWithPlaceholders(
                        "gui.history.gift.message", gift.getDescription());
                lore.add(descText);
            }

            // Add view details option
            lore.add(Component.empty());
            lore.add(plugin.getLanguageManager().getText("gui.history.gift.view_details"));

            AdventureAdapter.setLore(giftItem, lore);

            // Add click event
            final UUID giftId = gift.getGiftId();
            giftItems.add(new GuiItem(giftItem, event -> {
                event.setCancelled(true);
                openGiftDetailsMenu(player, giftId);
            }));
        }

        giftsPane.populateWithGuiItems(giftItems);

        // Handle empty state
        if (giftItems.isEmpty()) {
            StaticPane emptyPane = new StaticPane(3, 2, 3, 1);
            emptyPane.setPriority(Pane.Priority.HIGHEST);

            ItemStack emptyItem = new ItemStack(Material.BARRIER);
            Component emptyText = plugin.getLanguageManager().getText(
                    tab == HistoryTab.SENT ? "gui.history.empty.sent" : "gui.history.empty.received");
            AdventureAdapter.setDisplayName(emptyItem, emptyText);

            emptyPane.addItem(new GuiItem(emptyItem, event -> event.setCancelled(true)), 1, 0);
            gui.addPane(emptyPane);
        } else {
            gui.addPane(giftsPane);
        }

        // Navigation controls
        StaticPane controlsPane = new StaticPane(0, 5, 9, 1);
        controlsPane.setPriority(Pane.Priority.HIGH);

        // Back button
        ItemStack backItem = new ItemStack(Material.BARRIER);
        Component backText = plugin.getLanguageManager().getText("gui.history.back");
        AdventureAdapter.setDisplayName(backItem, backText);

        controlsPane.addItem(new GuiItem(backItem, event -> {
            event.setCancelled(true);
            player.closeInventory();
            new GiftGui(plugin).openMainMenu(player);
        }), 4, 0);

        // Pagination if needed
        int maxPages = (int) Math.ceil(gifts.size() / (double) ITEMS_PER_PAGE);

        if (maxPages > 1) {
            // Previous page button
            if (page > 0) {
                ItemStack prevItem = new ItemStack(Material.ARROW);
                Component prevText = plugin.getLanguageManager().getText("gui.history.prev_page");
                AdventureAdapter.setDisplayName(prevItem, prevText);

                controlsPane.addItem(new GuiItem(prevItem, event -> {
                    event.setCancelled(true);
                    openHistoryMenu(player, tab, page - 1);
                }), 1, 0);
            }

            // Next page button
            if (page < maxPages - 1) {
                ItemStack nextItem = new ItemStack(Material.ARROW);
                Component nextText = plugin.getLanguageManager().getText("gui.history.next_page");
                AdventureAdapter.setDisplayName(nextItem, nextText);

                controlsPane.addItem(new GuiItem(nextItem, event -> {
                    event.setCancelled(true);
                    openHistoryMenu(player, tab, page + 1);
                }), 7, 0);
            }

            // Page indicator
            ItemStack pageItem = new ItemStack(Material.PAPER);
            Component pageText = plugin.getLanguageManager().getTextWithPlaceholders(
                    "gui.history.page_indicator", String.valueOf(page + 1), String.valueOf(maxPages));
            AdventureAdapter.setDisplayName(pageItem, pageText);

            controlsPane.addItem(new GuiItem(pageItem, event -> event.setCancelled(true)), 4, 0);
        }

        gui.addPane(controlsPane);
        gui.show(player);
    }

    /**
     * Open the gift details menu
     */
    public void openGiftDetailsMenu(Player player, UUID giftId) {
        Gift gift = plugin.getGiftManager().getStoredGift(giftId);
        if (gift == null) {
            player.sendMessage(plugin.getLanguageManager().getText("message.gift_not_found"));
            return;
        }

        String title = getTitle(plugin.getLanguageManager().getTextWithPlaceholders(
                "gui.history.details.title", gift.getName()));
        ChestGui gui = new ChestGui(6, title);

        // Border pane
        OutlinePane borderPane = new OutlinePane(0, 0, 9, 6);
        borderPane.setRepeat(true);
        borderPane.setPriority(Pane.Priority.LOWEST);

        ItemStack borderItem = new ItemStack(Material.PINK_STAINED_GLASS_PANE);
        Component borderName = plugin.getLanguageManager().getText("gui.border.name");
        AdventureAdapter.setDisplayName(borderItem, borderName);

        borderPane.addItem(new GuiItem(borderItem, event -> event.setCancelled(true)));
        gui.addPane(borderPane);

        // Gift info pane
        StaticPane infoPane = new StaticPane(1, 1, 7, 3);
        infoPane.setPriority(Pane.Priority.HIGH);

        // Gift info
        ItemStack infoItem = new ItemStack(Material.BOOK);
        Component infoName = plugin.getLanguageManager().getTextWithPlaceholders(
                "gui.history.details.info.name", gift.getName());
        AdventureAdapter.setDisplayName(infoItem, infoName);

        List<Component> infoLore = new ArrayList<>();

        // Status
        Component statusText = plugin.getLanguageManager().getText(gift.getStatusKey());
        infoLore.add(statusText);

        // Sender
        String senderName = Bukkit.getOfflinePlayer(gift.getSender()).getName();
        if (senderName == null) senderName = "Someone";
        infoLore.add(plugin.getLanguageManager().getTextWithPlaceholders(
                "gui.history.details.info.sender", senderName));

        // Recipient
        String recipientName = Bukkit.getOfflinePlayer(gift.getRecipient()).getName();
        if (recipientName == null) recipientName = "Someone";
        infoLore.add(plugin.getLanguageManager().getTextWithPlaceholders(
                "gui.history.details.info.recipient", recipientName));

        // Dates
        if (gift.getCreationDate() != null) {
            infoLore.add(plugin.getLanguageManager().getTextWithPlaceholders(
                    "gui.history.details.info.created", gift.getCreationDate().format(DATE_FORMAT)));
        }

        if (gift.getDeliveryDate() != null) {
            infoLore.add(plugin.getLanguageManager().getTextWithPlaceholders(
                    "gui.history.details.info.delivered", gift.getDeliveryDate().format(DATE_FORMAT)));
        }

        if (gift.getOpenedDate() != null) {
            infoLore.add(plugin.getLanguageManager().getTextWithPlaceholders(
                    "gui.history.details.info.opened", gift.getOpenedDate().format(DATE_FORMAT)));
        }

        // Description
        if (gift.getDescription() != null && !gift.getDescription().isEmpty()) {
            infoLore.add(Component.empty());
            infoLore.add(plugin.getLanguageManager().getText("gui.history.details.info.message_header"));

            // Split long descriptions into multiple lines
            String desc = gift.getDescription();
            int maxLineLength = 40;

            for (int i = 0; i < desc.length(); i += maxLineLength) {
                int endIndex = Math.min(i + maxLineLength, desc.length());
                String line = desc.substring(i, endIndex);
                infoLore.add(Component.text("  " + line).color(NamedTextColor.GRAY));
            }
        }

        AdventureAdapter.setLore(infoItem, infoLore);
        infoPane.addItem(new GuiItem(infoItem, event -> event.setCancelled(true)), 1, 1);

        // Sender head
        ItemStack senderHead = new ItemStack(Material.PLAYER_HEAD);
        if (senderHead.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(gift.getSender()));
            Component senderNameComponent = plugin.getLanguageManager().getTextWithPlaceholders(
                    "gui.history.details.sender", Bukkit.getOfflinePlayer(gift.getSender()).getName());
            AdventureAdapter.setDisplayName(senderHead, senderNameComponent);
            senderHead.setItemMeta(skullMeta);
        }
        infoPane.addItem(new GuiItem(senderHead, event -> event.setCancelled(true)), 5, 0);

        // Recipient head
        ItemStack recipientHead = new ItemStack(Material.PLAYER_HEAD);
        if (recipientHead.getItemMeta() instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(gift.getRecipient()));
            Component recipientNameComponent = plugin.getLanguageManager().getTextWithPlaceholders(
                    "gui.history.details.recipient", Bukkit.getOfflinePlayer(gift.getRecipient()).getName());
            AdventureAdapter.setDisplayName(recipientHead, recipientNameComponent);
            recipientHead.setItemMeta(skullMeta);
        }
        infoPane.addItem(new GuiItem(recipientHead, event -> event.setCancelled(true)), 5, 2);

        gui.addPane(infoPane);

        // Gift items
        PaginatedPane itemsPane = new PaginatedPane(2, 4, 5, 1);
        itemsPane.setPriority(Pane.Priority.HIGH);

        List<GuiItem> items = new ArrayList<>();
        for (ItemStack item : gift.getItems()) {
            if (item != null) {
                items.add(new GuiItem(item.clone(), event -> event.setCancelled(true)));
            }
        }

        if (!items.isEmpty()) {
            itemsPane.populateWithGuiItems(items);
            itemsPane.setPage(0);
            gui.addPane(itemsPane);

            // Items header
            StaticPane headerPane = new StaticPane(3, 3, 3, 1);
            headerPane.setPriority(Pane.Priority.HIGH);

            ItemStack headerItem = new ItemStack(Material.HOPPER);
            Component headerText = plugin.getLanguageManager().getText("gui.history.details.items");
            AdventureAdapter.setDisplayName(headerItem, headerText);

            headerPane.addItem(new GuiItem(headerItem, event -> event.setCancelled(true)), 1, 0);
            gui.addPane(headerPane);
        }

        // Back button
        StaticPane controlsPane = new StaticPane(0, 5, 9, 1);
        controlsPane.setPriority(Pane.Priority.HIGH);

        ItemStack backItem = new ItemStack(Material.ARROW);
        Component backText = plugin.getLanguageManager().getText("gui.history.details.back");
        AdventureAdapter.setDisplayName(backItem, backText);

        controlsPane.addItem(new GuiItem(backItem, event -> {
            event.setCancelled(true);
            openHistoryMenu(player);
        }), 4, 0);

        gui.addPane(controlsPane);
        gui.show(player);
    }
}