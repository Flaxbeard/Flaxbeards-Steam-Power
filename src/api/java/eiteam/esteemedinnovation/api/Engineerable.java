package eiteam.esteemedinnovation.api;

import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

public interface Engineerable {
    Pair<Integer, Integer>[] engineerCoordinates();

    @Nonnull
    ItemStack getStackInSlot(@Nonnull ItemStack me, int var1);

    void setInventorySlotContents(@Nonnull ItemStack me, int var1, @Nonnull ItemStack stack);

    boolean isItemValidForSlot(@Nonnull ItemStack me, int var1, @Nonnull ItemStack var2);

    @Nonnull
    ItemStack decrStackSize(@Nonnull ItemStack me, int var1, int var2);

    void drawSlot(ContainerScreen screen, int slotnum, int i, int j);

    boolean canPutInSlot(@Nonnull ItemStack me, int slotNum, @Nonnull ItemStack upgrade);

    void drawBackground(ContainerScreen screen, int i, int j, int k);
}
