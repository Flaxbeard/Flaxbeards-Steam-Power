package eiteam.esteemedinnovation.api.exosuit;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class UtilPlates {
    public static ResourceLocation getIconFromPlate(String name, ExosuitArmor item) {
        ExosuitPlate plate = ExosuitRegistry.plates.get(name);
        return plate.getIcon(item);
    }

    public static String getArmorLocationFromPlate(String name, ExosuitArmor item, EquipmentSlot armorType) {
        ExosuitPlate plate = ExosuitRegistry.plates.get(name);
        return plate.getArmorLocation(item, armorType);
    }

    public static ExosuitPlate getPlate(String string) {
        return ExosuitRegistry.plates.get(string);
    }

    /**
     * Removes Exosuit Plates from the given Exosuit piece.
     * TODO: This is too detail-focused and does not work for non-steam suits.
     * @param exosuitPiece The Exosuit Piece to remove the plates from.
     */
    public static void removePlate(ItemStack exosuitPiece) {
        if (exosuitPiece.hasTag()) {
            CompoundTag nbt = exosuitPiece.getTag();
            if (nbt.contains("plate")) {
                nbt.remove("plate");
            }
            if (nbt.contains("inv") && nbt.getCompound("inv").contains("1")) {
                nbt.getCompound("inv").remove("1");
            }
        }
    }
}
