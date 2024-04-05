package eiteam.esteemedinnovation.api.exosuit;

import eiteam.esteemedinnovation.api.util.ItemStackUtility;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.function.BiPredicate;

public class ExosuitUtility {
    /**
     * @param entityLiving The entity being checked against.
     * @return The number of ExosuitArmor pieces the Entity is wearing in their equipment slots.
     */
    public static int getExoArmor(LivingEntity entityLiving) {
        return getExoArmorMatchesPredicate(entityLiving, (slot, stack) -> true);
    }

    /**
     * @param entityLiving The entity being checked against.
     * @param predicate A predicate that passes the slot and its according item stack.
     * @return The number of ExosuitArmor pieces the Entity is wearing that match the provided predicate.
     */
    public static int getExoArmorMatchesPredicate(LivingEntity entityLiving, BiPredicate<EquipmentSlot, ItemStack> predicate) {
        int num = 0;

        for (EquipmentSlot armor : ItemStackUtility.ARMOR_SLOTS) {
            ItemStack stack = entityLiving.getItemBySlot(armor);
            if (stack.getItem() instanceof ExosuitArmor && predicate.test(armor, stack)) {
                num++;
            }
        }
        return num;
    }
}
