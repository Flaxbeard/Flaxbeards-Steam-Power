package eiteam.esteemedinnovation.api.tile;

import eiteam.esteemedinnovation.api.SteamTransporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Similar to the {@link SteamTransporterBlockEntity}, this tile entity is used for steam "reactor" blocks.
 * For example: Steam Whistle, Rupture Disc.
 * <p>
 * It provides default safe update (see {@link BlockEntityTickableSafe}) methods that do nothing, because it is
 * completely possible to make a steam reactor that does not tick.
 */
public class SteamReactorBlockEntity extends BlockEntityTickableSafe {
    public SteamReactorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return false;
    }

    @Override
    public void safeUpdate() {}

    /**
     * @param dir The FACING value for the reactor.
     * @return The pressure of the attached transporter.
     */
    public float getPressure(Direction dir) {
        SteamTransporter transporter = getAdjacentTransporter(dir);
        return transporter == null ? 0F : transporter.getPressure();
    }

    /**
     * Drains steam from the attached transporter.
     * @param s The amount of steam to drain.
     * @param dir The FACING value for the reactor.
     */
    public void drainSteam(int s, Direction dir) {
        SteamTransporter transporter = getAdjacentTransporter(dir);
        if (transporter != null) {
            transporter.decrSteam(s);
        }
    }

    /**
     * Gets the current amount of steam in the attached transporter.
     * @param dir The FACING value for the reactor..
     * @return The steam in the transporter.
     */
    public int getSteam(Direction dir) {
        SteamTransporter transporter = getAdjacentTransporter(dir);
        return transporter == null ? 0 : transporter.getSteamShare();
    }

    /**
     * Gets the attached transporter.
     * @return null if there is no SteamTransporter adjacent to it.
     */
    public SteamTransporter getAdjacentTransporter(Direction dir) {
        Direction d = dir.getOpposite();
        BlockEntity be = level.getBlockEntity(getRelativePos(d));
        if (be instanceof SteamTransporter) {
            return (SteamTransporter) be;
        }
        return null;
    }

    public void markForUpdate() {
        markDirty();
    }
}
