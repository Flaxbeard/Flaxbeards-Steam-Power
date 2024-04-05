package eiteam.esteemedinnovation.api.util;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public record Coord4(BlockPos pos, ResourceLocation dimension) {
    public static Coord4 readFromNBT(CompoundTag nbt) {
        // todo: tag constants
        int x = nbt.getInt("X");
        int y = nbt.getInt("Y");
        int z = nbt.getInt("Z");
        ResourceLocation d = new ResourceLocation(nbt.getString("Dimension"));
        BlockPos pos = new BlockPos(x, y, z);
        return new Coord4(pos, d);
    }

    public CompoundTag writeToNBT(CompoundTag nbt) {
        nbt.putInt("X", pos.getX());
        nbt.putInt("Y", pos.getY());
        nbt.putInt("Z", pos.getZ());
        nbt.putString("Dimension", dimension.toString());

        return nbt;
    }

    public BlockEntity getBlockEntity(BlockGetter world) {
        return world.getBlockEntity(pos);
    }

    public Block getBlock(BlockGetter world) {
        return getBlockState(world).getBlock();
    }

    public BlockState getBlockState(BlockGetter world) {
        return world.getBlockState(pos);
    }
}