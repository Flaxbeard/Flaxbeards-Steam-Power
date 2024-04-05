package eiteam.esteemedinnovation.smasher;

import eiteam.esteemedinnovation.api.SmasherRegistry;
import eiteam.esteemedinnovation.api.steamnet.SteamNetwork;
import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.api.wrench.WrenchDisplay;
import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.core.Direction;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static eiteam.esteemedinnovation.smasher.SmasherModule.ROCK_SMASHER;
import static eiteam.esteemedinnovation.smasher.SmasherModule.ROCK_SMASHER_DUMMY;

public class TileEntitySmasher extends SteamTransporterBlockEntity implements Wrenchable, WrenchDisplay {
    public int spinup = 0;
    private float extendedLength = 0.0F;
    private Block smooshingBlock;
    private int smooshingMeta;
    public int extendedTicks = 0;
    private List<ItemStack> smooshedStack;
    private boolean isActive = false;
    private boolean isBreaking = false;
    private boolean shouldStop = false;
    private boolean running = false;
    private boolean smashNextRound = false;
    private boolean blockBreakerMode = false;
    private boolean hasBeenSet = false;

    public TileEntitySmasher() {
        super(EnumFacing.VALUES);
    }

    @Override
    public void readFromNBT(NBTTagCompound access) {
        super.readFromNBT(access);
        extendedLength = access.getFloat("extendedLength");
        extendedTicks = access.getInteger("extendedTicks");
        spinup = access.getInteger("spinup");
        blockBreakerMode = access.getBoolean("blockBreakerMode");
        hasBeenSet = access.getBoolean("hasBeenSet");
        smooshingBlock = Block.getBlockById(access.getInteger("block"));
        smooshingMeta = access.getInteger("smooshingMeta");
        NBTTagList nbttaglist = (NBTTagList) access.getTag("Items");
        smooshedStack = new ArrayList<>();

        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            smooshedStack.add(new ItemStack(nbttagcompound1));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound access) {
        super.writeToNBT(access);
        access.setBoolean("blockBreakerMode", blockBreakerMode);
        access.setBoolean("hasBeenSet", hasBeenSet);
        access.setInteger("spinup", spinup);
        access.setFloat("extendedLength", extendedLength);
        access.setInteger("extendedTicks", extendedTicks);
        access.setInteger("block", Block.getIdFromBlock(smooshingBlock));
        access.setInteger("smooshingMeta", smooshingMeta);
        NBTTagList nbttaglist = new NBTTagList();

        if (smooshedStack != null) {
            for (ItemStack aSmooshedStack : smooshedStack) {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                aSmooshedStack.writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        access.setTag("Items", nbttaglist);

        return access;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = super.getUpdateTag();
        access.setInteger("spinup", spinup);
        access.setFloat("extendedLength", extendedLength);
        access.setInteger("extendedTicks", extendedTicks);
        access.setInteger("block", Block.getIdFromBlock(smooshingBlock));
        access.setInteger("smooshingMeta", smooshingMeta);
        access.setBoolean("running", running);
        access.setBoolean("blockBreakerMode", blockBreakerMode);
        access.setBoolean("hasBeenSet", hasBeenSet);
        return new SPacketUpdateTileEntity(pos, 1, access);
    }

    private void decodeAndCreateParticles() {
        if (world.isRemote) {
            if (extendedTicks > 15) {
                float xVelocity = 0F;
                float zVelocity = 0F;
                double xOffset = 0D;
                double zOffset = 0D;
                EnumFacing facing = world.getBlockState(pos).getValue(BlockSmasher.FACING);
                if (facing.getAxis() == EnumFacing.Axis.X) {
                    xVelocity = 0.05F * -facing.getXOffset();
                    xOffset = 0.1D * -facing.getXOffset();
                } else {
                    zVelocity = 0.05F * -facing.getZOffset();
                    zOffset = 0.1D * -facing.getZOffset();
                }
                double xParticle = pos.getX() + 0.5D + xOffset;
                double yParticle = pos.getY() + 1.1D;
                double zParticle = pos.getZ() + 0.5D + zOffset;
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, xParticle, yParticle, zParticle, xVelocity, 0.05F, zVelocity);
            }
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        extendedLength = access.getFloat("extendedLength");
        extendedTicks = access.getInteger("extendedTicks");
        spinup = access.getInteger("spinup");
        smooshingBlock = Block.getBlockById(access.getInteger("block"));
        smooshingMeta = access.getInteger("smooshingMeta");
        running = access.getBoolean("running");
        blockBreakerMode = access.getBoolean("blockBreakerMode");
        hasBeenSet = access.getBoolean("hasBeenSet");
        markForResync();
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == ROCK_SMASHER;
    }

    @Override
    public void initialUpdate() {
        super.initialUpdate();
        EnumFacing facing = world.getBlockState(pos).getValue(BlockSmasher.FACING);
        addSideToGaugeBlacklist(facing);
        setValidDistributionDirectionsExcluding(facing, EnumFacing.UP);
    }

    @Override
    public void safeUpdate() {
        if (!world.isRemote) {
            int[] target = getTarget(1);

            int x = target[0];
            int y = pos.getY();
            int z = target[1];
            BlockPos soundPos = new BlockPos(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
            if (spinup == 1) {
                world.playSound(null, soundPos, EsteemedInnovation.SOUND_HISS, SoundCategory.BLOCKS,
                  SoundType.ANVIL.getVolume(), 0.9F);
            }
            if (extendedTicks > 15) {
                world.playSound(null, soundPos, EsteemedInnovation.SOUND_LEAK, SoundCategory.BLOCKS, 2F, 0.9F);
            }
            if (extendedTicks == 5) {
                float pitch = (0.75F * (float) Math.random() * 0.1F);
                world.playSound(null, soundPos, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 0.5F, pitch);
            }
            if (extendedTicks > 0 && extendedTicks < 6 && smooshingBlock != null) {
                SoundType smooshingSoundType = smooshingBlock.getSoundType(world.getBlockState(soundPos), world, soundPos, null);
                SoundEvent breakSound = smooshingSoundType.getBreakSound();
                world.playSound(null, soundPos, breakSound, SoundCategory.BLOCKS,
                  smooshingSoundType.getVolume(), smooshingSoundType.getPitch());
            }
            //handle state changes
            if (shouldStop) {
                spinup = 0;
                extendedLength = 0;
                extendedTicks = 0;
                isActive = false;
                shouldStop = false;
                isBreaking = false;
                running = false;
                markForResync();
                return;
            }
            if (!smashNextRound && hasSomethingToSmash() && hasPartner() && getSteamShare() > 1000 && !isActive) {
                smashNextRound = true;
                return;
            }
            boolean smashThisRound = false;
            if (smashNextRound) {
                smashThisRound = true;
                smashNextRound = false;
            }
            if (smashThisRound) {
                if (hasSomethingToSmash() && !isActive) {
                    decrSteam(1000);
                    running = true;
                    markForResync();
                    isActive = true;
                    isBreaking = true;
                }
            }

            BlockPos middleBlockPos = new BlockPos(x, y, z);
            //handle processing
            if (isActive) {
                // if we haven't spun up yet, do it.
                if (isBreaking) {
                    if (!hasSomethingToSmash() && spinup < 40 && isPrimary()) {
                        shouldStop = true;
                        int[] tc = getTarget(2);
                        BlockPos position = new BlockPos(tc[0], y, tc[1]);
                        TileEntitySmasher partner = (TileEntitySmasher) world.getTileEntity(position);
                        if (partner != null) {
                            partner.shouldStop = true;
                        }
                        return;
                    }
                    if (spinup < 41) {
                        // spinup complete. SMAASH!
                        if (spinup == 40) {
                            IBlockState middleBlockState = world.getBlockState(middleBlockPos);
                            Block middleBlock = middleBlockState.getBlock();
                            if (hasSomethingToSmash()) {
                                spinup++;
                                if (isPrimary()) {
                                    smooshingBlock = middleBlock;
                                    smooshingMeta = middleBlock.getMetaFromState(middleBlockState);
                                    smooshedStack = smooshingBlock.getDrops(world, middleBlockPos, middleBlockState, 0);
                                    markForResync();
                                    world.setBlockState(middleBlockPos, ROCK_SMASHER_DUMMY.getDefaultState());
                                }
                            } else {
                                if (hasPartner()) {
                                    int[] pc = getTarget(2);
                                    BlockPos partnerPosition = new BlockPos(pc[0], y, pc[1]);
                                    TileEntitySmasher partner = (TileEntitySmasher) world.getTileEntity(partnerPosition);
                                    //noinspection ConstantConditions
                                    if (partner.spinup < 41 || (partner.spinup >= 41 && partner.shouldStop)) {
                                        shouldStop = true;
                                    }

                                    if (shouldStop) {
                                        spinup++;
                                        return;
                                    }
                                }
                            }
                        }

                        spinup++;
//                     if we've spun up, extend
                    } else if (extendedLength < 0.5F && !shouldStop) {
                        extendedLength += 0.1F;
                        if (extendedTicks == 3 && isPrimary() && !world.isRemote) {
                            spawnOutputForSmashedBlock(middleBlockPos);
                        }
                        extendedTicks++;

                        // we're done extending. Time to go inactive and start retracting
                    } else {
                        isBreaking = false;
                        spinup = 0;
                    }
                } else {
                    // Get back in line!
                    if (extendedLength > 0.0F) {
                        extendedLength -= 0.025F;
                        extendedTicks++;

                        if (extendedLength < 0F) {
                            extendedLength = 0F;
                        }
                    } else {
                        isActive = false;
                        running = false;
                        markForResync();
                        extendedTicks = 0;
                        if (world.getBlockState(middleBlockPos).getBlock() == ROCK_SMASHER_DUMMY) {
                            world.setBlockToAir(middleBlockPos);
                        }
                    }
                }
                // Sync.
            } else if (world.getBlockState(middleBlockPos).getBlock() == ROCK_SMASHER_DUMMY && isPrimary()) {
                world.setBlockToAir(middleBlockPos);
            }
        } else {
            if (running) {
                decodeAndCreateParticles();
                if (spinup < 40) {
                    spinup++;
                } else if (spinup == 40) {
                    int[] tc = getTarget(1);
                    int x = tc[0];
                    int y = tc[1];
                    int z = tc[2];
                    BlockPos targetPosition = new BlockPos(x, y, z);
                    spinup++;
                    if (!world.isAirBlock(targetPosition) && world.getTileEntity(targetPosition) == null) {
                        IBlockState blockState = world.getBlockState(targetPosition);
                        if (blockState.getBlockHardness(world, targetPosition) < 50F && isPrimary()) {
                            smooshingBlock = blockState.getBlock();
                            smooshingMeta = blockState.getBlock().getMetaFromState(blockState);
                        }
                    }
                } else if (extendedTicks < 25) {
                    extendedTicks++;
                }
            } else {
                spinup = 0;
                extendedTicks = 0;
            }
        }

        super.safeUpdate();
    }

    private boolean isPrimary() {
        return world.getBlockState(pos).getValue(BlockSmasher.FACING).getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
    }

    private void spawnOutputForSmashedBlock(BlockPos position) {
        if (smooshedStack != null) {
            double x = position.getX() + 0.5F;
            double y = position.getY() + 0.1F;
            double z = position.getZ() + 0.5F;
            for (ItemStack stack : smooshedStack) {
                List<ItemStack> output = SmasherRegistry.getOutput(stack, world);
                
                if (output.isEmpty() || blockBreakerMode) {
                    spawnItems(x, y, z, Collections.singletonList(stack));
                } else {
                    spawnItems(x, y, z, output);
                    smooshedStack = null;
                }
            }
        }
    }

    private void spawnItems(double x, double y, double z, List<ItemStack> items) {
        for (ItemStack item : items) {
            EntityItem entityItem = new EntityItem(world, x, y, z, item);
            world.spawnEntity(entityItem);
        }
    }

    private boolean hasSomethingToSmash() {
        int[] target = getTarget(1);
        int x = target[0];
        int y = pos.getY();
        int z = target[1];
        BlockPos position = new BlockPos(x, y, z);
        IBlockState blockState = world.getBlockState(position);
        Block block = blockState.getBlock();
        return !world.isAirBlock(position) && block != Blocks.BEDROCK &&
          world.getTileEntity(position) == null && blockState.getBlockHardness(world, position) < 50F;
    }

    public boolean hasPartner() {
        int[] target = getTarget(2);
        int x = target[0];
        int y = pos.getY();
        int z = target[1];
        int opposite = target[2];

        BlockPos partnerPos = new BlockPos(x, y, z);
        IBlockState partnerState = world.getBlockState(partnerPos);
        Block partner = partnerState.getBlock();
        TileEntity partnerTE = world.getTileEntity(partnerPos);
        if (partnerTE == null || !(partnerTE instanceof TileEntitySmasher)) {
            return false;
        }
        if (partner == ROCK_SMASHER && ((TileEntitySmasher) partnerTE).getSteamShare() > 100 &&
          partner.getMetaFromState(partnerState) == opposite) {
            TileEntitySmasher partnerSmasher = (TileEntitySmasher) partnerTE;
            if (partnerSmasher.blockBreakerMode != blockBreakerMode) {
                if (hasBeenSet && !partnerSmasher.hasBeenSet) {
                    partnerSmasher.blockBreakerMode = blockBreakerMode;
                } else if (!hasBeenSet && partnerSmasher.hasBeenSet) {
                    blockBreakerMode = partnerSmasher.blockBreakerMode;
                } else {
                    blockBreakerMode = partnerSmasher.blockBreakerMode;
                }
                hasBeenSet = true;
                partnerSmasher.hasBeenSet = true;
            }
            return true;
        }

        return false;
    }

    /**
     * @param distance The distance from the current smasher to check in. For example, getTarget(2) will check
     *                 X_Y where X is the current smasher, and Y is the space to check.
     * @return An array of the X and Z coordinates (no Y because the smasher cannot face up/down) and the opposite
     *         facing horizontal index.
     */
    private int[] getTarget(int distance) {
        EnumFacing facing = world.getBlockState(pos).getValue(BlockSmasher.FACING);
        EnumFacing opposite = facing.getOpposite();
        BlockPos target = pos.offset(facing, distance);
        return new int[] { target.getX(), target.getZ(), opposite.getHorizontalIndex() };
    }

    @Override
    public boolean onWrench(ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            hasBeenSet = true;
            blockBreakerMode = !blockBreakerMode;
            int[] target = getTarget(2);
            int x2 = target[0];
            int y2 = this.pos.getY();
            int z2 = target[1];
            int opposite = target[2];
            BlockPos pos2 = new BlockPos(x2, y2, z2);
            IBlockState state2 = level.getBlockState(pos2);
            Block block = state2.getBlock();
            if (block == ROCK_SMASHER && block.getMetaFromState(state2) == opposite) {
                TileEntitySmasher smasher = (TileEntitySmasher) level.getTileEntity(pos2);
                if (smasher != null) {
                    smasher.blockBreakerMode = blockBreakerMode;
                    smasher.hasBeenSet = true;
                    smasher.markForResync();
                }
            }
            markForResync();
        } else {
            int steam = getSteamShare();
            getNetwork().split(this, true);
            EnumFacing myFacing = level.getBlockState(pos).getValue(BlockSmasher.FACING);
            setValidDistributionDirectionsExcluding(myFacing, EnumFacing.UP);
            SteamNetwork.newOrJoin(this);
            getNetwork().addSteam(steam);
        }
        return true;
    }


    @Override
    public void displayWrench(Post event) {
        GlStateManager.pushMatrix();
        int color = Minecraft.getMinecraft().player.isSneaking() ? 0xC6C6C6 : 0x777777;
        int x = event.getResolution().getScaledWidth() / 2 - 8;
        int y = event.getResolution().getScaledHeight() / 2 - 8;
        String loc = I18n.format("esteemedinnovation.smasher." + blockBreakerMode);
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(loc, x + 15, y + 13, color);
        GlStateManager.popMatrix();
    }
}
