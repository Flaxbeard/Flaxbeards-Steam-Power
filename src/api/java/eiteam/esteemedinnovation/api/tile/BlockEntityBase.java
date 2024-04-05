package eiteam.esteemedinnovation.api.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base block entity class, mainly so we can have handy helper methods across all of our BEs.
 */
public class BlockEntityBase extends BlockEntity {
    public BlockEntityBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    /**
     * Marks the block entity for a server to client resync, with a specific old state.
     * @param old The old state
     */
    public void markForResync(BlockState old) {
        level.sendBlockUpdated(getBlockPos(), old, level.getBlockState(getBlockPos()), 0);
    }

    /**
     * Marks the block entity for a server to client resync, using the same blockstate for both the "newState" and "oldState" parameters.
     */
    public void markForResync() {
        markForResync(level.getBlockState(getBlockPos()));
    }

    /**
     * Like the old TileEntity#markDirty method.
     */
    public void markDirty() {
        setChanged();
        if (level instanceof ServerLevel sl) {
            sl.getChunkSource().blockChanged(worldPosition);
        }
    }

    /**
     * Gets the current BlockPos offset by the Direction.
     * @param facing The direction
     */
    protected BlockPos getRelativePos(Direction facing) {
        return getBlockPos().relative(facing);
    }
}
