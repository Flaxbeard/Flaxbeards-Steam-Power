package eiteam.esteemedinnovation.steamsafety.disc;

import eiteam.esteemedinnovation.api.tile.SteamReactorBlockEntity;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import eiteam.esteemedinnovation.steamsafety.SafetyModule;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityRuptureDisc extends SteamReactorBlockEntity {
    private boolean isLeaking = false;

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = new NBTTagCompound();
        access.setBoolean("isLeaking", isLeaking);
        return new SPacketUpdateTileEntity(pos, 1, access);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        isLeaking = access.getBoolean("isLeaking");
        markForUpdate();
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == SafetyModule.RUPTURE_DISC;
    }

    @Override
    public void safeUpdate() {
        IBlockState startingState = world.getBlockState(pos);
        EnumFacing dir = startingState.getValue(BlockRuptureDisc.FACING);
        if (world.isRemote) {
            if (isLeaking) {
                float offset = 10.0F / 16.0F;
                float xOffset = dir.getOpposite().getXOffset() * offset;
                float yOffset = dir.getOpposite().getYOffset() * offset;
                float zOffset = dir.getOpposite().getZOffset() * offset;
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5F + xOffset,
                  pos.getY() + 0.5F + yOffset, pos.getZ() + 0.5F + zOffset, dir.getXOffset() * 0.1F,
                  dir.getYOffset() * 0.1F, dir.getZOffset() * 0.1F);
            }
        } else {
            if (getPressure(dir) > 1.1F && !startingState.getValue(BlockRuptureDisc.IS_BURST)) {
                world.createExplosion(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, 0.0F, true);
                world.setBlockState(pos, startingState.withProperty(BlockRuptureDisc.IS_BURST, true));
            }
            // We may or may not change the state up there^
            startingState = world.getBlockState(pos);
            if (startingState.getValue(BlockRuptureDisc.IS_BURST)) {
                int i = 0;
                if (getSteam(dir) > 0) {
                    if (!isLeaking) {
                        isLeaking = true;
                        markForResync(startingState);
                        markDirty();
                    }

                    world.playSound(null, pos, EsteemedInnovation.SOUND_LEAK, SoundCategory.BLOCKS, 2F, 0.9F);
                } else {
                    if (isLeaking) {
                        isLeaking = false;
                        markForResync(startingState);
                    }
                }
                while (getSteam(dir) > 0 && i < 10) {
                    drainSteam(10, dir);
                    i++;
                }
            } else {
                if (isLeaking) {
                    isLeaking = false;
                    markForResync(startingState);
                }
            }
        }
    }
}
