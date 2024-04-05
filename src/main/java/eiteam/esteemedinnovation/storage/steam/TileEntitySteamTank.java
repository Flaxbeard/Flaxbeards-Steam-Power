package eiteam.esteemedinnovation.storage.steam;

import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.storage.StorageModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntitySteamTank extends SteamTransporterBlockEntity {
    public TileEntitySteamTank() {
        super(80000, EnumFacing.VALUES);
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == StorageModule.STEAM_TANK;
    }
}
