package eiteam.esteemedinnovation.api.tool;


import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class ToolUpgradeRegistry {
    /**
     * Map of the upgrades and their according texture locations. This is used primarily for model rendering, but could
     * be potentially useful in any case where every single upgrade is needed (or its textures).
     */
    private static Map<SteamToolUpgrade, ResourceLocation[]> upgrades = new HashMap<>();

    /**
     * Registers an {@link SteamToolUpgrade} to the {@link ToolUpgradeRegistry#upgrades} map.
     * @param upgrade The upgrade to register. Uses {@link SteamToolUpgrade#getBaseIcon()} to determine the locations
     *                to register. For universal upgrades ({@link SteamToolUpgrade#isUniversal()} returns {@code true}),
     *                it will use {@code Drill0}, {@code Drill1}, {@code Axe0}, {@code Axe1}, {@code Shovel0},
     *                and {@code Shovel1} accordingly. Otherwise, it will use {@code Drill}, {@code Axe}, or
     *                {@code Shovel} according to what is returned by {@link SteamToolUpgrade#getToolSlot()}.
     */
    public static void register(SteamToolUpgrade upgrade) {
        List<ResourceLocation> textures = new ArrayList<>();
        ResourceLocation base = upgrade.getBaseIcon();
        if (base != null) {
            String baseDomain = base.getNamespace();
            String basePath = base.getPath();
            if (upgrade.isUniversal()) {
                textures.add(new ResourceLocation(baseDomain, basePath + "_drill0"));
                textures.add(new ResourceLocation(baseDomain, basePath + "_drill1"));
                textures.add(new ResourceLocation(baseDomain, basePath + "_axe0"));
                textures.add(new ResourceLocation(baseDomain, basePath + "_axe1"));
                textures.add(new ResourceLocation(baseDomain, basePath + "_shovel0"));
                textures.add(new ResourceLocation(baseDomain, basePath + "_shovel1"));
            } else {
                SteamToolSlot slot = upgrade.getToolSlot();
                String tool = "drill";
                switch (slot.tool) {
                    case 1: {
                        tool = "axe";
                        break;
                    }
                    case 2: {
                        tool = "shovel";
                        break;
                    }
                    default: {
                        break;
                    }
                }
                textures.add(new ResourceLocation(baseDomain, basePath + "_" + tool + "0"));
                textures.add(new ResourceLocation(baseDomain, basePath + "_" + tool + "1"));
            }
        }

        upgrades.put(upgrade, textures.toArray(new ResourceLocation[textures.size()]));
    }

    public static ResourceLocation[] getResources(SteamToolUpgrade upgrade) {
        return upgrades.get(upgrade);
    }

    public static Set<SteamToolUpgrade> getUpgrades() {
        return upgrades.keySet();
    }
}
