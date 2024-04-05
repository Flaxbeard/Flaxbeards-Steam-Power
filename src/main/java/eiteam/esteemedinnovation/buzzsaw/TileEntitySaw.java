package eiteam.esteemedinnovation.buzzsaw;

import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.commons.util.OreDictHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;

public class TileEntitySaw extends SteamTransporterBlockEntity {
    private static final int CONSUMPTION = 10;
    private static final int PASSIVE_CONSUMPTION = 1;
    private static final int WINDUP_TICKS_MAX = 50;
    private static final String WINDUP_TICKS_KEY = "WindupTicks";
    private boolean isWoundUp;
    private int windupTicks;

    public TileEntitySaw() {
        super(new EnumFacing[] { EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH });
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == BuzzsawModule.BUZZSAW;
    }

    @Override
    public void initialUpdate() {
        super.initialUpdate();
        EnumFacing dir = world.getBlockState(pos).getValue(BlockSaw.FACING);
        addSideToGaugeBlacklist(dir);
        setValidDistributionDirectionsExcluding(dir, EnumFacing.UP);
    }

    @Override
    public void safeUpdate() {
        // Redstone to turn it off.
        if (getSteamShare() < CONSUMPTION || world.isBlockPowered(pos)) {
            resetWinding();
            return;
        }

        // Passive consumption of steam in order to keep the blades rotating.
        decrSteam(PASSIVE_CONSUMPTION);

        if (!isWoundUp) {
            if (windupTicks == WINDUP_TICKS_MAX) {
                isWoundUp = true;
            } else {
                windupTicks++;
                return;
            }
        }

        BlockPos woodPos = getRelativePos(world.getBlockState(pos).getValue(BlockSaw.FACING));
        IBlockState woodState = world.getBlockState(woodPos);
        Block woodBlock = woodState.getBlock();
        Item woodItem = Item.getItemFromBlock(woodBlock);
        if (woodItem == Items.AIR) {
            return;
        }
        int meta = woodBlock.damageDropped(woodState);
        Pair<Item, Integer> pair = Pair.of(woodItem, meta);
        ItemStack output = ItemStack.EMPTY;
        // If the block is a plankWood, output sticks; if the block is a logWood, output planks.
        if (OreDictHelper.arrayContains(OreDictHelper.planks, pair)) {
            output = new ItemStack(Items.STICK, world.rand.nextInt(2) + 2);
        } else {
            Pair<Item, Integer> outPair = OreDictHelper.getLogToPlankOutPair(woodItem, meta, world);
            if (outPair != null) {
                output = new ItemStack(outPair.getLeft(), world.rand.nextInt(3) + 4, outPair.getRight());
            }
        }

        if (!output.isEmpty()) {
            world.destroyBlock(woodPos, false);
            world.spawnEntity(new EntityItem(world, woodPos.getX() + 0.5, woodPos.getY() + 0.5, woodPos.getZ() + 0.5, output));
            if (!world.isRemote) {
                decrSteam(CONSUMPTION);
            }
        }

        super.safeUpdate();
    }

    void uninitialize() {
        setInitialized(false);
    }

    private void resetWinding() {
        if (windupTicks != 0) {
            windupTicks--;
        }
        isWoundUp = false;
    }

    @Override
    public void readFromNBT(NBTTagCompound access) {
        super.readFromNBT(access);
        windupTicks = access.getInteger(WINDUP_TICKS_KEY);
    }

    @Nonnull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound access) {
        super.writeToNBT(access);
        access.setInteger(WINDUP_TICKS_KEY, windupTicks);
        return access;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = writeToNBT(getUpdateTag());
        return new SPacketUpdateTileEntity(pos, 1, access);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        readFromNBT(pkt.getNbtCompound());
        markForResync();
    }
}
