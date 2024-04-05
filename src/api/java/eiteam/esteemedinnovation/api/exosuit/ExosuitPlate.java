package eiteam.esteemedinnovation.api.exosuit;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import eiteam.esteemedinnovation.api.Constants;
import eiteam.esteemedinnovation.api.util.ItemStackUtility;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;

import javax.annotation.Nonnull;

public class ExosuitPlate implements ExosuitEventHandler {
    private String identifier;
    private String invMod;
    private String armorMod;
    // TODO: We may be able to remove this. It does not seem to be used anywhere.
    private Object plate;
    private String effect;

    public ExosuitPlate(String id, Object item, String invLocMod, String armorLocMod, String effectLoc) {
        identifier = id;
        invMod = invLocMod;
        armorMod = armorLocMod;
        plate = item;
        effect = effectLoc;
    }

    public String getIdentifier() {
        return identifier;
    }

    public Object getItem() {
        return plate;
    }

    public void setItem(Object item) {
        plate = item;
    }

    public ResourceLocation getIcon(ExosuitArmor item) {
        return new ResourceLocation(item.getItemIconResource() + "_" + invMod);
    }

    public String getArmorLocation(ExosuitArmor item, EquipmentSlot slot) {
        // TODO: Abstract out of API
        if (slot != EquipmentSlot.LEGS) {
            return Constants.EI_MODID + ":textures/models/armor/exo_plate_" + armorMod + "_1.png";
        } else {
            return Constants.EI_MODID + ":textures/models/armor/exo_plate_" + armorMod + "_2.png";
        }
    }

    public String getArmorMod() {
        return armorMod;
    }

    /**
     * @param slot The armor slot that this plate is installed in
     * @param source The damage source
     * @return The damage reduction amount for the slot and the source. Default implementation returns the IRON
     *         damage reduction amount.
     */
    public int getDamageReductionAmount(EquipmentSlot slot, DamageSource source) {
        return ArmorMaterials.IRON.getDefenseForType(ItemStackUtility.equipmentSlotToArmorType(slot));
    }

    public String effect() {
        return I18n.get(effect);
    }

    /**
     * @see ExosuitUpgrade#getAttributeModifiersForExosuit(EquipmentSlot, ItemStack)
     */
    public Multimap<String, AttributeModifier> getAttributeModifiersForExosuit(EquipmentSlot armorSlot, @Nonnull ItemStack armorPieceStack) {
        return HashMultimap.create();
    }
}
