package eiteam.esteemedinnovation.api.wrench;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

/**
 * Implement this interface on subclasses of Item to have the item work as pipe wrench.
 */
public interface PipeWrench {
    /**
     * Called to ensure the pipe wrench can be used on a block.
     * @param player The player wrenching
     * @param pos The position of the block being wrenched
     * @return true if wrenching is possible; false if not.
     */
    boolean canWrench(Player player, BlockPos pos);
}
