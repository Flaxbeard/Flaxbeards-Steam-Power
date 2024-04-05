package eiteam.esteemedinnovation.thumper;

import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.api.tile.ThumperAdjacentBehaviorModifier;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileEntityThumper extends SteamTransporterBlockEntity {
    public int progress = 0;
    private boolean isRunning = false;
    private static final Material[] VALID_MATERIALS = {
      Material.SAND,
      Material.GROUND,
      Material.ROCK,
      Material.CLAY,
      Material.GRASS
    };
    private static final float ANVIL_VOLUME = Blocks.ANVIL.getSoundType().getVolume();

    public TileEntityThumper() {
        super(new EnumFacing[] {
          EnumFacing.DOWN,
          EnumFacing.NORTH,
          EnumFacing.SOUTH,
          EnumFacing.EAST,
          EnumFacing.WEST
        });
        addSidesToGaugeBlacklist(EnumFacing.VALUES);
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == ThumperModule.THUMPER;
    }

    @Override
    public void safeUpdate() {
        if (world.isRemote) {
            if (isRunning) {
                if (progress < 110) {
                    progress++;
                } else {
                    progress = 0;
                    isRunning = false;
                }
            } else {
                progress = 0;
                isRunning = false;
            }
        } else {
            if (getSteamShare() >= 2000 && progress == 0) {
                if (!isRunning) {
                    isRunning = true;
                }
                progress++;
                decrSteam(2000);
                markForResync();
            } else if (progress > 0 && !isRunning) {
                isRunning = true;
                markForResync();
            }
            if (progress == 15) {
                world.playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, EsteemedInnovation.SOUND_HISS,
                  SoundCategory.BLOCKS, ANVIL_VOLUME, 0.9F, false);
            }

            if (progress > 0 && progress < 110) {
                progress++;
            }
            if (progress >= 110) {
                progress = 0;
                world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 8F,
                  (1F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F) * 0.7F);
                //	        List players = this.world.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(xCoord-4.5F, yCoord-4.5F, zCoord-4.5F, xCoord+5.5F, yCoord+5.5F, zCoord+5.5F));
                //	        for (Object obj : players) {
                //	        	if (obj instanceof EntityPlayer && this.world.isRemote) {
                //	        		EntityPlayer player = (EntityPlayer) obj;
                //		        	player.rotationPitch += (this.world.rand.nextInt(9) - 4)*1F;
                //		        	player.rotationYaw += (this.world.rand.nextInt(9) - 4)*1F;
                //	        	}
                //
                //	        }
                if (!world.isRemote) {
                    for (int z = 0; z < 4; z++) {
                        boolean hasTarget = false;
                        int i = 0;
                        BlockPos target = new BlockPos(pos.getX(), pos.getY() - 10, pos.getZ());
                        int meta = getBlockMetadata();
                        EnumFacing[] moveDirs;
                        EnumFacing[] moveDirsNotUp;
                        EnumFacing[] forbiddenDirs;
                        if (meta == 1 || meta == 3) {
                            EnumFacing[] moveDirs2 = {EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.EAST, EnumFacing.EAST, EnumFacing.EAST, EnumFacing.EAST, EnumFacing.UP, EnumFacing.WEST, EnumFacing.WEST, EnumFacing.WEST, EnumFacing.WEST, EnumFacing.WEST};
                            EnumFacing[] moveDirsNotUp2 = {EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.EAST, EnumFacing.EAST, EnumFacing.EAST, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.WEST, EnumFacing.WEST, EnumFacing.WEST, EnumFacing.WEST};
                            EnumFacing[] forbiddenDirs2 = {EnumFacing.NORTH, EnumFacing.SOUTH};

                            moveDirs = moveDirs2;
                            moveDirsNotUp = moveDirsNotUp2;
                            forbiddenDirs = forbiddenDirs2;
                        } else {
                            EnumFacing[] moveDirs2 = {EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.UP, EnumFacing.SOUTH, EnumFacing.SOUTH, EnumFacing.SOUTH, EnumFacing.SOUTH, EnumFacing.SOUTH};
                            EnumFacing[] moveDirsNotUp2 = {EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.SOUTH, EnumFacing.SOUTH, EnumFacing.SOUTH, EnumFacing.SOUTH};
                            EnumFacing[] forbiddenDirs2 = {EnumFacing.EAST, EnumFacing.WEST};

                            moveDirs = moveDirs2;
                            moveDirsNotUp = moveDirsNotUp2;
                            forbiddenDirs = forbiddenDirs2;
                        }

                        while (!hasTarget && i < 160) {
                            if (!world.isAirBlock(target) &&
                              Arrays.asList(VALID_MATERIALS).contains(world.getBlockState(target).getMaterial()) &&
                              (world.getBlockState(target).getBlockHardness(world, target) != -1.0F) &&
                              !world.canBlockSeeSky(new BlockPos(target.getX() - 1, target.getY() + 1, target.getZ())) &&
                              !world.canBlockSeeSky(new BlockPos(target.getX() + 1, target.getY() + 1, target.getZ())) &&
                              !world.canBlockSeeSky(new BlockPos(target.getX(), target.getY() + 1, target.getZ() - 1)) &&
                              !world.canBlockSeeSky(new BlockPos(target.getX(), target.getY() + 1, target.getZ() + 1)) &&
                              !world.canBlockSeeSky(new BlockPos(target.getX(), target.getY() + 1, target.getZ()))) {
                                hasTarget = true;
                            } else {
                                if (target.getY() < pos.getY() - 3) {
                                    EnumFacing direction = moveDirs[world.rand.nextInt(moveDirs.length)];
                                    if (world.rand.nextInt(50) == 0) {
                                        direction = forbiddenDirs[world.rand.nextInt(forbiddenDirs.length)];
                                    }
                                    target = new BlockPos(target.getX() + direction.getXOffset(),
                                      target.getY() + direction.getYOffset(),
                                      target.getZ() + direction.getZOffset());
                                } else {
                                    EnumFacing direction = moveDirsNotUp[world.rand.nextInt(moveDirsNotUp.length)];
                                    if (world.rand.nextInt(50) == 0) {
                                        direction = forbiddenDirs[world.rand.nextInt(forbiddenDirs.length)];
                                    }
                                    target = new BlockPos(target.getX() + direction.getXOffset(),
                                      target.getY() + direction.getYOffset(),
                                      target.getZ() + direction.getZOffset());
                                }
                            }
                            i++;
                        }
                        if (hasTarget) {
                            harvestBlock(target);
                        }
                    }
                }
            }
        }

        super.safeUpdate();
    }

    private void harvestBlock(BlockPos position) {
        IBlockState state = world.getBlockState(position);
        Block block = state.getBlock();
        if (ThumperModule.dropItem) {
            Map<EnumFacing, ThumperAdjacentBehaviorModifier> modifiers = getAllAdjacentBehaviorModifiers();
            List<ItemStack> drops = block.getDrops(world, position, state, 0);
            for (Map.Entry<EnumFacing, ThumperAdjacentBehaviorModifier> entry : modifiers.entrySet()) {
                entry.getValue().dropItems(this, drops, state, modifiers.values(), entry.getKey());
            }
            // Default Thumper behavior.
            for (ItemStack drop : drops) {
                Block.spawnAsEntity(world, position, drop);
            }
        }
        world.setBlockToAir(position);
    }

    /**
     * @return All of the behavior modifiers that are adjacent to this tile entity.
     */
    @Nonnull
    private Map<EnumFacing, ThumperAdjacentBehaviorModifier> getAllAdjacentBehaviorModifiers() {
        Map<EnumFacing, ThumperAdjacentBehaviorModifier> behaviorModifiers = new HashMap<>();
        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
            TileEntity behaviorModTE = world.getTileEntity(pos.offset(dir));
            if (behaviorModTE instanceof ThumperAdjacentBehaviorModifier) {
                ThumperAdjacentBehaviorModifier behaviorModifier = (ThumperAdjacentBehaviorModifier) behaviorModTE;
                if (behaviorModifier.isValidBehaviorModifier(this, dir)) {
                    behaviorModifiers.put(dir, behaviorModifier);
                }
            }
        }
        return behaviorModifiers;
    }

    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 5, pos.getZ() + 1);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        progress = nbt.getShort("progress");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setShort("progress", (short) progress);
        return nbt;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = super.getUpdateTag();

        access.setInteger("progress", progress);
        access.setBoolean("isRunning", isRunning);

        return new SPacketUpdateTileEntity(pos, 1, access);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        progress = access.getInteger("progress");
        isRunning = access.getBoolean("isRunning");

        markForResync();
    }
}