package eiteam.esteemedinnovation.api.tile;

import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Collection;
import java.util.List;

/**
 * An adjacent block modifier for the Thumper is a Tile Entity which modifies the behavior of the Thumper when it
 * is adjacent.
 * <p>
 * ThumperAdjacentBehaviorModifiers are expected to be Tile Entities.
 */
public interface ThumperAdjacentBehaviorModifier {
    /**
     * Called when the Thumper is harvesting a block. The block has not been turned into air (broken) at this point.
     * @param thumper The Thumper that is harvesting the block.
     * @param drops All of the drops for this block. You must remove drops you process from this list, otherwise
     *              you run the risk of creating a duping bug.
     * @param state The block state currently being harvested by the Thumper.
     * @param allBehaviorModifiers A list of all of the adjacent behavior modifiers for this Thumper, including this.
     * @param directionIn The direction from the Thumper that this behavior modifier was found in.
     */
    void dropItems(SteamTransporterBlockEntity thumper, List<ItemStack> drops, BlockState state, Collection<ThumperAdjacentBehaviorModifier> allBehaviorModifiers, Direction directionIn);

    /**
     * Called to ensure that this behavior modifier is actually valid. Use this to make sure that it is configured properly.
     * @param thumper The Thumper that is being checked.
     * @param directionIn {@inheritDoc}
     * @return Whether this behavior modifier can be used to modify Thumper behavior.
     */
    boolean isValidBehaviorModifier(SteamTransporterBlockEntity thumper, Direction directionIn);
}
