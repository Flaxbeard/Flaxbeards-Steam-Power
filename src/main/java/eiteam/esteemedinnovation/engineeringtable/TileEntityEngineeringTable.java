package eiteam.esteemedinnovation.engineeringtable;

import eiteam.esteemedinnovation.api.Engineerable;
import eiteam.esteemedinnovation.api.tile.BlockEntityBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

public class TileEntityEngineeringTable extends BlockEntityBase {
    private final IItemHandler engineerableItem = new ItemStackHandler(1) {
        private void setUpgradeSlotContents(int slot, ItemStack upgradeStack) {
            ItemStack mainStack = getStackInSlot(0);
            Item mainItem = mainStack.getItem();
            if (mainItem instanceof Engineerable) {
                ((Engineerable) mainItem).setInventorySlotContents(mainStack, slot, upgradeStack);
            }
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            super.setStackInSlot(slot, stack);
            if (slot > 0) {
                setUpgradeSlotContents(slot, stack);
            }
        }

        @Nonnull
        @Override
        public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
            ItemStack remaining = super.insertItem(slot, stack, simulate);
            if (slot > 0 && !simulate) {
                setUpgradeSlotContents(slot, stack);
            }
            return remaining;
        }

        @Nonnull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            ItemStack remaining = super.extractItem(slot, amount, simulate);
            if (slot > 0 && !simulate) {
                ItemStack mainStack = getStackInSlot(0);
                ((Engineerable) mainStack.getItem()).decrStackSize(mainStack, slot, amount);
            }
            return remaining;
        }
    };
    
    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        if (compound.hasKey("Item")) {
            engineerableItem.insertItem(0, new ItemStack(compound.getCompoundTag("Item")), false);
        }
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setTag("Item", engineerableItem.getStackInSlot(0).writeToNBT(new NBTTagCompound()));
        return compound;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
        return capability == ITEM_HANDLER_CAPABILITY ? ITEM_HANDLER_CAPABILITY.cast(engineerableItem) : super.getCapability(capability, facing);
    }
}
