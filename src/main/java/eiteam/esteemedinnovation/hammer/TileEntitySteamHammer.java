package eiteam.esteemedinnovation.hammer;

import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySteamHammer extends SteamTransporterBlockEntity implements IInventory {
    public int hammerTicks = 0;
    public String itemName = "";
    public int cost = 0;
    public int progress = 0;
    private boolean isInitialized = false;
    private boolean isWorking = false;
    private boolean hadItem = false;
    private ItemStack[] inventory = new ItemStack[3];
    private static final int CONSUMPTION = HammerModule.hammerConsumption;
    private static final float VOLUME = Blocks.ANVIL.getSoundType().getVolume();

    public TileEntitySteamHammer() {
        super(EnumFacing.HORIZONTALS);
        addSidesToGaugeBlacklist(EnumFacing.HORIZONTALS);
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == HammerModule.STEAM_HAMMER;
    }

    @Override
    public void safeUpdate() {
        EnumFacing dir = myDir();
        if (!isInitialized) {

            EnumFacing[] dirs = {dir.getOpposite()};
            setDistributionDirections(dirs);
            isInitialized = true;
        }
        if (world.isRemote) {
            if (isWorking) {
                if (cost > 0 && progress < cost && hammerTicks == 355) {
                    progress++;
                }
                hammerTicks = (hammerTicks + 5) % 360;
                if (hammerTicks == 20) {
                    for (int i = 0; i < 5; i++) {
                        EsteemedInnovation.proxy.spawnBreakParticles(world,
                          pos.getX() + 0.5F + 0.25F * dir.getXOffset(), pos.getY(),
                          pos.getZ() + 0.5F + 0.25F * dir.getZOffset(), Blocks.ANVIL,
                          (float) (Math.random() - 0.5F) / 12F, 0F, (float) (Math.random() - 0.5F) / 12F
                        );
                    }
                }
            } else {
                hammerTicks = 0;
                progress = 0;
            }
        } else {
            if (getStackInSlot(0) != null) {
                if (!hadItem) {
                    hadItem = true;
                    markForResync();
                }
            } else if (hadItem) {
                hadItem = false;
                markForResync();
            }
            if (cost > 0 && progress < cost && hammerTicks == 355) {
                progress++;
            }
            if (cost > 0 && progress < cost && getStackInSlot(2) != null) {
                if (hammerTicks == 0) {
                    if (getSteamShare() >= CONSUMPTION) {
                        decrSteam(CONSUMPTION);
                        if (!isWorking) {
                            isWorking = true;
                            markForResync();
                        }

                    } else {
                        if (isWorking) {
                            isWorking = false;
                            markForResync();
                        }
                        return;
                    }
                }
                hammerTicks = (hammerTicks + 5) % 360;
                switch (hammerTicks) {
                    case 15:
                    case 40: {
                        playHammerSound(SoundEvents.BLOCK_ANVIL_LAND, (float) (0.75 + (Math.random() * 0.1F)));
                        break;
                    }
                    case 170: {
                        playHammerSound(EsteemedInnovation.SOUND_HISS, 0.9F);
                        break;
                    }
                    default: {
                        break;
                    }
                }
            } else {
                if (isWorking) {
                    isWorking = false;
                    markForResync();
                }
                if (hammerTicks > 0) {
                    hammerTicks = 0;
                }
            }
        }

        super.safeUpdate();
    }

    private void playHammerSound(SoundEvent sound, float volume, float pitch) {
        this.world.playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F,
          sound, SoundCategory.BLOCKS, volume, pitch, false);
    }

    private void playHammerSound(SoundEvent sound, float pitch) {
        playHammerSound(sound, VOLUME, pitch);
    }

    private EnumFacing myDir() {
        return world.getBlockState(pos).getValue(BlockSteamHammer.FACING);
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory[index];
    }

    @Override
    public ItemStack decrStackSize(int index, int count) {
        if (inventory[index] != null) {
            ItemStack itemstack;

            if (inventory[index].getCount() <= count) {
                itemstack = inventory[index];
                inventory[index] = null;
                return itemstack;
            } else {
                itemstack = inventory[index].splitStack(count);

                if (inventory[index].isEmpty()) {
                    inventory[index] = null;
                }

                return itemstack;
            }
        } else {
            return null;
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        if (inventory[index] != null) {
            ItemStack itemstack = inventory[index];
            inventory[index] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventory[index] = stack;

        if (stack != null && stack.getCount() > getInventoryStackLimit()) {
            stack.setCount(getInventoryStackLimit());
        }
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this &&
          player.getDistanceSq((double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D, (double) pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = super.getUpdateTag();

        access.setInteger("cost", cost);
        access.setInteger("progress", progress);
        access.setInteger("hammerTicks", hammerTicks);
        access.setBoolean("isWorking", this.isWorking);

        return new SPacketUpdateTileEntity(pos, 1, access);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        cost = access.getInteger("cost");
        isWorking = access.getBoolean("isWorking");
        progress = access.getInteger("progress");
        hammerTicks = access.getInteger("hammerTicks");

        markForResync();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList nbttaglist = (NBTTagList) nbt.getTag("Items");
        inventory = new ItemStack[getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            byte b0 = nbttagcompound1.getByte("Slot");

            if (b0 >= 0 && b0 < inventory.length) {
                inventory[b0] = new ItemStack(nbttagcompound1);
            }
        }

        cost = nbt.getInteger("cost");
        progress = nbt.getInteger("progress");
        hammerTicks = nbt.getInteger("hammerTicks");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("cost", cost);
        nbt.setInteger("progress", progress);
        nbt.setInteger("hammerTicks", hammerTicks);

        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < inventory.length; ++i) {
            if (inventory[i] != null) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        nbt.setTag("Items", nbttaglist);

        return nbt;
    }

    @Override
    public int getField(int id) {
        switch (id) {
            case 0: {
                return hammerTicks;
            }
            case 1: {
                return cost;
            }
            case 2: {
                return progress;
            }
        }
        return 0;
    }

    @Override
    public void setField(int id, int value) {
        switch (id) {
            case 0: {
                hammerTicks = value;
                break;
            }
            case 1: {
                cost = value;
                break;
            }
            case 2: {
                progress = value;
                break;
            }
        }
    }

    @Override
    public int getFieldCount() {
        return 3;
    }

    @Override
    public void clear() {
        for (int i = 0; i < getSizeInventory(); i++) {
            inventory[i] = null;
        }
    }

    @Override
    public boolean isEmpty() {
        // TODO: Rewrite completely
        for (ItemStack stack : inventory) {
            if (stack != null) {
                return false;
            }
        }
        return true;
    }
}
