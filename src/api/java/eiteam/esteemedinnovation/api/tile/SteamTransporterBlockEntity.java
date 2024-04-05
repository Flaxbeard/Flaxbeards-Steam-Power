package eiteam.esteemedinnovation.api.tile;

import eiteam.esteemedinnovation.api.SteamTransporter;
import eiteam.esteemedinnovation.api.steamnet.SteamNetwork;
import eiteam.esteemedinnovation.api.util.Coord4;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import java.util.*;

public abstract class SteamTransporterBlockEntity extends BlockEntityTickableSafe implements SteamTransporter {
    public String name = "SteamTransporterTileEntity";
    private float pressureResistance = 0.8F;
    private float lastPressure = -1F;
    private float pressure;
    protected int capacity;
    protected String networkName;
    protected SteamNetwork network;
    protected Direction[] distributionDirections;
    private boolean shouldJoin = false;
    private int steam = 0;
    private List<Direction> gaugeSideBlacklist = new ArrayList<>();
    private boolean shouldExplode;
    private boolean hasExploded;

    public SteamTransporterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, Direction.values());
    }

    public SteamTransporterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Direction[] distributionDirections) {
        this(type, pos, state, 10_000, distributionDirections);
    }

    public SteamTransporterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity, Direction[] distributionDirections) {
        super(type, pos, state);
        this.distributionDirections = distributionDirections;
        this.capacity = capacity;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void readSteamData(CompoundTag tag) {
        if (tag.contains("NetworkName")) {
            networkName = tag.getString("NetworkName");
            pressure = tag.getFloat("Pressure");
            steam = tag.getInt("Steam");
        }
    }

    private void writeSteamData(CompoundTag tag) {
        if (networkName != null) {
            tag.putString("NetworkName", networkName);
            tag.putFloat("Pressure", getPressure());
            tag.putFloat("Steam", steam);
        }
    }

    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        writeSteamData(tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        handleUpdateTag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        readSteamData(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        writeSteamData(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        readSteamData(tag);
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public float getPressure() {
        SteamNetwork net = getNetwork();
        return net == null ? pressure : net.getPressure();
    }

    @Override
    public void setPressure(float pressure) {
        this.pressure = pressure;
    }

    @Override
    public void initialUpdate() {
        refresh();
        setInitialized(true);
    }

    @Override
    public void safeUpdate() {
        if (shouldJoin) {
            refresh();
        }
        if (!level.isClientSide()) {
            if (steam != getSteamShare()) {
                steam = getSteamShare();
                markDirty();
            }
            SteamNetwork net = getNetwork();
            if (hasGauge() && net != null) {
                if (Math.abs(getPressure() - lastPressure) > 0.01F) {
                    //EsteemedInnovation.log.debug("Updating PRESHAAA");
                    markForResync();
                    lastPressure = getPressure();
                }
            }
            if (shouldExplode) {
                shouldExplode = false;
                explode();
            }
        }
    }

    @Override
    public void insertSteam(int amount, Direction face) {
        SteamNetwork net = getNetwork();
        if (net != null) {
            net.addSteam(amount);
        }
    }

    @Override
    public void decrSteam(int i) {
        SteamNetwork net = getNetwork();
        if (net != null && net.getSteam() != 0) {
            net.decrSteam(i);
        }
    }

    @Override
    public void explode() {
        SteamNetwork net = getNetwork();
        net.decrSteam((int) (net.getSteam() * 0.1F));
        net.split(this, true);
        level.explode(null, worldPosition.getX() + 0.5F, worldPosition.getY() + 0.5F, worldPosition.getZ() + 0.5F, 4F, Level.ExplosionInteraction.TNT);
    }

    @Override
    public void shouldExplode() {
        shouldExplode = true;
    }

    private boolean isValidSteamSide(Direction face) {
        for (Direction d : distributionDirections) {
            if (d == face) {
                return true;
            }
        }
        return false;
    }

    protected void addSideToGaugeBlacklist(Direction face) {
        gaugeSideBlacklist.add(face);
    }

    public void addSidesToGaugeBlacklist(Direction[] faces) {
        for (Direction face : faces) {
            addSideToGaugeBlacklist(face);
        }
    }

    @Override
    public boolean doesConnect(Direction face) {
        return isValidSteamSide(face);
    }

    @Override
    public boolean acceptsGauge(Direction face) {
        return !gaugeSideBlacklist.contains(face);
    }

    @Override
    public float getPressureResistance() {
        return pressureResistance;
    }

    protected void setPressureResistance(float resistance) {
        pressureResistance = resistance;
    }

    /**
     * Sets the distribution directions to everything except the provided directions.
     * @param exclusions The directions to exclude form the distribution directions.
     */
    protected void setValidDistributionDirectionsExcluding(Direction... exclusions) {
        Direction[] validDirs = new Direction[6 - exclusions.length];
        int i = 0;
        List<Direction> exclusionList = Arrays.asList(exclusions);
        for (Direction dir : Direction.values()) {
            if (!exclusionList.contains(dir)) {
                validDirs[i] = dir;
                i++;
            }
        }
        setDistributionDirections(validDirs);
    }

    protected void setDistributionDirections(Direction[] faces) {
        distributionDirections = faces;
    }

    @Override
    public Set<Direction> getConnectionSides() {
        HashSet<Direction> out = new HashSet<>();
        out.addAll(Arrays.asList(distributionDirections));
        return out;
    }

    @Override
    public Coord4 getCoords() {
        return new Coord4(worldPosition, level.dimension().location());
    }

    @Override
    public void setNetworkName(String name) {
        networkName = name;
    }

    @Override
    public SteamNetwork getNetwork() {
        return network;
    }

    @Override
    public void setNetwork(SteamNetwork network) {
        this.network = network;
    }

    @Override
    public int getSteamShare() {
        SteamNetwork net = getNetwork();
        if (net != null) {
            return (int) (Math.floor((double) getCapacity() * (double) net.getPressure()));
        }
        return 0;
    }

    @Override
    public int getSteam() {
        return steam;
    }

    public boolean hasGauge() {
        for (Direction dir : Direction.values()) {
            if (acceptsGauge(dir)) {
                BlockEntity tile = level.getBlockEntity(getRelativePos(dir));
                if (tile instanceof SteamReactorBlockEntity) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void refresh() {
        /*
        if (!world.isRemote) {
            FMLRelaunchLog.info("Refreshing", null);
        }
        */
        if (getNetwork() == null && !level.isClientSide()) {
            /*
            EsteemedInnovation.log.debug("Null network");
            if (this.networkName != null && SteamNetworkRegistry.getInstance().isInitialized(this.getDimension())){
                EsteemedInnovation.log.debug("I have a network!");
                this.network = SteamNetworkRegistry.getInstance().getNetwork(this.networkName, this);
                this.network.rejoin(this);
            } else {
                EsteemedInnovation.log.debug("Requesting new network or joining existing.en");
            }
            */
            SteamNetwork.newOrJoin(this);
            markForResync();
        }
    }

    @Override
    public ResourceLocation getDimension() {
        return level.dimension().location();
    }

    @Nonnull
    @Override
    public Level getLevelObj() {
        return level;
    }

    @Override
    public void updateSteam(int amount) {
        steam = amount;
    }

    protected void shouldJoin() {
        shouldJoin = true;
    }

    @Nonnull
    @Override
    public String getName() {
        return name;
    }

    @Override
    public void wasAdded() {}
}
