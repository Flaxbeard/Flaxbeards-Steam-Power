package eiteam.esteemedinnovation.api;

import eiteam.esteemedinnovation.api.exosuit.ExosuitArmor;
import eiteam.esteemedinnovation.api.util.ItemStackUtility;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ChargableUtility {
    /**
     * Helper method for draining the steam from an exosuit without having to explicitly null check and type check
     * in your own code every time.
     * @param stack The ItemStack that contains the armor piece
     * @param amount The amount of steam to drain.
     * @param entity The entity using the thing.
     * @return Whether it was successfully drained.
     */
    public static boolean drainSteam(@Nonnull ItemStack stack, int amount, LivingEntity entity) {
        Item item = stack.getItem();
        return item instanceof SteamChargable &&
          ((SteamChargable) item).canCharge(stack) &&
          ((SteamChargable) item).drainSteam(stack, amount, entity);
    }

    /**
     * Helper method for checking if the provided entity has steam in their exosuit, without having to explicitly
     * find the proper armor piece, null check, type check, and check for the power every time.
     * @param entityLiving The entity to check for power. It will check every equipment slot.
     * @param i The amount of power required.
     * @return Whether the player has any {@link ExosuitArmor} piece in their inventory with the required amount of power.
     *         It will return as soon as it finds a single piece (so you could have 1 piece with no power, and 1 with
     *         {@code i} power, and it will return true as soon as it finds the one with {@code i} power).
     */
    public static boolean hasPower(LivingEntity entityLiving, int i) {
        boolean hasPower = false;
        for (EquipmentSlot slot : ItemStackUtility.ARMOR_SLOTS) {
            ItemStack equipment = entityLiving.getItemBySlot(slot);
            Item item = equipment.getItem();
            if (item instanceof SteamChargable && ((SteamChargable) item).canCharge(equipment)) {
                hasPower = ((SteamChargable) item).hasPower(equipment, i);
            }
            if (hasPower) {
                break;
            }
        }
        return hasPower;
    }

    /**
     * @param elb The entity to check
     * @return The {@link SteamChargable#steamPerDurability()} value in the first found {@link SteamChargable} armor
     *         piece that the entity is wearing. 0 if not found.
     */
    public static int steamPerDurabilityInArmor(LivingEntity elb) {
        ItemStack chargableArmor = findFirstChargableArmor(elb);
        if (chargableArmor != null && chargableArmor.getItem() instanceof SteamChargable) {
            return ((SteamChargable) chargableArmor.getItem()).steamPerDurability();
        }
        return 0;
    }

    /**
     * @param elb The entity to check
     * @return The first {@link SteamChargable} item that the entity is wearing. Null if not found.
     */
    @Nullable
    public static ItemStack findFirstChargableArmor(LivingEntity elb) {
        for (EquipmentSlot slot : ItemStackUtility.ARMOR_SLOTS) {
            ItemStack equipment = elb.getItemBySlot(slot);
            Item item = equipment.getItem();
            if (item instanceof SteamChargable && ((SteamChargable) item).canCharge(equipment)) {
                return equipment;
            }
        }
        return null;
    }
}
