package eiteam.esteemedinnovation.api.steamnet;

import eiteam.esteemedinnovation.api.APIConfig;
import eiteam.esteemedinnovation.api.SteamTransporter;
import eiteam.esteemedinnovation.api.util.Coord4;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.*;

public class SteamNetwork {
    private static Random random = new Random();
    private int refreshWaitTicks = 0;
    private int globalRefreshTicks = 300;
    private String name;
    private int steam;
    private int capacity;
    private boolean shouldRefresh = false;
    private Coord4[] transporterCoords;
    private int initializedTicks = 0;
    private HashMap<Coord4, SteamTransporter> transporters = new HashMap<>();

    public SteamNetwork() {
        this.steam = 0;
        this.capacity = 0;
    }

    public SteamNetwork(int capacity) {
        this.capacity = capacity;
    }

    public SteamNetwork(int capacity, String name, ArrayList<Coord4> coordList) {
        this(capacity);
        for (Coord4 c : coordList) {
            transporters.put(c, null);
        }
        this.name = name;
    }

    public static SteamNetwork newOrJoin(SteamTransporter trans) {
        if (!trans.canSteamPassThrough()) {
            return null;
        }

        HashSet<SteamTransporter> others = getNeighboringTransporters(trans);
        HashSet<SteamNetwork> nets = new HashSet<>();
        SteamNetwork theNetwork = null;
        boolean hasJoinedNetwork = false;
        if (others.size() > 0) {
            others.stream()
              .filter(t -> t.canSteamPassThrough() && t.getNetwork() != null)
              .forEach(t -> nets.add(t.getNetwork()));

            if (nets.size() > 0) {
                SteamNetwork main = null;
                for (SteamNetwork net : nets) {
                    if (main != null) {
                        main.join(net);
                    } else {
                        main = net;
                    }
                }

                if (main != null) {
                    main.addTransporter(trans);
                }
                hasJoinedNetwork = true;
                theNetwork = main;
            }
        }

        if (!hasJoinedNetwork) {
            SteamNetwork net = SteamNetworkRegistry.getNewNetwork();
            net.addTransporter(trans);
            SteamNetworkRegistry.add(net);
            theNetwork = net;
        }
        return theNetwork;
    }

    public static HashSet<SteamTransporter> getNeighboringTransporters(SteamTransporter trans) {
        HashSet<SteamTransporter> out = new HashSet<>();
        Coord4 transCoords = trans.getCoords();
        for (Direction d : trans.getConnectionSides()) {
            BlockEntity be = trans.getLevelObj().getBlockEntity(transCoords.pos().relative(d));
            if (be instanceof SteamTransporter t && t != trans && t.getConnectionSides().contains(d.getOpposite())) {
                out.add(t);
            }
        }
        return out;
    }

    public String getName() {
        return name;
    }

    protected void setName(String name) {
        this.name = name;
    }

