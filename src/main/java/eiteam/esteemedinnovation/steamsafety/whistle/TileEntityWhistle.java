package eiteam.esteemedinnovation.steamsafety.whistle;

import eiteam.esteemedinnovation.api.tile.SteamReactorBlockEntity;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import eiteam.esteemedinnovation.commons.audio.SoundTile;
import eiteam.esteemedinnovation.commons.audio.TickableSoundTile;
import eiteam.esteemedinnovation.steamsafety.SafetyModule;
import eiteam.esteemedinnovation.transport.steam.TileEntitySteamPipe;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityWhistle extends SteamReactorBlockEntity implements SoundTile {
    private float volume;
    private boolean isSoundRegistered;
    private boolean isSounding;
    private int steamTick;

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        super.getUpdatePacket();
        NBTTagCompound access = new NBTTagCompound();
        access.setBoolean("isSounding", isSounding);

        return new SPacketUpdateTileEntity(pos, 1, access);
    }


    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        isSounding = access.getBoolean("isSounding");

        markForUpdate();
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == SafetyModule.STEAM_WHISTLE;
    }

    @Override
    public void safeUpdate() {
        if (world.isRemote) {
            updateSound();
        } else {
            if (shouldPlay()) {
                if (!isSounding) {
                    isSounding = true;
                }
            } else {
                if (isSounding) {
                    isSounding = false;
                }
            }
            markForResync();
        }
    }

    @SideOnly(Side.CLIENT)
    private void updateSound() {
        if (!isSoundRegistered) {
            world.playSound(null, pos, EsteemedInnovation.SOUND_WHISTLE, SoundCategory.BLOCKS, volume, 1F);
//                Sounds.addSoundTile(this);
            isSoundRegistered = true;
        }

        if (isSounding) {
            if (steamTick == 0) {
                EnumFacing dir = world.getBlockState(pos).getValue(BlockWhistle.FACING).getOpposite();
                TileEntity te = world.getTileEntity(pos.offset(dir));
                float offset = 2.0F / 16.0F;
                if (te instanceof TileEntitySteamPipe) {
                    offset = 6.0F / 16.0F;
                }
                float offset2 = (2.0F / 16.0F / 3.0F);

                float xOffset = dir.getXOffset() * offset;
                float zOffset = dir.getZOffset() * offset;
                float yOffset = dir.getYOffset() * offset;
                float xOffset2 = dir.getXOffset() * offset2;
                float zOffset2 = dir.getZOffset() * offset2;
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5D + xOffset,
                  pos.getY() + 0.7D + yOffset, pos.getZ() + 0.5D + zOffset, 0F - xOffset2, 0.05F, 0F - zOffset2);

                world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                  EsteemedInnovation.SOUND_WHISTLE, SoundCategory.BLOCKS, volume, 1F, true);
            }
            steamTick++;
            if (steamTick >= 4) {
                steamTick = 0;
            }

            if (volume < 0.75F) {
                volume += 0.01F;
            }
        } else if (volume > 0F) {
            volume -= 0.25F;
        } else {
            volume = 0F;
        }
    }

    @Override
    public boolean shouldPlay() {
        return getPressure(world.getBlockState(pos).getValue(BlockWhistle.FACING)) > 1.02F;
    }

    @Override
    public ResourceLocation getSound() {
        return EsteemedInnovation.SOUND_WHISTLE.getSoundName();
    }

    @Override
    public TileEntity getTileEntity() {
        return this;
    }

    @Override
    public boolean handleUpdate() {
        return true;
    }

    @Override
    public void update(TickableSoundTile tickableSoundTile) {
        tickableSoundTile.volume = volume;
    }
}
