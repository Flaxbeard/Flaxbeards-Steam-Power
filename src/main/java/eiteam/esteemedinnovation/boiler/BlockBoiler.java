package eiteam.esteemedinnovation.boiler;

import eiteam.esteemedinnovation.api.block.BlockSteamTransporter;
import eiteam.esteemedinnovation.api.util.FluidHelper;
import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import eiteam.esteemedinnovation.commons.util.WorldHelper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Random;

public class BlockBoiler extends BlockSteamTransporter implements Wrenchable {
    public static final PropertyBool IS_ON = PropertyBool.create("on");
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    BlockBoiler() {
        super(Material.IRON);
        setHardness(5F);
        setResistance(10F);

        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(IS_ON, false));
    }

    @Nonnull
    @Override
    public BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, IS_ON);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Nonnull
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
    }

    @Nonnull
    @Override
    public IBlockState getActualState(@Nonnull IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = WorldHelper.getTileEntitySafely(world, pos);
        if (tile instanceof TileEntityBoiler) {
            TileEntityBoiler boiler = (TileEntityBoiler) tile;
            return getDefaultState().withProperty(FACING, state.getValue(FACING)).withProperty(IS_ON, boiler.isBurning());
        }
        return state;
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
    }

    @Nonnull
    @Override
    public IBlockState withRotation(@Nonnull IBlockState state, Rotation rot) {
        return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Nonnull
    @Override
    public IBlockState withMirror(@Nonnull IBlockState state, Mirror mirrorIn) {
        return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityBoiler)) {
            return;
        }
        TileEntityBoiler boiler = (TileEntityBoiler) tile;
        if (boiler.isBurning()) {
            EnumFacing facing = state.getValue(FACING);
            float f = pos.getX() + 0.5F;
            float f1 = pos.getY() + 0.0F + rand.nextFloat() * 6.0F / 16.0F;
            float f2 = pos.getZ() + 0.5F;
            float f3 = 0.52F;
            float f4 = rand.nextFloat() * 0.6F - 0.3F;

            double xCoord;
            double zCoord;

            switch (facing) {
                case WEST: {
                    xCoord = (double) f - f3;
                    zCoord = (double) f2 + f4;
                    break;
                }
                case EAST: {
                    xCoord = (double) f + f3;
                    zCoord = (double) f2 + f4;
                    break;
                }
                case NORTH: {
                    xCoord = (double) f + f4;
                    zCoord = (double) f2 - f3;
                    break;
                }
                case SOUTH: {
                    xCoord = (double) f + f4;
                    zCoord = (double) f2 + f3;
                    break;
                }
                default: {
                    return;
                }
            }

            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xCoord, f1, zCoord, 0D, 0D, 0D);
            world.spawnParticle(EnumParticleTypes.FLAME, xCoord, f1, zCoord, 0D, 0D, 0D);
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(@Nonnull World world, @Nonnull IBlockState state) {
        return new TileEntityBoiler();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntityBoiler tileEntity = (TileEntityBoiler) world.getTileEntity(pos);

        boolean isClient = !world.isRemote;

        if (tileEntity != null) {
            if (!FluidHelper.playerIsHoldingWaterContainer(player) && isClient) {
                player.openGui(EsteemedInnovation.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
            } else {
                FluidHelper.fillTankFromItem(player.getHeldItem(hand), tileEntity.getTank(), !player.capabilities.isCreativeMode);
            }
        }

        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tileentity = world.getTileEntity(pos);

        if (tileentity instanceof TileEntityBoiler) {
            TileEntityBoiler boiler = (TileEntityBoiler) tileentity;
            for (int i = 0; i < boiler.inventory.getSlots(); i++) {
                EntityItem entityItem = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), boiler.inventory.getStackInSlot(i));
                world.spawnEntity(entityItem);
            }
            world.updateComparatorOutputLevel(pos, state.getBlock());
        }

        super.breakBlock(world, pos, state);
    }

    @Nonnull
    @Override
    public ItemStack getPickBlock(@Nonnull IBlockState state, RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, EntityPlayer player) {
        return new ItemStack(BoilerModule.BOILER);
    }

    @Override
    public boolean onWrench(@Nonnull ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            return true;
        }
        if (facing != EnumFacing.DOWN && facing != EnumFacing.UP) {
            WorldHelper.rotateProperly(FACING, level, state, pos, facing);
        }
        return false;
    }
}
