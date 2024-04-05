package eiteam.esteemedinnovation.api.enhancement;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class UtilEnhancements {
    /**
     * Registers the ResourceLocations for the icons for every possible upgrade for the provided item.
     * @param item The item that can take enhancements.
     * @return A list of all registered resource locations for the item.
     */
    public static List<ResourceLocation> registerEnhancementsForItem(Item item) {
        List<ResourceLocation> locs = new ArrayList<>();
        for (Enhancement enhancement : EnhancementRegistry.enhancements.values()) {
            if (enhancement.canApplyTo(new ItemStack(item))) {
                ResourceLocation loc = enhancement.getModel(item);
                locs.add(loc);
                EnhancementRegistry.enhancementIcons.put(Pair.of(item, enhancement), loc);
            }
        }
        return locs;
    }

    public static boolean hasEnhancement(@Nonnull ItemStack item) {
        return getEnhancementFromItem(item) != null;
    }

    public static Enhancement getEnhancementFromItem(@Nonnull ItemStack item) {
        if (item.hasTag()) {
            CompoundTag nbt = item.getTag();
            if (nbt.contains("enhancements")) {
                CompoundTag enhancements = nbt.getCompound("enhancements");
                return EnhancementRegistry.enhancements.get(enhancements.getString("id"));
            }
        }
        return null;
    }

    public static ResourceLocation getIconFromEnhancement(@Nonnull ItemStack item, Enhancement enhancement) {
        return EnhancementRegistry.enhancementIcons.get(Pair.of(item.getItem(), enhancement));
    }

    public static ResourceLocation getIconFromEnhancement(@Nonnull ItemStack item) {
        return getIconFromEnhancement(item, getEnhancementFromItem(item));
    }

    public static String getNameFromEnhancement(@Nonnull ItemStack item, Enhancement enhancement) {
        return enhancement.getName(item.getItem());
    }

    public static String getNameFromEnhancement(@Nonnull ItemStack item) {
        return getNameFromEnhancement(item, getEnhancementFromItem(item));
    }

    public static String getEnhancementDisplayText(@Nonnull ItemStack item) {
        if (hasEnhancement(item)) {
            return ChatFormatting.RED.toString() + new ItemStack(((Item) getEnhancementFromItem(item))).getDisplayName();
        }
        return "";
    }

    public static boolean canEnhance(@Nonnull ItemStack item) {
        return !item.hasTag() || !item.getTag().contains("enhancements");
    }

    public static ItemStack getEnhancedItem(@Nonnull ItemStack item, @Nonnull Enhancement enhancement) {
        ItemStack output = item.copy();
        if (!output.hasTag()) {
            output.setTag(new CompoundTag());
        }
        CompoundTag enhancements = new CompoundTag();
        enhancements.putString("id", enhancement.getID());
        output.getTag().put("enhancements", enhancements);

        return output;
    }

    public static void removeEnhancement(@Nonnull ItemStack item) {
        if (item.hasTag() && item.getTag().contains("enhancements")) {
            item.getTag().remove("enhancements");
        }
    }
}
