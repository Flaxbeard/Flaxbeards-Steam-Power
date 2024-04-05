package eiteam.esteemedinnovation.smasher;

import eiteam.esteemedinnovation.api.tile.BlockEntityBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ITickable;

public class TileEntityDummyBlock extends BlockEntityBase implements ITickable {
    private int timeToLive = 25;

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        super.getUpdatePacket();
        NBTTagCompound access = new NBTTagCompound();
        access.setInteger("ttl", timeToLive);
        return new SPacketUpdateTileEntity(pos, 1, access);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        timeToLive = access.getInteger("ttl");
        markDirty();
    }

    @Override
    public void update() {
        if (timeToLive <= 0) {
            world.setBlockToAir(pos);
        }
        timeToLive--;
    }
}
