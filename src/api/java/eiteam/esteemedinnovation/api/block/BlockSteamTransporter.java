package eiteam.esteemedinnovation.api.block;

import eiteam.esteemedinnovation.api.SteamTransporter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockSteamTransporter extends Block {
    public BlockSteamTransporter(Properties properties) {
        super(properties);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        SteamTransporter te = (SteamTransporter) level.getBlockEntity(pos);
        if (te == null) {
            return;
        }
        if (te.getNetwork() != null) {
            te.getNetwork().split(te, true);
        }
        te.refresh();
    }
}
