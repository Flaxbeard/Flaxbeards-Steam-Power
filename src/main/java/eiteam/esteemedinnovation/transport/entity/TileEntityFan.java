package eiteam.esteemedinnovation.transport.entity;

import eiteam.esteemedinnovation.api.steamnet.SteamNetwork;
import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.api.wrench.WrenchDisplay;
import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import eiteam.esteemedinnovation.commons.util.MathUtility;
import eiteam.esteemedinnovation.transport.TransportationModule;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.core.Direction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.fluids.BlockFluidBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class TileEntityFan extends SteamTransporterBlockEntity implements Wrenchable, WrenchDisplay {
    public boolean active;
    public boolean powered = false;
    public boolean lastSteam = false;
    public int rotateTicks = 0;
    public int range = 9;
    private static final int STEAM_CONSUMPTION = TransportationModule.fanConsumption;

    public TileEntityFan() {
        addSidesToGaugeBlacklist(EnumFacing.HORIZONTALS);
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound access) {
        super.writeToNBT(access);
        access.setBoolean("powered", powered);
        access.setShort("range", (short) range);

        return access;
    }

    @Override
    public void readFromNBT(NBTTagCompound access) {
        super.readFromNBT(access);
        powered = access.getBoolean("powered");
        range = access.getShort("range");
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = super.getUpdateTag();
        access.setBoolean("active", getSteamShare() > 0 && !powered);
        access.setShort("range", (short) range);
        return new SPacketUpdateTileEntity(pos, 1, access);
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        active = access.getBoolean("active");
        range = access.getShort("range");
        markForResync();
    }

    /**
     * Gets either a random double, or 0.5 based on the offset.
     * @param offset The front offset for the direction
     * @return double
     */
    private double getRandomOrSlight(int offset) {
        return offset == 0 ? Math.random() : 0.5D;
    }

    /**
     * Gets the X, Y, Z positions for the smoke particles.
     * @param dir The dir
     * @return {double, double, double}
     */
    private double[] getSmokePositions(EnumFacing dir) {
        return new double[] {
          pos.getX() + getRandomOrSlight(dir.getXOffset()),
          pos.getY() + getRandomOrSlight(dir.getYOffset()),
          pos.getZ() + getRandomOrSlight(dir.getZOffset())
        };
    }

    /**
     * Gets the X, Y, Z speeds for the smoke particles.
     * @param dir The dir
     * @return {double, double, double}
     */
    private double[] getSmokeSpeeds(EnumFacing dir) {
        return new double[] {
          dir.getXOffset() * 0.2F,
          dir.getYOffset() * 0.2F,
          dir.getZOffset() * 0.2F
        };
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() == TransportationModule.FAN;
    }

    @Override
    public void initialUpdate() {
        super.initialUpdate();
        powered = world.isBlockPowered(pos);
        setDistributionDirections(new EnumFacing[] { world.getBlockState(pos).getValue(BlockFan.FACING).getOpposite() });
    }

    @Override
    public void safeUpdate() {
        if (lastSteam != getSteamShare() >= STEAM_CONSUMPTION) {
            markForResync();
        }
        lastSteam = getSteamShare() > STEAM_CONSUMPTION;
        if (!lastSteam && !world.isRemote && active) {
            markForResync();
        }
        if (active && world.isRemote) {
            rotateTicks++;
        }

        if (active && world.isRemote || (getSteamShare() >= STEAM_CONSUMPTION && !powered)) {
            if (!world.isRemote) {
                decrSteam(STEAM_CONSUMPTION);
            }
            EnumFacing dir = world.getBlockState(pos).getValue(BlockFan.FACING);
            double[] positions = getSmokePositions(dir);
            double[] speeds = getSmokeSpeeds(dir);
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, positions[0], positions[1], positions[2],
              speeds[0], speeds[1], speeds[2]);
            int blocksInFront = 0;
            boolean blocked = false;
            for (int i = 1; i < range; i++) {
                int x = pos.getX() + dir.getXOffset() * i;
                int y = pos.getY() + dir.getYOffset() * i;
                int z = pos.getZ() + dir.getZOffset() * i;
                BlockPos offsetPos = new BlockPos(x, y, z);
                IBlockState offsetState = world.getBlockState(offsetPos);
                Block offsetBlock = offsetState.getBlock();
                if (!world.isRemote && world.rand.nextInt(20) == 0 && !blocked && offsetBlock != Blocks.AIR &&
                  offsetBlock.isReplaceable(world, offsetPos) || offsetBlock instanceof BlockCrops) {
                    // We don't need to check the other FluidBlock classes because they all inherit Base.
                    if (offsetBlock instanceof BlockFluidBase) {
                        for (int v = 0; v < 5; v++) {
                            EsteemedInnovation.proxy.spawnBreakParticles(world, x + 0.5F, y + 0.5F, z + 0.5F, offsetBlock, 0F, 0F, 0F);
                        }
                    }
                    world.setBlockToAir(offsetPos);
                }
                if (!blocked && (offsetBlock.isReplaceable(world, offsetPos) || world.isAirBlock(offsetPos) ||
                  offsetBlock instanceof BlockTrapDoor || offsetState.getCollisionBoundingBox(world, offsetPos) == null)) {
                    blocksInFront = i;
                    if (i != range - 1) {
                        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + getRandomOrSlight(dir.getXOffset()),
                          y + getRandomOrSlight(dir.getYOffset()), z + getRandomOrSlight(dir.getZOffset()),
                          speeds[0], speeds[1], speeds[2]);
                    }
                } else {
                    blocked = true;
                }
            }
            List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.getX() + (dir.getXOffset() < 0 ? dir.getXOffset() * blocksInFront : 0), pos.getY() + (dir.getYOffset() < 0 ? dir.getYOffset() * blocksInFront : 0), pos.getZ() + (dir.getZOffset() < 0 ? dir.getZOffset() * blocksInFront : 0), pos.getX() + 1 + (dir.getXOffset() > 0 ? dir.getXOffset() * blocksInFront : 0), pos.getY() + 1 + (dir.getYOffset() > 0 ? dir.getYOffset() * blocksInFront : 0), pos.getZ() + 1 + (dir.getZOffset() > 0 ? dir.getZOffset() * blocksInFront : 0)));
            for (Entity entity : entities) {
                if (!(entity instanceof EntityPlayer) || !(((EntityPlayer) entity).capabilities.isFlying && ((EntityPlayer) entity).capabilities.isCreativeMode)) {
                    if (entity instanceof EntityPlayer && entity.isSneaking()) {
                        entity.motionX += dir.getXOffset() * 0.025F;
                        entity.motionY += dir.getYOffset() * 0.05F;
                        entity.motionZ += dir.getZOffset() * 0.025F;
                    } else {
                        entity.motionX += dir.getXOffset() * 0.075F;
                        entity.motionY += dir.getYOffset() * 0.1F;
                        entity.motionZ += dir.getZOffset() * 0.075F;
                    }
                    entity.fallDistance = 0.0F;
                }
            }
        }

        super.safeUpdate();
    }

    public void updateRedstoneState(boolean flag) {
        if (flag != powered) {
            powered = flag;
            markForResync();
        }
    }

    @Override
    public boolean onWrench(ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            range = MathUtility.minWithDefault(19, range + 2, 5);
            markForResync(state);
        } else {
            int steam = getSteamShare();
            getNetwork().split(this, true);
            setDistributionDirections(new EnumFacing[] { level.getBlockState(pos).getValue(BlockFan.FACING).getOpposite() });

            SteamNetwork.newOrJoin(this);
            getNetwork().addSteam(steam);
        }
        return true;
    }

    public static void rangeDisplay(Post event, int range) {
        GL11.glPushMatrix();
        Minecraft mc =  Minecraft.getMinecraft();
        int color = mc.player.isSneaking() ? 0xC6C6C6 : 0x777777;
        int x = event.getResolution().getScaledWidth() / 2 - 8;
        int y = event.getResolution().getScaledHeight() / 2 - 8;
        mc.fontRenderer.drawStringWithShadow(I18n.format("esteemedinnovation.fan.range", range), x + 15, y + 13, color);
        GL11.glPopMatrix();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void displayWrench(Post event) {
        rangeDisplay(event, range);
    }
}
