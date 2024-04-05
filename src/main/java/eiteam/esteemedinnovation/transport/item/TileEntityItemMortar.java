package eiteam.esteemedinnovation.transport.item;

import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import eiteam.esteemedinnovation.transport.TransportationModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityItemMortar extends SteamTransporterBlockEntity implements IInventory {
    public int xTarget;
    public int zTarget;
    public boolean hasTarget = false;
    public int fireTicks = 0;
    private ItemStack inventory;

    public TileEntityItemMortar() {
        super(new EnumFacing[] {
          EnumFacing.DOWN,
          EnumFacing.NORTH,
          EnumFacing.SOUTH,
          EnumFacing.EAST,
          EnumFacing.WEST
        });
        addSidesToGaugeBlacklist(EnumFacing.VALUES);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        xTarget = nbt.getShort("xTarget");
        zTarget = nbt.getShort("zTarget");
        fireTicks = nbt.getShort("fireTicks");
        hasTarget = nbt.getBoolean("hasTarget");

        if (nbt.hasKey("inventory")) {
            inventory = new ItemStack(nbt.getCompoundTag("inventory"));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setShort("xTarget", (short) xTarget);
        nbt.setShort("zTarget", (short) zTarget);
        nbt.setShort("fireTicks", (short) fireTicks);

        nbt.setBoolean("hasTarget", hasTarget);

        if (inventory != null) {
            NBTTagCompound nbttagcompound1 = new NBTTagCompound();
            inventory.writeToNBT(nbttagcompound1);
            nbt.setTag("inventory", nbttagcompound1);
        }

        return nbt;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = super.getUpdateTag();

        access.setInteger("fireTicks", fireTicks);
        access.setInteger("xTarget", xTarget);
        access.setInteger("zTarget", zTarget);

        return new SPacketUpdateTileEntity(pos, 1, access);
    }


    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        xTarget = access.getInteger("xTarget");
        zTarget = access.getInteger("zTarget");
        fireTicks = access.getInteger("fireTicks");
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == TransportationModule.ITEM_MORTAR;
    }

    @Override
    public void safeUpdate() {
        if (!world.isRemote) {
            BlockPos thisPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
            ItemStack stackInSlotZero = getStackInSlot(0);
            if ((stackInSlotZero != null && world.canBlockSeeSky(thisPos)) || fireTicks >= 60) {
                ItemStack stack = null;
                if (fireTicks < 60 && stackInSlotZero != null) {
                    stack = stackInSlotZero.copy();
                }
                if (getSteamShare() > 2000 && hasTarget) {
                    if (fireTicks == 0) {
                        markForResync();
                    }
                    fireTicks++;
                    if (fireTicks == 10) {
                        world.playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                          EsteemedInnovation.SOUND_HISS, SoundCategory.BLOCKS, Blocks.ANVIL.getSoundType().getVolume(), 0.9F, false);
                    }
                    if (fireTicks == 60 && stack != null) {
                        world.playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
                          EsteemedInnovation.SOUND_CANNON, SoundCategory.BLOCKS, 2F, 0.8F, false);
                        decrSteam(2000);
                        ItemStack stack2 = stack.copy();
                        stack2.setCount(1);
                        EntityMortarItem entityItem = new EntityMortarItem(world, pos.getX() + 0.5F,
                          pos.getY() + 1.25F, pos.getZ() + 0.5F, stack2, xTarget, zTarget);
                        world.spawnEntity(entityItem);
                        entityItem.motionY = 1.0F;
                        if (stack.getCount() > 1) {
                            stack.shrink(1);
                            setInventorySlotContents(0, stack);
                        } else {
                            setInventorySlotContents(0, null);
                        }
                    }
                    if (fireTicks == 80) {
                        fireTicks = 0;
                    }
                } else {
                    fireTicks = 0;
                    markForResync();
                }
            } else {
                fireTicks = 0;
                markForResync();
            }
        } else {
            if (fireTicks > 0) {
                fireTicks++;
                if (fireTicks == 80) {
                    fireTicks = 0;
                }
            }
        }

        super.safeUpdate();
    }

    @Override
    public int getSizeInventory() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return this.inventory;
    }

    @Override
    public ItemStack decrStackSize(int slot, int var2) {
        if (inventory != null) {
            ItemStack itemstack;

            if (inventory.getCount() <= var2) {
                itemstack = inventory;
                inventory = null;
                return itemstack;
            } else {
                itemstack = inventory.splitStack(var2);

                if (inventory.isEmpty()) {
                    inventory = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return inventory;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory = stack;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public ITextComponent getDisplayName() {
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer var1) {
        return true;
    }

    @Override
    public void openInventory(EntityPlayer player) {
    }

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int var1, ItemStack var2) {
        return true;
    }

    @Override
    public int getField(int id) {
        switch (id) {
            case 0: {
                return fireTicks;
            }
            case 1: {
                return xTarget;
            }
            case 2: {
                return zTarget;
            }
            default: {
                return 0;
            }
        }
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case 0: {
                fireTicks = value;
                return;
            }
            case 1: {
                xTarget = value;
                return;
            }
            case 2: {
                zTarget = value;
                return;
            }
            default: {}
        }
    }

    @Override
    public int getFieldCount() {
        return 3;
    }

    @Override
    public void clear() {
        inventory = null;
    }

    @Override
    public boolean isEmpty() {
        // TODO
        return inventory == null;
    }
}
