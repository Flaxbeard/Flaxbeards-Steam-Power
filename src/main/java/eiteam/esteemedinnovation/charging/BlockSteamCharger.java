package eiteam.esteemedinnovation.charging;

import eiteam.esteemedinnovation.api.SteamChargable;
import eiteam.esteemedinnovation.api.block.BlockSteamTransporter;
import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import eiteam.esteemedinnovation.commons.util.WorldHelper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockSteamCharger extends BlockSteamTransporter implements Wrenchable {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    private static final AxisAlignedBB CHARGER_AABB = new AxisAlignedBB(0, 0, 0, 1, 1F / 2F, 1);

    public BlockSteamCharger() {
        super(Material.IRON);
        setHardness(3.5F);
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return CHARGER_AABB;
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase elb, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, elb.getHorizontalFacing().getOpposite()));
    }

    /**
     * Whether the item is chargeable (and can be charged) or it is an empty cell item.
     * @param item The ItemStack to check.
     * @return Whether the item can be placed in the TileEntity's inventory.
     */
    private boolean canItemBeCharged(ItemStack item) {
        if (item == null) {
            return false;
        }
        if (item.getItem() instanceof SteamChargable) {
            return ((SteamChargable) item.getItem()).canCharge(item);
        } else {
            /*
            if (CrossMod.TINKERS_CONSTRUCT && item.getItem() instanceof ToolCore) {
                NBTTagCompound nbt = item.getTagCompound();
                return nbt.getCompoundTag("InfiTool").hasKey("Steam");
            }
            */
            return item.getItem() == ChargingModule.STEAM_CELL_EMPTY;
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntitySteamCharger tile = (TileEntitySteamCharger) world.getTileEntity(pos);
        if (tile == null) {
            return false;
        }
        ItemStack stackInSlot = tile.getStackInSlot(0);
        if (stackInSlot != null) {
            if (!world.isRemote) {
                tile.dropItem(stackInSlot);
            }
            tile.setInventorySlotContents(0, null);
            return true;
        } else {
            ItemStack heldItem = player.getHeldItem(hand);
            if (canItemBeCharged(heldItem)) {
                ItemStack copy = heldItem.copy();
                copy.setCount(1);
                tile.setInventorySlotContents(0, copy);
                heldItem.shrink(1);
                tile.randomDegrees = world.rand.nextInt(361);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntitySteamCharger();
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntitySteamCharger tileentitysteamcharger = (TileEntitySteamCharger) world.getTileEntity(pos);

        if (tileentitysteamcharger != null) {
            InventoryHelper.dropInventoryItems(world, pos, tileentitysteamcharger);
            world.updateComparatorOutputLevel(pos, state.getBlock());
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onWrench(ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
        if (facing != EnumFacing.DOWN && facing != EnumFacing.UP) {
            WorldHelper.rotateProperly(FACING, level, state, pos, facing);
            return true;
        }
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}