package eiteam.esteemedinnovation.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public interface SteamChargable {
    /**
     * How much steam is used per use
     */
    int steamPerDurability();

    /**
     * Called to ensure the item can be charged with steam devices
     * @param me The itemstack of the item
     * @return true if the item can charge
     */
    boolean canCharge(@Nonnull ItemStack me);

    /**
     * Adds an amount of steam to the item.
     * @param me The ItemStack
     * @param amount How much steam to add.
     * @param entity The entity using the thing.
     * @return Whether it was a successful add.
     */
    boolean addSteam(@Nonnull ItemStack me, int amount, LivingEntity entity);

    /**
     * Checks whether the ItemStack has the amount of power in its steam storage. The opposite of
     * @param me The ItemStack
     * @param powerNeeded The amount of power needed
     * @return True if it has power, false if it doesn't.
     * @see #needsPower(ItemStack, int)
     */
    boolean hasPower(@Nonnull ItemStack me, int powerNeeded);

    /**
     * Checks whether the ItemStack can have the amount of power added to its steam storage.
     * @param me The ItemStack
     * @param powerNeeded The amount of power to add
     * @return True if it will not exceed the limit with this amount of power added to it, false if
     *         it will, or if it is not a chestplate.
     */
    boolean needsPower(@Nonnull ItemStack me, int powerNeeded);

    /**
     * Drains the provided amount of steam from the armor.
     * @param me The armor piece to drain from.
     * @param amountToDrain The amount of steam to drain from the armor.
     * @param entity The entity using the thing.
     * @return Whether steam was successfully drained.
     */
    boolean drainSteam(@Nonnull ItemStack me, int amountToDrain, LivingEntity entity);
}
