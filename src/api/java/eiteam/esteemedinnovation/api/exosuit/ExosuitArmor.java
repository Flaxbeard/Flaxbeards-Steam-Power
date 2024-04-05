package eiteam.esteemedinnovation.api.exosuit;

import eiteam.esteemedinnovation.api.Engineerable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public interface ExosuitArmor extends Engineerable {
    /**
     * @return The string representing the location for the armor's item texture.
     */
    ResourceLocation getItemIconResource();

    /**
     * @param me The armor piece to check in.
     * @param check The item upgrade to check for.
     * @return Whether the provided piece of armor has the provided exosuit upgrade. Should return false if either are null.
     */
    boolean hasUpgrade(@Nonnull ItemStack me, Item check);

    /**
     * @param self The ItemStack containing the armor piece
     * @return An array of all of the ExosuitUpgrades in this armor piece. Cannot be null.
     */
    @Nonnull
    ExosuitUpgrade[] getUpgrades(@Nonnull ItemStack self);

    /**
     * @param self The ItemStack containing the armor piece.
     * @return An array of all the ExosuitEventHandlers installed in the armor piece. This includes both ExosuitUpgrades
     *         and ExosuitPlates, along with anything else that you may create that implements ExosuitEventHandler.
     *         This is used for event handling delegation, but is not limited to that. Cannot be null.
     */
    @Nonnull
    ExosuitEventHandler[] getInstalledEventHandlers(@Nonnull ItemStack self);
}
