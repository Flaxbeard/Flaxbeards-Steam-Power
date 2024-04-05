package eiteam.esteemedinnovation.thumper;

import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Random;

import static eiteam.esteemedinnovation.thumper.ThumperModule.THUMPER;

public class BlockThumperDummy extends Block implements Wrenchable {
    public BlockThumperDummy() {
        super(Material.IRON);
        setHardness(3.5F);
    }

    @Override
    public int quantityDropped(Random rand) {
        return 0;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        Item item = Item.getItemFromBlock(THUMPER);
        return item == null ? null : new ItemStack(item, 1, 0);
    }

    @Override
    public boolean onWrench(ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
        if (facing.getAxis() == EnumFacing.Axis.Y) {
            return false;
        }
        for (int i = 1; i < 4; i++) {
            BlockPos blockPos = pos.down(i);
            IBlockState blockState = level.getBlockState(blockPos);
            Block block = blockState.getBlock();
            if (block == THUMPER) {
                ((BlockThumper) block).onWrench(stack, player, level, blockPos, hand, facing, blockState, hitX, hitY, hitZ);
                return true;
            }
        }
        return false;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        // No need to break the Thumper itself because that is handled by BlockThumper#neighborChanged.
        for (int i = -1; i < 1; i++) {
            BlockPos next = pos.up(i);
            if (world.getBlockState(next).getBlock() == this) {
                world.setBlockToAir(next);
            }
        }

        super.breakBlock(world, pos, state);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.INVISIBLE;
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
