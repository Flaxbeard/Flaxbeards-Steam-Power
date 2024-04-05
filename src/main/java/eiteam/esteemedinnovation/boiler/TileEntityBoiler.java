package eiteam.esteemedinnovation.boiler;

import eiteam.esteemedinnovation.api.block.DisguisableBlock;
import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.api.util.FluidHelper;
import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import eiteam.esteemedinnovation.commons.util.OreDictHelper;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;

public class TileEntityBoiler extends SteamTransporterBlockEntity implements Wrenchable, DisguisableBlock {
    public ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
            if (slot == 0) {
                return getItemBurnTime(stack) > 0;
            } else {
                return slot == 1 && FluidHelper.itemStackIsWaterContainer(stack);
            }
        }

        @Override
        protected void onContentsChanged(int slot) {
            TileEntityBoiler.this.markDirty();
        }
    };
    private FluidTank myTank = new FluidTank(new FluidStack(FluidHelper.getWaterFluid(), 0), 10000);
    public int cookTime;
    public int burnTime;
    public int currentItemBurnTime;
    private Block disguiseBlock;
    private int disguiseMeta;
    private String customName;
    private boolean wasBurning;

    public TileEntityBoiler(int capacity) {
        super(capacity, new EnumFacing[] { EnumFacing.UP });
        addSideToGaugeBlacklist(EnumFacing.UP);
        setPressureResistance(0.5F);
    }

    public TileEntityBoiler() {
        this(50000);
    }

    public static int getItemBurnTime(@Nonnull ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        int burnTime = ForgeEventFactory.getItemBurnTime(stack);
        if (burnTime >= 0) {
            return burnTime;
        }
        if (item instanceof ItemBlock && Block.getBlockFromItem(item) != Blocks.AIR) {
            Block block = Block.getBlockFromItem(item);

            if (OreDictHelper.slabWoods.contains(item)) {
                burnTime = 150;
            }

            if (block.getDefaultState().getMaterial() == Material.WOOD) {
                burnTime =  300;
            }

            if (OreDictHelper.blockCoals.contains(item)) {
                burnTime =  16000;
            }
        }

        if (item instanceof ItemTool && "WOOD".equals(((ItemTool) item).getToolMaterialName())) {
            burnTime = 200;
        }
        if (item instanceof ItemSword && "WOOD".equals(((ItemSword) item).getToolMaterialName())) {
            burnTime = 200;
        }
        if (item instanceof ItemHoe && "WOOD".equals(((ItemHoe) item).getMaterialName())) {
            burnTime = 200;
        }
        if (OreDictHelper.listHasItem(OreDictHelper.sticks, item)) {
            burnTime =  100;
        }
        if (item == Items.COAL) {
            burnTime =  1600;
        }
        if (item == Items.LAVA_BUCKET) {
            burnTime = 20000;
        }
        if (OreDictHelper.saplings.contains(item)) {
            burnTime = 100;
        }
        if (item == Items.BLAZE_ROD) {
            burnTime = 2400;
        }
        return burnTime;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = getUpdateTag();
        access.setInteger("WaterStored", myTank.getFluidAmount());
        access.setShort("BurnTime", (short) burnTime);
        access.setShort("CookTime", (short) cookTime);
        access.setShort("CurrentItemBurnTime", (short) currentItemBurnTime);
        access.setInteger("DisguiseBlock", Block.getIdFromBlock(disguiseBlock));
        access.setInteger("DisguiseMetadata", disguiseMeta);

        return new SPacketUpdateTileEntity(pos, 1, access);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        myTank.setFluid(new FluidStack(FluidHelper.getWaterFluid(), access.getInteger("WaterStored")));
        burnTime = access.getShort("BurnTime");
        currentItemBurnTime = access.getShort("CurrentItemBurnTime");
        cookTime = access.getShort("CookTime");
        disguiseBlock = Block.getBlockById(access.getInteger("DisguiseBlock"));
        disguiseMeta = access.getInteger("DisguiseMetadata");
        markForResync();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        inventory.deserializeNBT(nbt.getCompoundTag("Items"));
        burnTime = nbt.getShort("BurnTime");
        cookTime = nbt.getShort("CookTime");
        currentItemBurnTime = nbt.getShort("CurrentItemBurnTime");

        if (nbt.hasKey("CustomName")) {
            customName = nbt.getString("CustomName");
        }

        if (nbt.hasKey("WaterStored")) {
            myTank.setFluid(new FluidStack(FluidHelper.getWaterFluid(), nbt.getShort("WaterStored")));
        }
        disguiseBlock = Block.getBlockById(nbt.getInteger("DisguiseBlock"));
        disguiseMeta = nbt.getInteger("DisguiseMetadata");
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("Items", inventory.serializeNBT());
        nbt.setShort("BurnTime", (short) burnTime);
        nbt.setShort("WaterStored", (short) myTank.getFluidAmount());
        nbt.setShort("CookTime", (short) cookTime);
        nbt.setShort("CurrentItemBurnTime", (short) currentItemBurnTime);
        nbt.setInteger("DisguiseBlock", Block.getIdFromBlock(disguiseBlock));
        nbt.setInteger("DisguiseMetadata", disguiseMeta);

        if (hasCustomName()) {
            nbt.setString("CustomName", customName);
        }

        return nbt;
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == BoilerModule.BOILER;
    }

    @Override
    public void safeUpdate() {
        /*
        if (worldObj.isRemote) {
            boolean hasWrench = BlockSteamPipeRenderer.updateWrenchStatus();
            if (hasWrench != lastWrench && !(disguiseBlock == null || disguiseBlock == Blocks.AIR)) {
                super.markForResync();
            }
            lastWrench = hasWrench;
        }
        */

        ItemStack stackInInput = inventory.getStackInSlot(1);
        if (FluidHelper.itemStackIsWaterContainer(stackInInput)) {
            ItemStack drainedItemStack = FluidHelper.fillTankFromItem(stackInInput, myTank, true);
            inventory.setStackInSlot(1, drainedItemStack);
        }

        boolean isBurnTimeGreaterThanZero = burnTime > 0;
        if (burnTime > 0) {
            //maxThisTick = Math.min(burnTime, 10);
            burnTime -= 1; //maxThisTick
        }
        if (burnTime < 0) {
            burnTime = 0;
        }


        if (!world.isRemote) {
            if (burnTime == 0 && canSmelt()) {
                ItemStack stack = inventory.getStackInSlot(0);
                currentItemBurnTime = burnTime = getItemBurnTime(stack);
                if (burnTime > 0) {
                    ItemStack container = stack.getItem().getContainerItem(stack);
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        inventory.setStackInSlot(0, container);
                    }
                }

                //worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            }

            if (isBurning() && canSmelt() && getNetwork() != null) {
                ++cookTime;

                if (cookTime > 0) {
                    //int i = 0;
                    //while (i<maxThisTick && isBurning() && canSmelt()) {
                    getNetwork().addSteam(10);
                    myTank.drain(2, true);
                    ///i++;
                    //}
                    cookTime = 0;
                }
            } else {
                cookTime = 0;
            }

            if (isBurnTimeGreaterThanZero != burnTime > 0) {
                markForResync();
            }
        }

        if (wasBurning != isBurning()) {
            wasBurning = isBurning();
            markForResync();
        }

        super.safeUpdate();
    }

    private boolean canSmelt() {
        return myTank.getFluidAmount() > 9;
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
        return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(myTank);
        }
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        }
        return super.getCapability(capability, facing);
    }

    @Nonnull
    @Override
    public String getName() {
        return hasCustomName() ? customName : "container.furnace";
    }

    public boolean hasCustomName() {
        return customName != null && !customName.isEmpty();
    }

    @Nonnull
    @Override
    public ITextComponent getDisplayName() {
        String name = getName();
        return (hasCustomName() ? new TextComponentString(name) : new TextComponentTranslation(name));
    }

    public int getPressureAsInt() {
        return (int) Math.floor((double) getPressure() * 1000);
    }

    @SideOnly(Side.CLIENT)
    public int getBurnTimeRemainingScaled(int pixels) {
        if (currentItemBurnTime == 0) {
            currentItemBurnTime = 200;
        }

        return burnTime * pixels / currentItemBurnTime;
    }

    public FluidTank getTank() {
        return myTank;
    }

    @Override
    public boolean onWrench(@Nonnull ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            Block disguiseBlock = getDisguiseBlock();
            if (disguiseBlock == null) {
                return false;
            }
            if (!player.capabilities.isCreativeMode) {
                EntityItem entityItem = new EntityItem(level, player.posX, player.posY, player.posZ, new ItemStack(disguiseBlock, 1, getDisguiseMeta()));
                level.spawnEntity(entityItem);
            }
            SoundType sound = disguiseBlock.getSoundType(null, level, pos, player);
            level.playSound((pos.getX() + 0.5F), (pos.getY() + 0.5F),
              (pos.getZ() + 0.5F), sound.getBreakSound(), SoundCategory.BLOCKS,
              (sound.getVolume() + 1F) / 2F, sound.getPitch() * 0.8F, false);
            setDisguiseBlock(null);

            markForResync();
        }
        return true;
    }

    @Override
    public Block getDisguiseBlock() {
        return disguiseBlock;
    }

    @Override
    public void setDisguiseBlock(Block block) {
        disguiseBlock = block;
    }

    @Override
    public int getDisguiseMeta() {
        return disguiseMeta;
    }

    @Override
    public void setDisguiseMeta(int meta) {
        disguiseMeta = meta;
    }
}
