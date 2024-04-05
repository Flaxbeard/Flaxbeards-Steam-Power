package eiteam.esteemedinnovation.storage.steam;

import eiteam.esteemedinnovation.api.steamnet.SteamNetwork;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityCreativeTank extends TileEntitySteamTank {
    public TileEntityCreativeTank() {
        super();
        name = "Creative Tank";
        capacity = 1;
    }

    @Override
    public int getSteamShare() {
        return 0;
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return super.canUpdate(target) && target.getValue(BlockSteamTank.IS_CREATIVE);
    }

    @Override
    public void safeUpdate() {
        SteamNetwork net = getNetwork();
        if (net != null && net.getCapacity() > 100 && net.getPressure() < 1F) {
            int capacity = net.getCapacity();
            float pressure = net.getPressure();
            if (capacity > 100 && pressure < 1F) {
                float toAdd = (1F - pressure) * 0.1F * capacity;
                insertSteam((int) (Math.floor((double) toAdd)), EnumFacing.UP);
            }
        }

        super.safeUpdate();
    }
}
