package dev.ua.ikeepcalm.happygifts.gifts;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
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
    @Setter
    private LocalDateTime creationDate;
    @Setter
    private LocalDateTime deliveryDate;
    @Setter
    private LocalDateTime openedDate;
    @Setter
    private boolean opened;

    /**
     * Create a new gift
     */
    public Gift(UUID sender) {
        this.giftId = UUID.randomUUID();
        this.sender = sender;
        this.items = new ArrayList<>();
        this.delivered = false;
        this.opened = false;
        this.creationDate = LocalDateTime.now();
    }

    /**
     * Create a gift with all details (used when loading from storage)
     */
    public Gift(UUID giftId, UUID sender, UUID recipient, String name, String description,
                List<ItemStack> items, boolean delivered, LocalDateTime creationDate,
                LocalDateTime deliveryDate, LocalDateTime openedDate, boolean opened) {
        this.giftId = giftId;
        this.sender = sender;
        this.recipient = recipient;
        this.name = name;
        this.description = description;
        this.items = items;
        this.delivered = delivered;
        this.opened = opened;
        this.creationDate = creationDate;
        this.deliveryDate = deliveryDate;
        this.openedDate = openedDate;
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

    /**
     * Mark the gift as delivered and set the delivery date
     */
    public void markDelivered() {
        this.delivered = true;
        this.deliveryDate = LocalDateTime.now();
    }

    /**
     * Mark the gift as opened and set the opened date
     */
    public void markOpened() {
        this.opened = true;
        this.openedDate = LocalDateTime.now();
    }

    /**
     * Get gift status for display in history
     * @return Status string key for language manager
     */
    public String getStatusKey() {
        if (opened) {
            return "gift.status.opened";
        } else if (delivered) {
            return "gift.status.delivered";
        } else {
            return "gift.status.pending";
        }
    }
}