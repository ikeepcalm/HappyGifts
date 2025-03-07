package dev.ua.ikeepcalm.happygifts.utils;

import com.github.stefvanschie.inventoryframework.gui.GuiItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility class for adapting Adventure API components to inventory items
 */
public class AdventureAdapter {

    /**
     * Convert an Adventure Component to legacy string format for inventory framework
     */
    public static String componentToLegacy(Component component) {
        return LegacyComponentSerializer.legacySection().serialize(component);
    }

    /**
     * Apply Adventure Component as the display name of an ItemStack
     */
    public static void setDisplayName(ItemStack item, Component displayName) {
        if (item != null && item.getItemMeta() != null) {
            ItemMeta meta = item.getItemMeta();
            meta.displayName(displayName.decoration(TextDecoration.ITALIC, false));
            item.setItemMeta(meta);
        }
    }

    /**
     * Apply a list of Adventure Components as the lore of an ItemStack
     * and removing italic formatting from each component
     */
    public static void setLore(ItemStack item, List<Component> lore) {
        if (item != null && item.getItemMeta() != null) {
            List<Component> nonItalicLore = new ArrayList<>();
            for (Component component : lore) {
                nonItalicLore.add(component.decoration(TextDecoration.ITALIC, false));
            }

            ItemMeta meta = item.getItemMeta();
            meta.lore(nonItalicLore);
            item.setItemMeta(meta);
        }
    }

    /**
     * Apply Adventure Component as the lore of an ItemStack,
     * converting the newlines in the Component to separate lore lines
     * and removing italic formatting while preserving other formatting
     */
    public static void setLore(ItemStack item, Component lore) {
        if (item != null && item.getItemMeta() != null) {
            String miniMessageString = MiniMessage.miniMessage().serialize(lore);

            String[] lines = miniMessageString.split("\n");
            List<Component> loreComponents = new ArrayList<>();
            for (String line : lines) {
                Component lineComponent = MiniMessage.miniMessage().deserialize(line);
                lineComponent = lineComponent.decoration(TextDecoration.ITALIC, false);
                loreComponents.add(lineComponent);
            }

            ItemMeta meta = item.getItemMeta();
            meta.lore(loreComponents);
            item.setItemMeta(meta);
        }
    }

    /**
     * Create a GuiItem with Adventure Component display name and lore
     */
    public static GuiItem createGuiItem(ItemStack item, Component displayName, Component lore, Consumer<InventoryClickEvent> action) {
        setDisplayName(item, displayName);
        setLore(item, lore);
        return new GuiItem(item, action);
    }

}