    protected boolean tick() {
        if (transporters.size() == 0) {
            return false;
        }
        if (shouldRefresh) {
            if (refreshWaitTicks > 0) {
                refreshWaitTicks--;
            } else {
                refresh();
                shouldRefresh = false;
            }

        }

        if (globalRefreshTicks > 0) {
            globalRefreshTicks--;
        } else {
            refresh();
            globalRefreshTicks = 300;
        }
        if (initializedTicks >= 1200) {

            if (APIConfig.SAFE_MODE.get()) {
                if (getPressure() > 1.09F) {
                    steam = (int) Math.floor(capacity * 1.09D);
                }
            } else {
                if (transporters != null) {
                    if (getPressure() > 1.2F) {
                        Iterator<Map.Entry<Coord4, SteamTransporter>> iter = transporters.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry<Coord4, SteamTransporter> entry = iter.next();
                            SteamTransporter trans = entry.getValue();
                            if ((trans == null || ((BlockEntity) trans).isRemoved())) {
                                iter.remove();
                            } else if (!trans.getLevelObj().isClientSide() && shouldExplode(oneInX(getPressure(), trans.getPressureResistance()))) {
                                trans.shouldExplode();
                            }
                        }
                    }
                } else {
                    return false;
                }
            }
        } else {
            initializedTicks++;
        }
        return true;
    }

    public void addSteam(int amount) {
        steam += amount;
        shouldRefresh();
    }

    public void decrSteam(int amount) {
        steam -= amount;
        if (steam < 0) {
            steam = 0;
        }
    }

    /**
     * @return The total amount of steam currently contained in the network.
     */
    public int getSteam() {
        return steam;
    }

    /**
     * @return The total capacity of steam that the steam network can hold.
     */
    public int getCapacity() {
        return capacity;
    }

    private int oneInX(float pressure, float resistance) {
        return Math.max(1, (int) Math.floor((double) (500.0F - (pressure / (1.1F + resistance) * 100))));
    }

    private boolean shouldExplode(int oneInX) {
        return oneInX <= 1 || random.nextInt(oneInX - 1) == 0;
    }

    /**
     * @return The total pressure of the network. Calculated as steam / capacity in the default implementation.
     *         In the default implementation 1.2F pressure is considered dangerously high and capable of exploding.
     */
    public float getPressure() {
        float capacity = getCapacity();
        return capacity > 0 ? getSteam() / capacity : 0;

    }

    public int getSize() {
        return transporters.size();
    }

    public void addTransporter(SteamTransporter trans) {
        if (trans != null && !this.contains(trans)) {
            this.capacity += trans.getCapacity();
            Coord4 transCoords = trans.getCoords();
            transporters.put(transCoords, trans);
            trans.setNetworkName(getName());
            trans.setNetwork(this);
            addSteam(trans.getSteam());
            trans.wasAdded();
        }
    }

    public void setTransporterCoords(Coord4[] coords) {
        transporterCoords = coords;
    }

    public void init(Level level) {
        if (transporterCoords != null) {
            this.loadTransporters(level);
        }
    }

    public void loadTransporters(Level level) {
        for (int i = transporterCoords.length - 1; i >= 0; i--) {
            Coord4 coords = transporterCoords[i];
            BlockEntity be = level.getBlockEntity(coords.pos());
            if (be instanceof SteamTransporter) {
                transporters.put(transporterCoords[i], (SteamTransporter) be);
            }

        }
    }

    public int split(SteamTransporter split, boolean removeCapacity) {
        int steamRemoved = 0;
        if (removeCapacity && getSteam() >= split.getCapacity() * getPressure()) {
            steamRemoved = (int) Math.floor(split.getCapacity() * getPressure());
            steam -= steamRemoved;
            capacity -= split.getCapacity();
        }
        for (SteamTransporter trans : transporters.values()) {
            trans.updateSteam((int) (trans.getCapacity() * getPressure()));
        }

        //World world = split.getWorldObj();
        //Tuple3<Integer, Integer, Integer> coords = split.getCoords();
        //int x = coords.first, y= coords.second, z=coords.third;
        //HashSet<EnumFacing> dirs = split.getConnectionSides();
        Set<SteamNetwork> newNets = new HashSet<>();
        for (SteamTransporter trans : getNeighboringTransporters(split)) {
            if (trans.canSteamPassThrough()) {
                boolean isInNetwork = false;
                if (newNets.size() > 0) {
                    for (SteamNetwork net : newNets) {
                        if (net.contains(trans)) {
                            isInNetwork = true;
                            break;
                        }
                    }
                }
                if (!isInNetwork) {
                    SteamNetwork net = SteamNetworkRegistry.getNewNetwork();
                    SteamTransporter ignore = null;
                    if (removeCapacity) {
                        ignore = split;
                    }

                    net.buildFromTransporter(trans, net, ignore);
                    newNets.add(net);
                }
            }

        }
        if (newNets.size() > 0) {
            for (SteamNetwork net : newNets) {
                SteamNetworkRegistry.add(net);
                net.shouldRefresh();
            }
        }

        shouldRefresh();
        return steamRemoved;
    }

    public void buildFromTransporter(SteamTransporter trans, SteamNetwork target, SteamTransporter ignore) {
        //////EsteemedInnovation.log.debug("Building network!");
        HashSet<SteamTransporter> checked = new HashSet<>();
        HashSet<SteamTransporter> members = target.crawlNetwork(trans, checked, ignore);
        boolean targetIsThis = target == this;
        SteamNetwork net = targetIsThis ? this : SteamNetworkRegistry.getNewNetwork();
        for (SteamTransporter member : members) {
            if (!transporters.containsValue(member)) {
                target.addTransporter(member);
            }
        }
        net.addTransporter(trans);
    }

    public boolean contains(SteamTransporter trans) {
        return transporters.containsValue(trans);
    }

    protected HashSet<SteamTransporter> crawlNetwork(SteamTransporter trans, HashSet<SteamTransporter> checked, SteamTransporter ignore) {
        if (checked == null) {
            checked = new HashSet<>();
        }
        if (!checked.contains(trans) && trans.canSteamPassThrough()) {
            checked.add(trans);
        }
        HashSet<SteamTransporter> neighbors = getNeighboringTransporters(trans);
        for (SteamTransporter neighbor : neighbors) {
            //log.debug(neighbor == ignore ? "Should ignore this." : "Should not be ignored");

            if (!checked.contains(neighbor) && neighbor != ignore && trans.canSteamPassThrough()) {
                //log.debug("Didn't ignore");
                checked.add(neighbor);
                crawlNetwork(neighbor, checked, ignore);
            }
        }
        return checked;
    }

    public void join(SteamNetwork other) {
        for (SteamTransporter trans : other.transporters.values()) {
            addTransporter(trans);
        }
        //this.steam += other.getSteam();
        SteamNetworkRegistry.remove(other);
    }

    public ResourceLocation getDimension() {
        if (transporters.size() > 0) {
            return transporters.keySet().iterator().next().dimension();
        } else {
            return null;
        }

    }

    public Level getLevel() {
        if (transporters.values().iterator().hasNext() && transporters.values().iterator().next() != null) {
            return transporters.values().iterator().next().getLevelObj();
        } else {
            return null;
        }
    }

    public void refresh() {
        float press = getPressure();
        int targetCapacity = 0;
        if (transporters.size() == 0) {
            SteamNetworkRegistry.remove(this);
            return;
        }
        HashMap<Coord4, SteamTransporter> temp = (HashMap<Coord4, SteamTransporter>) transporters.clone();
        for (Coord4 c : temp.keySet()) {
            BlockEntity be = c.getBlockEntity(getLevel());
            if (be == null || !(be instanceof SteamTransporter)) {
                transporters.remove(c);
            } else {
                SteamTransporter trans = (SteamTransporter) be;
                if (trans.getNetwork() != this) {
                    transporters.remove(c);
                    steam -= getPressure() * trans.getCapacity();
                    transporters.remove(c);
                } else {
                    targetCapacity += trans.getCapacity();
                }
            }
        }
        int currentCapacity = getCapacity();
        float currentPressure = getPressure();
        if (currentCapacity != targetCapacity) {
            int idealSteam = (int) (targetCapacity * press);
            steam = idealSteam;
            capacity = targetCapacity;
        }
    }

    public void shouldRefresh() {
        //log.debug(this.name+": I should refresh");
        shouldRefresh = true;
        refreshWaitTicks = 40;
    }

    public String getShortName() {
        return name.subSequence(0, 5).toString();
    }

}
