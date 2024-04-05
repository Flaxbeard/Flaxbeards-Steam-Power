package eiteam.esteemedinnovation.api.steamnet;

import eiteam.esteemedinnovation.api.Constants;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.TickEvent;

import java.util.*;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, modid = Constants.API_MODID)
public class SteamNetworkRegistry {
    /**
     * Key: Dimension, Value: All networks in that dimension.
     */
    private static final Map<ResourceLocation, List<SteamNetwork>> networks = new HashMap<>();

    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent e) {
        for (List<SteamNetwork> nets : networks.values()) {
            nets.removeIf(net -> !net.tick());
        }
    }

    public static SteamNetwork getNewNetwork() {
        SteamNetwork net = new SteamNetwork();
        String name = UUID.randomUUID().toString();
        net.setName(name);
        return net;
    }

    public static void add(SteamNetwork network) {
        if (!networks.containsKey(network.getDimension())) {
            networks.put(network.getDimension(), new ArrayList<>());
        }
        List<SteamNetwork> dimension = networks.get(network.getDimension());
        dimension.add(network);
    }

    public static void remove(SteamNetwork network) {
        if (networks.containsKey(network.getDimension())) {
            List<SteamNetwork> dimension = networks.get(network.getDimension());
            dimension.remove(network);
        }
    }
}
