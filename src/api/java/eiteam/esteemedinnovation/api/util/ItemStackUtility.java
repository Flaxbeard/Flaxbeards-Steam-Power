package eiteam.esteemedinnovation.api.util;

import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ItemStackUtility {
    /**
     * A performance-friendly cache of all the equipment slots.
     */
    public static final EquipmentSlot[] EQUIPMENT_SLOTS = EquipmentSlot.values();

    /**
     * A performance-friendly cache of all armor slots (excludes hand slots).
     */
    public static final EquipmentSlot[] ARMOR_SLOTS = new ArrayList<>(Arrays.asList(EQUIPMENT_SLOTS))
      .stream()
      .filter(slot -> slot.getType() == EquipmentSlot.Type.ARMOR)
      .collect(Collectors.toList())
      .toArray(new EquipmentSlot[] {});

    public static boolean compareItemStacks(@Nonnull ItemStack stack1, @Nonnull ItemStack stack2) {
        return stack2.getItem() == stack1.getItem() && stack2.getDamageValue() == stack1.getDamageValue();
    }

    /**
     * Gets the EquipmentSlot by enum index.
     * @param index The index
     * @return The slot or null.
     */
    @Nullable
    public static EquipmentSlot getSlotFromSlotIndex(int index) {
        for (EquipmentSlot slot : EQUIPMENT_SLOTS) {
            if (slot.getIndex() == index) {
                return slot;
            }
        }
        return null;
    }

    @Nullable
    public static EquipmentSlot getSlotFromIndex(int index) {
        for (EquipmentSlot slot : EQUIPMENT_SLOTS) {
            if (slot.getIndex() == index) {
                return slot;
            }
        }
        return null;
    }

    /**
     * Gets the player's held item by priority (main, off, null)
     * @param player The player
     * @return The main hand itemstack, offhand itemstack, or null if both were empty itemstacks.
     */
    @Nullable
    public static ItemStack getHeldItemStack(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        if (mainHand.isEmpty()) {
            return offHand.isEmpty() ? ItemStack.EMPTY : offHand;
        } else {
            return mainHand;
        }
    }

    /**
     * Checks if the item is in the inventory. Does not handle metadata.
     * @param inventory The inventory
     * @param check The item
     * @return boolean
     */
    public static boolean inventoryHasItem(Container inventory, Item check) {
        return findItemStackFromInventory(inventory, check) != null;
    }

    /**
     * Searches for an item in the inventory and returns its stack.
     * @param haystack The inventory to search in
     * @param needle The item to search for
     * @return The itemstack, or null
     */
    @Nullable
    public static ItemStack findItemStackFromInventory(Container haystack, Item needle) {
        for (int slot = 0; slot < haystack.getContainerSize(); slot++) {
            ItemStack inSlot = haystack.getItem(slot);
            if (inSlot.isEmpty()) {
                continue;
            }
            if (inSlot.getItem() == needle) {
                return inSlot;
            }
        }
        return null;
    }

    /**
     * A very specific method for consuming an ItemStack in the player's inventory.
     * This is basically the old consumeInventoryItem method.
     * @param inventory The player's inventory
     * @param item The item to consume.
     */
    public static void consumePlayerInventoryItem(Inventory inventory, Item item) {
        ItemStack stack = findItemStackFromInventory(inventory, item);
        if (stack == null) {
            return;
        }
        stack.shrink(1);

        if (stack.isEmpty()) {
            inventory.removeItem(stack);
        }
    }

    /**
     * @param item The ItemStack to check
     * @param tag The tag to check
     * @return Whether the provided ItemStack is tagged as provided.
     */
    public static boolean isItemTaggedAs(ItemStack item, TagKey<Item> tag) {
        return item.getTags().anyMatch(t -> t.equals(tag));
    }

    /**
     * @param slot The equipment slot
     * @return The corresponding armor type for the provided equipment slot. Null if offhand or mainhand slots are provided.
     */
    public static ArmorItem.Type equipmentSlotToArmorType(EquipmentSlot slot) {
        return switch (slot) {
            case FEET -> ArmorItem.Type.BOOTS;
            case LEGS -> ArmorItem.Type.LEGGINGS;
            case CHEST -> ArmorItem.Type.CHESTPLATE;
            case HEAD -> ArmorItem.Type.HELMET;
            case MAINHAND, OFFHAND -> null;
        };
    }
}
