package eiteam.esteemedinnovation.thumper;

import eiteam.esteemedinnovation.api.block.BlockSteamTransporter;
import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import eiteam.esteemedinnovation.commons.util.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import static eiteam.esteemedinnovation.thumper.ThumperModule.THUMPER_DUMMY;

public class BlockThumper extends BlockSteamTransporter implements Wrenchable {
    public static final PropertyDirection FACING = BlockHorizontal.FACING;
    // Because the defaults variant cannot have submodels :|
    private static final PropertyBool ALWAYS_TRUE = PropertyBool.create("always_true");

    public BlockThumper() {
        super(Material.IRON);
        setHardness(3.5F);
    }

    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ALWAYS_TRUE);
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
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return state.withProperty(ALWAYS_TRUE, true);
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block, BlockPos fromPos) {
        BlockPos above = pos.up();
        TileEntityThumper tile = (TileEntityThumper) world.getTileEntity(pos);
        if (world.getBlockState(above).getBlock() != THUMPER_DUMMY && tile != null) {
            if (!world.isRemote) {
                dropBlockAsItem(world, pos, getDefaultState(), 0);
            }
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        // No need to break the rest of the Thumper dummies as that is handled by BlockThumperDummy#breakBlock.
        BlockPos dummyPos = pos.up();
        if (world.getBlockState(dummyPos).getBlock() == THUMPER_DUMMY) {
            world.setBlockToAir(dummyPos);
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase elb, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, elb.getHorizontalFacing().getOpposite()), 2);
        for (int y = 1; y < 4; y++) {
            BlockPos dummyPos = pos.up(y);
            world.setBlockState(dummyPos, THUMPER_DUMMY.getDefaultState());
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityThumper();
    }

    @Override
    public boolean onWrench(ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
        if (facing.getAxis() == EnumFacing.Axis.Y) {
            return false;
        }
        WorldHelper.rotateProperly(FACING, level, state, pos, facing);
        return true;
    }

    @Override
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }
}
