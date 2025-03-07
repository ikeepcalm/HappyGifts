package dev.ua.ikeepcalm.happygifts.gifts;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a gift that can be sent to another player
 */
@Getter
public class Gift {
    private final UUID giftId;
    private final UUID sender;
    @Setter
    private UUID recipient;
    @Setter
    private String name;
    @Setter
    private String description;
    private final List<ItemStack> items;
    @Setter
    private boolean delivered;

    /**
     * Create a new gift
     */
    public Gift(UUID sender) {
        this.giftId = UUID.randomUUID();
        this.sender = sender;
        this.items = new ArrayList<>();
        this.delivered = false;
    }

    /**
     * Create a gift with all details (used when loading from storage)
     */
    public Gift(UUID giftId, UUID sender, UUID recipient, String name, String description, List<ItemStack> items, boolean delivered) {
        this.giftId = giftId;
        this.sender = sender;
        this.recipient = recipient;
        this.name = name;
        this.description = description;
        this.items = items;
        this.delivered = delivered;
    }

    /**
     * Set an item at a specific index
     */
    public void setItem(int index, ItemStack item) {
        while (items.size() <= index) {
            items.add(null);
        }
        items.set(index, item);
    }

    /**
     * Remove an item at a specific index
     */
    public void removeItem(int index) {
        if (index < items.size()) {
            items.set(index, null);
        }
    }

    /**
     * Check if the gift is valid and ready to be sent
     */
    public boolean isValid() {
        items.removeIf(Objects::isNull);

        return name != null && !name.isEmpty() &&
                recipient != null &&
                description != null && !description.isEmpty() &&
                !items.isEmpty();
    }

    public void addItem(ItemStack item) {
        if (item != null) {
            if (items.size() + 1 <= 5) {
                items.add(item);
            }
        }
    }
}