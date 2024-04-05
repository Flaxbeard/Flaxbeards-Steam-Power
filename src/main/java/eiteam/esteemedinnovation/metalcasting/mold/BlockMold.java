package eiteam.esteemedinnovation.metalcasting.mold;

import eiteam.esteemedinnovation.api.mold.CrucibleMold;
import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import eiteam.esteemedinnovation.commons.util.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BlockMold extends Block implements Wrenchable {
    private static final float px = (1.0F / 16.0F);
    public static PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockMold() {
        super(Material.ROCK);
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
    public IBlockState withRotation(IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntityMold tileentitymold = (TileEntityMold) world.getTileEntity(pos);

        if (tileentitymold != null) {
            InventoryHelper.dropInventoryItems(world, pos, tileentitymold);
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), tileentitymold.mold);
            world.updateComparatorOutputLevel(pos, state.getBlock());
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityMold();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase elb, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, elb.getHorizontalFacing().getOpposite()), 2);
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(2 * px, 0.0F, 2 * px, 1.0F - 2 * px, 1.0F - 8 * px, 1.0F - 2 * px);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntityMold tile = (TileEntityMold) world.getTileEntity(pos);
        if (tile == null) {
            return false;
        }
        ItemStack heldItem = player.getHeldItem(hand);
        boolean editingMold = heldItem.getItem() instanceof CrucibleMold || player.isSneaking();
        if (editingMold) {
            if (tile.isOpen) {
                if (!tile.mold.isEmpty()) {
                    if (!world.isRemote) {
                        if (!player.capabilities.isCreativeMode) {
                            tile.dropItem(tile.mold);
                        }
                    }
                    tile.mold = ItemStack.EMPTY;
                    // markDirty
                }
                if (heldItem.getItem() instanceof CrucibleMold) {
                    tile.mold = heldItem;
                    if (!player.capabilities.isCreativeMode) {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, ItemStack.EMPTY);
                    }
                    // markDirty

                }
            }
            return true;
        } else {
            if (tile.changeTicks == 0 && (!(heldItem.getItem() instanceof ItemBlock))) {
                tile.isOpen = !tile.isOpen;
                tile.changeTicks = 20;
                // markDirty
                return true;
            }
        }

        return false;
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
}
