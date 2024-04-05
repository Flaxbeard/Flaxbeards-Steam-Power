package eiteam.esteemedinnovation.transport.steam;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.Cuboid6;
import eiteam.esteemedinnovation.api.SteamTransporter;
import eiteam.esteemedinnovation.api.steamnet.SteamNetwork;
import eiteam.esteemedinnovation.api.tile.SteamTransporterBlockEntity;
import eiteam.esteemedinnovation.api.wrench.Wrenchable;
import eiteam.esteemedinnovation.commons.EsteemedInnovation;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.core.Direction;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

public class TileEntitySteamPipe extends SteamTransporterBlockEntity implements Wrenchable {
    //protected FluidTank dummyFluidTank = FluidRegistry.isFluidRegistered("steam") ? new FluidTank(new FluidStack(FluidRegistry.getFluid("steam"), 0),10000) : null;
    public ArrayList<Integer> blacklistedSides = new ArrayList<>();
    public Block disguiseBlock = null;
    public int disguiseMeta = 0;
    public boolean isOriginalPipe = false;
    public boolean isOtherPipe = false;
    protected boolean isLeaking = false;
    private boolean lastWrench = false;

    public TileEntitySteamPipe() {
        super(EnumFacing.VALUES);
        name = "Pipe";
    }

    public TileEntitySteamPipe(int capacity) {
        this();
        this.capacity = capacity;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound access = super.getUpdateTag();
        access.setBoolean("isLeaking", isLeaking);
        NBTTagCompound list = new NBTTagCompound();
        int g = 0;
        for (int i : blacklistedSides) {
            list.setInteger(Integer.toString(g), i);
            g++;
        }
        list.setInteger("size", g);
        access.setTag("blacklistedSides", list);
        access.setInteger("disguiseBlock", Block.getIdFromBlock(disguiseBlock));
        access.setInteger("disguiseMeta", disguiseMeta);
        return new SPacketUpdateTileEntity(pos, 1, access);
    }


    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);
        NBTTagCompound access = pkt.getNbtCompound();
        isLeaking = access.getBoolean("isLeaking");
        NBTTagCompound sidesList = access.getCompoundTag("blacklistedSides");
        int length = sidesList.getInteger("size");
        Integer[] sidesInt = new Integer[length];
        for (int i = 0; i < length; i++) {
            sidesInt[i] = sidesList.getInteger(Integer.toString(i));
        }
        blacklistedSides = new ArrayList<>(Arrays.asList(sidesInt));
        disguiseBlock = Block.getBlockById(access.getInteger("disguiseBlock"));
        disguiseMeta = access.getInteger("disguiseMeta");
        markDirty();
    }


    @Override
    public void readFromNBT(NBTTagCompound access) {
        super.readFromNBT(access);
        NBTTagCompound sidesList = access.getCompoundTag("blacklistedSides");
        int length = sidesList.getInteger("size");
        Integer[] sidesInt = new Integer[length];
        for (int i = 0; i < length; i++) {
            sidesInt[i] = sidesList.getInteger(Integer.toString(i));
        }
        blacklistedSides = new ArrayList<>(Arrays.asList(sidesInt));
        disguiseBlock = Block.getBlockById(access.getInteger("disguiseBlock"));
        disguiseMeta = access.getInteger("disguiseMeta");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound access) {
        super.writeToNBT(access);
        NBTTagCompound list = new NBTTagCompound();
        int g = 0;
        for (int i : blacklistedSides) {
            list.setInteger(Integer.toString(g), i);
            g++;
        }
        list.setInteger("size", g);
        access.setTag("blacklistedSides", list);
        access.setInteger("disguiseBlock", Block.getIdFromBlock(disguiseBlock));
        access.setInteger("disguiseMeta", disguiseMeta);

        return access;
    }

    /*
    Supersuper methods for bypassing TileEntitySteamPipe's behavior.
    TODO: Create a base TileEntityPipe class for TileEntitySteamPipe (this), TileEntityValvePipe, and TileEntitySteamHeater to inherit.
     */
    protected void superUpdate() {
        super.safeUpdate();
   }

    protected NBTTagCompound superWriteToNBT(NBTTagCompound access) {
        return super.writeToNBT(access);
    }

    protected void superReadFromNBT(NBTTagCompound access) {
        super.readFromNBT(access);
    }

    public ArrayList<EnumFacing> getMyDirections() {
        ArrayList<EnumFacing> myDirections = new ArrayList<>();
        for (EnumFacing direction : getConnectionSides()) {
            TileEntity tile = world.getTileEntity(getRelativePos(direction));
            if (tile instanceof SteamTransporter) {
                SteamTransporter target = (SteamTransporter) tile;
                if (target.doesConnect(direction.getOpposite())) {
                    myDirections.add(direction);
                }
            }
        }

        return myDirections;
    }

    /**
     * Handles the leaking logic. Checks if there is only one connection, if it can leak, etc.
     * If it is leaking and shouldn't, this stops it, and if it isn't but should, this makes it leak.
     */
    public void leak() {
        ArrayList<EnumFacing> myDirections = getMyDirections();
        if (myDirections.size() != 1) {
            return;
        }
        EnumFacing direction = myDirections.get(0).getOpposite();
        while (!doesConnect(direction) || direction == myDirections.get(0)) {
            direction = EnumFacing.VALUES[(direction.ordinal() + 1) % 5];
        }
        if (!world.isRemote) {
            int i = 0;
            IBlockState myState = world.getBlockState(pos);
            if (canLeak(direction)) {
                world.playSound(null, pos, EsteemedInnovation.SOUND_LEAK, SoundCategory.BLOCKS, 2F, 0.9F);
                if (!isLeaking) {
                    isLeaking = true;
                    markForResync(myState);
                    markDirty();
                }
            } else {
                if (isLeaking) {
                    isLeaking = false;
                    markForResync(myState);
                    markDirty();
                }
            }
            while (isLeaking && i < 10) {
                decrSteam(10);
                i++;
            }
        } else if (isLeaking) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, pos.getX() + 0.5F, pos.getY() + 0.5F,
              pos.getZ() + 0.5F, direction.getXOffset() * 0.1F, direction.getYOffset() * 0.1F,
              direction.getZOffset() * 0.1F);
        }
    }

    /**
     * Gets whether this tileentity is able to leak from this side.
     * @param direction The side
     * @return Whether it can leak.
     */
    public boolean canLeak(EnumFacing direction) {
        BlockPos dirPos = getRelativePos(direction);
        return (getSteamShare() > 0 && (world.isAirBlock(dirPos) ||
          !world.isSideSolid(dirPos, direction.getOpposite())));
    }

    @Override
    public boolean canUpdate(BlockState target) {
        return target.getBlock() instanceof BlockSteamPipe;
    }

    @Override
    public void safeUpdate() {
        /*
        TODO: Port this.
        if (world.isRemote) {
            boolean hasWrench = BlockSteamPipeRenderer.updateWrenchStatus();
            if (hasWrench != lastWrench && !(disguiseBlock == null || disguiseBlock == Blocks.AIR)) {
                markDirty();
            }
            lastWrench = hasWrench;
        }
        */
        leak();
        super.safeUpdate();
    }

    @Override
    public Set<Direction> getConnectionSides() {
        HashSet<EnumFacing> out = new HashSet<>();
        HashSet<EnumFacing> blacklist = new HashSet<>();
        for (int i : blacklistedSides) {
            blacklist.add(EnumFacing.byIndex(i));
        }
        for (EnumFacing d : distributionDirections) {
            if (!blacklist.contains(d)) {
                out.add(d);
            }

        }
        return out;
    }

    @Override
    public boolean doesConnect(Direction face) {
        for (int i : blacklistedSides) {
            if (EnumFacing.VALUES[i] == face) {
                return false;
            }
        }
        return super.doesConnect(face);
    }

    @Override
    public int getSteamShare() {
        SteamNetwork network = getNetwork();
        if (network == null) {
            this.network = null;
            networkName = null;
            return 0;
        } else {
            return (int) Math.floor((double) network.getPressure() * (double) capacity);
        }

    }

    /**
     * Gets whether the pipe can connect to something in the given direction.
     * @param direction The direction
     * @return 0 if it cannot connect. 2 if it can connect to another pipe. 1 if it can connect, but not to another pipe.
     */
    public int canConnectSide(EnumFacing direction) {
        BlockPos pos = getRelativePos(direction);
        TileEntity tile = world.getTileEntity(pos);
        if (tile != null && tile instanceof SteamTransporter) {
            SteamTransporter target = (SteamTransporter) tile;
            if (target.doesConnect(direction.getOpposite())) {
                return target instanceof TileEntitySteamPipe ? 2 : 1;
            }
            if (target instanceof TileEntitySteamPipe && ((TileEntitySteamPipe) target).blacklistedSides.contains(direction.getOpposite().ordinal())) {
                return 2;
            }
        }
        return 0;
    }

    /**
     * Checks whether the pipe is connected at the given direction, or the opposite and only the opposite.
     * This is referred to in several comments as a "long" pipe, eg. =.=.=, the first and last are the "long" pipes,
     * because they do not connect to anything on more than one side, but are still rendered as "long", as if they were.
     *
     * This method will be significantly slower than the other shouldStretchInDirection method in most use cases,
     * as it manually calls getMyDirections. When this is being called > 1 times every tick or update, this should not
     * be used, and instead, the fast version should be called to prevent the performance hit of calling
     * getMyDirections() repeatedly.
     * @param actualState The actual state for the block
     * @param direction The first direction
     * @param opposite The second direction, usually the opposite.
     */
    public boolean shouldStretchInDirection(IBlockState actualState, PropertyBool direction, PropertyBool opposite) {
        return shouldStretchInDirection(actualState.getValue(direction), actualState.getValue(opposite), getMyDirections().size());
    }

    /**
     * A variant of shouldStretchInDirection that does not actually manually get all of the values, because, particularly
     * with the getMyDirections method, it might cause some serious performance hits. Recommended usage:
     * ```
     * boolean hasDown = actualState.getValue(DOWN);
     * boolean hasUp = actualState.getValue(UP);
     * int numDirs = pipe.getMyDirections().size();
     * shouldStretchInDirection(hasDown, hasUp, numDirs);
     * ```
     *
     * This method is simply a wrapper for a very common if statement in relation to pipe bounding and collision boxes.
     *
     * This method also has the benefit of being static, which the other shouldStretchInDirection method is not.
     *
     * @param hasDir Whether it is connected at this dir
     * @param hasOpposite Whether it is connected at this dir, usually the opposite of the dir used in hasDir
     * @param numDirs The number of directions that this pipe is connected at.
     */
    public static boolean shouldStretchInDirection(boolean hasDir, boolean hasOpposite, int numDirs) {
        return hasDir || (hasOpposite && numDirs == 1);
    }

    public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {
        float min = BlockSteamPipe.BASE_MIN;
        float max = BlockSteamPipe.BASE_MAX;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        IBlockState actualState = world.getBlockState(pos).getActualState(world, pos);
        boolean hasDown = actualState.getValue(BlockSteamPipe.DOWN);
        boolean hasUp = actualState.getValue(BlockSteamPipe.UP);
        boolean hasNorth = actualState.getValue(BlockSteamPipe.NORTH);
        boolean hasSouth = actualState.getValue(BlockSteamPipe.SOUTH);
        boolean hasWest = actualState.getValue(BlockSteamPipe.WEST);
        boolean hasEast = actualState.getValue(BlockSteamPipe.EAST);
        int numDirs = getMyDirections().size();
        if (shouldStretchInDirection(hasDown, hasUp, numDirs)) {
            int connectDown = canConnectSide(EnumFacing.DOWN);
            float bottom = connectDown == 2 ? -5F / 16F : 0F;
            cuboids.add(new IndexedCuboid6(0, new Cuboid6(x + min, y + bottom, z + min, x + max, y + max, z + max)));
        }
        if (shouldStretchInDirection(hasUp, hasDown, numDirs)) {
            int connectUp = canConnectSide(EnumFacing.UP);
            float top = connectUp == 2 ? 21F / 16F : 1F;
            cuboids.add(new IndexedCuboid6(1, new Cuboid6(x + min, y + min, z + min, x + max, y + top, z + max)));
        }
        if (shouldStretchInDirection(hasNorth, hasSouth, numDirs)) {
            int connectNorth = canConnectSide(EnumFacing.NORTH);
            float bottom = connectNorth == 2 ? -5F / 16F : 0F;
            cuboids.add(new IndexedCuboid6(2, new Cuboid6(x + min, y + min, z + bottom, x + max, y + max, z + max)));
        }
        if (shouldStretchInDirection(hasSouth, hasNorth, numDirs)) {
            int connectSouth = canConnectSide(EnumFacing.SOUTH);
            float top = connectSouth == 2 ? 21F / 16F : 1F;
            cuboids.add(new IndexedCuboid6(3, new Cuboid6(x + min, y + min, z + min, x + max, y + max, z + top)));
        }
        if (shouldStretchInDirection(hasWest, hasEast, numDirs)) {
            int connectWest = canConnectSide(EnumFacing.WEST);
            float bottom = connectWest == 2 ? -5F / 16F : 0F;
            cuboids.add(new IndexedCuboid6(4, new Cuboid6(x + bottom, y + min, z + min, x + max, y + max, z + max)));
        }
        if (shouldStretchInDirection(hasEast, hasWest, numDirs)) {
            int connectEast = canConnectSide(EnumFacing.EAST);
            float top = connectEast == 2 ? 21F / 16F : 1F;
            cuboids.add(new IndexedCuboid6(5, new Cuboid6(x + min, y + min, z + min, x + top, y + max, z + max)));
        }
        cuboids.add(new IndexedCuboid6(6, new Cuboid6(x + min, y + min, z + min, x + 12F / 16F, y + 12F / 16F, z + 12F / 16F)));
    }

    @Override
    public boolean onWrench(ItemStack stack, Player player, Level level, BlockPos pos, HumanoidArm hand, Direction facing, BlockState state, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) {
            if (this.disguiseBlock != null) {
                if (!player.capabilities.isCreativeMode) {
                    EntityItem entityItem = new EntityItem(level, player.posX, player.posY, player.posZ, new ItemStack(disguiseBlock, 1, disguiseMeta));
                    level.spawnEntity(entityItem);
                }
                SoundType sound = disguiseBlock.getSoundType();
                level.playSound((double) pos.getX() + 0.5F, (double) (pos.getY() + 0.5F),
                  (double) (pos.getZ() + 0.5F), sound.getBreakSound(), SoundCategory.BLOCKS,
                  (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F, false);
                disguiseBlock = null;
                markDirty();
                return true;
            }
        } else {
            if (this.world.isRemote) {
                RayTraceResult hit = RayTracer.retraceBlock(level, player, pos);
                // Use ryatracer to get the subpart that was hit. The # corresponds with a forge direction.
                if (hit == null) {
                    return false;
                }

                ConnectPacket packet = new ConnectPacket(pos, hit.subHit);
                EsteemedInnovation.channel.sendToServer(packet);
            }
        }
        return false;
    }

    public void connectDisconnect(World world, BlockPos pos, int subHit) {
        // Use raytracer to get the subpart that was hit. The # corresponds with a forge direction.
        // If hit a part from 0 to 5 (direction) and hit me

        IBlockState state = world.getBlockState(pos);
        if ((subHit >= 0) && (subHit < 6) && state instanceof BlockSteamPipe) {
            //Make sure that you can't make an 'end cap' by allowing less than 2 directions to connect
            int sidesConnect = 0;
            for (int i = 0; i < 6; i++) {
                if (doesConnect(EnumFacing.byIndex(i))) {
                    sidesConnect++;
                }
            }
            boolean netChange = false;
            //If does connect on this side, and has adequate sides left
            EnumFacing direction = EnumFacing.byIndex(subHit);
            if (doesConnect(direction)) {
                BlockPos offsetPos = getRelativePos(direction);
                TileEntity tile = world.getTileEntity(offsetPos);
                if (tile instanceof TileEntitySteamPipe && ((TileEntitySteamPipe) tile).blacklistedSides.contains(direction.getOpposite().ordinal())) {
                    TileEntitySteamPipe pipe = (TileEntitySteamPipe) tile;
                    pipe.blacklistedSides.remove((Integer) direction.getOpposite().ordinal());

                    //network stuff

                    pipe.shouldJoin();
                    pipe.isOtherPipe = true;
                    //pipe.getNetwork().addSteam(steam);
                    world.notifyBlockUpdate(offsetPos, world.getBlockState(offsetPos), world.getBlockState(offsetPos), 0);
                } else if (sidesConnect > 2) {
                    //add to blacklist
                    blacklistedSides.add(subHit);
                    {
                        if (tile != null && tile instanceof SteamTransporter) {
                            SteamTransporter p = (SteamTransporter) tile;
                            SteamNetwork network = p.getNetwork();
                            if (network != null) {
                                network.shouldRefresh();
                            }
                        }
                    }
                    isOriginalPipe = true;
                    //bad network stuff
                    int steam = getNetwork().split(this, false);
                    shouldJoin();
                    //this.getNetwork().addSteam(steam);

                    refreshNeighbors();
                    network.shouldRefresh();
                    markForResync();
                }
            }
            //else if doesn't connect
            else if (!doesConnect(direction)) {
                if (blacklistedSides.contains(subHit)) {
                    //remove from whitelist
                    blacklistedSides.remove((Integer) subHit);
                    //network stuff
                    int steam = getNetwork().split(this, false);
                    shouldJoin();
                    //this.getNetwork().addSteam(steam);
                    ////EsteemedInnovation.log.debug("C");
                    ////EsteemedInnovation.log.debug(this.getNetworkName());
                    ////EsteemedInnovation.log.debug("steam: "+steam+"; nw steam: "+this.getNetwork().getSteam());
                    markForResync();
                }
            }
//            if (getSteamShare() > 0) {
                //world.playSoundEffect(x+0.5F, y+0.5F, z+0.5F, EsteemedInnovation.MOD_ID + ":leaking", 2.0F, 0.9F);
//            }
            world.playSound(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, EsteemedInnovation.SOUND_WRENCH,
              SoundCategory.BLOCKS, 2F, 0.9F, false);
        }
    }

    @Override
    public void refresh() {
        super.refresh();
        isOriginalPipe = false;
        isOtherPipe = false;
    }

    private void refreshNeighbors() {
        //log.debug("Refreshing neighbors");
        for (EnumFacing dir : EnumFacing.VALUES) {
            TileEntity te = world.getTileEntity(getRelativePos(dir));
            if (te != null && te instanceof SteamTransporter) {
                SteamTransporter trans = (SteamTransporter) te;
                SteamNetwork transNetwork = trans.getNetwork();
                if (transNetwork != null && transNetwork != getNetwork()) {
                    transNetwork.shouldRefresh();
                }
            }
        }
    }
}
