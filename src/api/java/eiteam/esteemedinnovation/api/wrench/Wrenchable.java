package eiteam.esteemedinnovation.api.wrench;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;

/**
 * Implement this when you want your machine to do something when it gets wrenched.
 */
public interface Wrenchable {
    /**
     * Called when a player uses a wrench on a tile entity.
     *
     * @param stack  The ItemStack wrench
     * @param player The player
     * @param level  The world
     * @param pos    The block position
     * @param hand   The hand being used
     * @param facing The side of the block hit
     * @param state  The current blockstate.
     * @param hitX   See Item#onItemUseFirst
     * @param hitY   See Item#onItemUseFirst
     * @param hitZ   See Item#onItemUseFirst
     * @return Whether it was successful
     */
    boolean onWrench(@Nonnull ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ);
}
