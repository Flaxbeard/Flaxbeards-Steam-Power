package eiteam.esteemedinnovation.metalcasting.mold;

import eiteam.esteemedinnovation.api.crucible.CrucibleLiquid;
import eiteam.esteemedinnovation.api.mold.CrucibleMold;
import eiteam.esteemedinnovation.api.tile.BlockEntityBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileEntityMold extends BlockEntityBase implements ISidedInventory, ITickable {
    private static final int[] moldSlots = new int[] {0};
    public boolean isOpen = true;
    @Nonnull
    public ItemStack mold = ItemStack.EMPTY;
    public int changeTicks = 0;
    @Nonnull
    private ItemStack inventory = ItemStack.EMPTY;

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        super.getUpdatePacket();
        NBTTagCompound access = new NBTTagCompound();
        access.setBoolean("isOpen", isOpen);
        access.setInteger("changeTicks", changeTicks);
        NBTTagCompound nbttagcompound1;

        nbttagcompound1 = new NBTTagCompound();
        mold.writeToNBT(nbttagcompound1);
        access.setTag("mold", nbttagcompound1);

        nbttagcompound1 = new NBTTagCompound();
        inventory.writeToNBT(nbttagcompound1);
        access.setTag("inventory", nbttagcompound1);

        return new SPacketUpdateTileEntity(pos, 1, access);
    }


    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        isOpen = access.getBoolean("isOpen");
        if (changeTicks == 0) {
            changeTicks = access.getInteger("changeTicks");
        }

        inventory = new ItemStack(access.getCompoundTag("inventory"));
        mold = new ItemStack(access.getCompoundTag("mold"));
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        isOpen = nbt.getBoolean("isOpen");

        inventory = new ItemStack(nbt.getCompoundTag("inventory"));
        mold = new ItemStack(nbt.getCompoundTag("mold"));
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isOpen", isOpen);

        NBTTagCompound nbttagcompound1 = new NBTTagCompound();

        mold.writeToNBT(nbttagcompound1);
        nbt.setTag("mold", nbttagcompound1);

        inventory.writeToNBT(nbttagcompound1);
        nbt.setTag("inventory", nbttagcompound1);

        return nbt;
    }

    public boolean canPour() {
        return !isOpen && !mold.isEmpty() && inventory.isEmpty();
    }

    public void pour(CrucibleLiquid liquid) {
        inventory = ((CrucibleMold) mold.getItem()).getItemFromLiquid(liquid, mold);
        inventory.setCount(1);
        markDirty();
    }

    public void dropItem(ItemStack item) {
        EntityItem entityItem = new EntityItem(world, pos.getX() + 0.5F, pos.getY() + 1.5F, pos.getZ() + 0.5F, item);
        world.spawnEntity(entityItem);
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Nonnull
    @Override
    public ItemStack getStackInSlot(int var1) {
        return inventory;
    }

    @Nonnull
    @Override
    public ItemStack decrStackSize(int slot, int amt) {
        markDirty();
        if (inventory.getCount() <= amt) {
            ItemStack itemstack = inventory;
            inventory = ItemStack.EMPTY;
            return itemstack;
        }
        ItemStack itemstack = inventory.splitStack(amt);
        if (inventory.isEmpty()) {
            inventory = ItemStack.EMPTY;
        }
        return itemstack;
    }

    @Nonnull
    @Override
    public ItemStack removeStackFromSlot(int slot) {
        ItemStack itemstack = inventory;
        inventory = ItemStack.EMPTY;
        return itemstack;
    }

    @Override
    public void setInventorySlotContents(int slot, @Nonnull ItemStack stack) {
        inventory = stack;
    }

    @Nonnull
    @Override
    public String getName() {
        return "container.mold";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

//    TileEntity defines this method as @Nullable.
    @SuppressWarnings("NullableProblems")
    @Nullable
    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
        return isOpen;
    }

    @Override
    public void openInventory(@Nonnull EntityPlayer player) {}

    @Override
    public void closeInventory(@Nonnull EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
        return false;
    }

    @Override
    public int getField(int id) {
        return changeTicks;
    }

    @Override
    public void setField(int id, int value) {
        changeTicks = value;
    }

    @Override
    public int getFieldCount() {
        return 1;
    }

    @Override
    public void clear() {
        inventory = ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty() {
        // TODO
        return inventory.isEmpty();
    }

    @Nonnull
    @Override
    public int[] getSlotsForFace(@Nonnull EnumFacing side) {
        if (side == EnumFacing.DOWN) {
            return moldSlots;
        }
        return new int[0];
    }

    @Override
    public boolean canInsertItem(int slot, @Nonnull ItemStack item, @Nonnull EnumFacing side) {
        return false;
    }

    @Override
    public boolean canExtractItem(int slot, @Nonnull ItemStack item, @Nonnull EnumFacing side) {
        return true;
    }

    @Override
    public void update() {
        if (changeTicks > 0) {
            changeTicks--;
        }
        if (isOpen && !inventory.isEmpty() && changeTicks < 10) {
            if (!world.isRemote) {
                dropItem(inventory);
            }
            inventory = ItemStack.EMPTY;
        }
    }
}
