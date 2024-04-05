package eiteam.esteemedinnovation.api.tool;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: Use correct ID ranges (1-10? we only have 2 upgrades for each tool). Perhaps instead of using an NBTTagList we
// could simply have CoreUpgrade and HeadUpgrade NBT tags.
// TODO: Return pairs instead of lists.
public class UtilSteamTool {
    @SuppressWarnings("unchecked")
    public static final Pair<Integer, Integer>[] ENGINEER_COORDINATES = new Pair[] {
      Pair.of(60, 12),
      Pair.of(37, 40)
    };

    /**
     * Checks if the ItemStack has a particular upgrade. You can also call directly on the {@link SteamTool} item
     * rather than this. This is only used internally by EI for the actual {@link SteamTool#hasUpgrade(ItemStack, Item)}
     * implementations in the steam tool classes.
     * @param me The ItemStack version of the drill
     * @param check The item that is being checked against, or the upgrade
     * @return Whether it has any upgrades.
     */
    public static boolean hasUpgrade(@Nonnull ItemStack me, @Nonnull Item check) {
        if (check == Items.AIR) {
            return false;
        }

        if (me.hasTag() && me.getTag().contains("upgrades")) {
            for (int i = 1; i < 10; i++) {
                if (me.getTag().getCompound("upgrades").contains(Integer.toString(i))) {
                    ItemStack stack = ItemStack.of(me.getTag().getCompound("upgrades").getCompound(Integer.toString(i)));
                    if (stack.getItem() == check) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Gets all of the upgrades (except non-standard ones that do not implement {@link SteamToolUpgrade})
     * that are installed in the tool
     * @param me The tool ItemStack.
     * @return The {@link List} of all the upgrades. This can be empty. Expect emptiness.
     */
    public static List<SteamToolUpgrade> getUpgrades(@Nonnull ItemStack me) {
        List<SteamToolUpgrade> upgrades = new ArrayList<>();
        if (!me.hasTag() || !me.getTag().contains("upgrades")) {
            return upgrades;
        }

        CompoundTag unbt = me.getTag().getCompound("upgrades");

        for (int i = 1; i < 10; i++) {
            if (unbt.contains(Integer.toString(i))) {
                Item item = ItemStack.of(unbt.getCompound(Integer.toString(i))).getItem();
                if (item instanceof SteamToolUpgrade) {
                    upgrades.add((SteamToolUpgrade) item);
                }
            }
        }

        return upgrades;
    }

    /**
     * Exactly like {@link #getUpgrades(ItemStack)}, but obtains ItemStacks instead of {@link SteamToolUpgrade}.
     * @param self The ItemStack of the tool
     * @return A List of all the upgrade ItemStacks.
     */
    public static List<ItemStack> getUpgradeStacks(@Nonnull ItemStack self) {
        List<ItemStack> upgrades = new ArrayList<>();
        if (!self.hasTag() || !self.getTag().contains("upgrades")) {
            return upgrades;
        }

        CompoundTag unbt = self.getTag().getCompound("upgrades");
        for (int i = 0; i < 10; i++) {
            if (unbt.contains(Integer.toString(i))) {
                ItemStack stack = ItemStack.of(unbt.getCompound(Integer.toString(i)));
                Item item = stack.getItem();
                if (item instanceof SteamToolUpgrade) {
                    upgrades.add(stack);
                }
            }
        }

        return upgrades;
    }

    /**
     * Stores the provided upgrade stack in the provided steam tool stack. Used in setInventorySlotContents, which is
     * called by the Engineering Table when upgrading an item.
     * @param me The ItemStack being edited.
     * @param slot Upgrade slot
     * @param upgradeStack The upgrade being installed
     */
    public static void setNBTInventory(@Nonnull ItemStack me, int slot, @Nonnull ItemStack upgradeStack) {
        if (!me.hasTag()) {
            me.setTag(new CompoundTag());
        }
        if (!me.getTag().contains("upgrades")) {
            me.getTag().put("upgrades", new CompoundTag());
        }
        if (me.getTag().getCompound("upgrades").contains(Integer.toString(slot))) {
            me.getTag().getCompound("upgrades").remove(Integer.toString(slot));
        }
        if (!upgradeStack.isEmpty()) {
            me.getTag().getCompound("upgrades").put(Integer.toString(slot), upgradeStack.save(new CompoundTag()));
        }
    }

    /**
     * Checks if the ItemStack has the Speed and Ticks NBT tags. If it doesn't, it creates them
     * and sets them to 0.
     * @param me The ItemStack of the tool
     * @return The ItemStack's new CompoundTag.
     */
    @Nonnull
    public static CompoundTag checkNBT(@Nonnull ItemStack me) {
        if (!me.hasTag()) {
            me.setTag(new CompoundTag());
        }
        if (!me.getTag().contains("Speed")) {
            me.getTag().putInt("Speed", 0);
        }
        if (!me.getTag().contains("Ticks")) {
            me.getTag().putInt("Ticks", 0);
        }
        return me.getTag();
    }

    /**
     * Gets a List of tooltip components that should be put in the item's tooltip.
     * @param upgrades The ItemStacks that are being tested against. See {@link #getUpgradeStacks(ItemStack)}
     * @param redSlot The slot that should be red. See {@link ItemSteamTool#getRedSlot()}.
     * @return The components to be added.
     */
    @Nonnull
    public static List<Component> getUpgradeTooltipComponents(@Nullable Iterable<ItemStack> upgrades, @Nonnull SteamToolSlot redSlot) {
        if (upgrades == null) {
            return Collections.emptyList();
        }

        List<Component> components = new ArrayList<>();

        for (ItemStack stack : upgrades) {
            SteamToolUpgrade upgrade = (SteamToolUpgrade) stack.getItem();
            ChatFormatting format = upgrade.getToolSlot() == redSlot ? ChatFormatting.RED : ChatFormatting.DARK_GREEN;
            String id = stack.getDescriptionId();
            String toAdd = I18n.exists(id + ".info") ? I18n.get(id) + ".info" : stack.getDescriptionId() + ".name";
            components.add(Component.translatable(toAdd).withStyle(format));
        }

        return components;
    }

    /**
     * Gets the upgrade tooltips for the upgrades in the provided steam tool.
     * @param toolStack The steam tool.
     * @param redSlot The slot that should be red. See {@link ItemSteamTool#getRedSlot()}.
     * @return The components to be added.
     */
    @Nonnull
    public static List<Component> getUpgradeTooltipComponents(ItemStack toolStack, @Nonnull SteamToolSlot redSlot) {
        return getUpgradeTooltipComponents(getUpgradeStacks(toolStack), redSlot);
    }
}
