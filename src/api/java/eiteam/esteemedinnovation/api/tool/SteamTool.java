package eiteam.esteemedinnovation.api.tool;

import eiteam.esteemedinnovation.api.SteamChargable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Important note: Upgradeable steam tools must be in the item tag {@link eiteam.esteemedinnovation.api.Constants#UPGRADEABLE_TOOLS}
 * in order for them to modify loot.
 */
public interface SteamTool extends SteamChargable {
    /**
     * Checks if the tool is wound up.
     * @param stack The tool
     * @return Whether the tool has been wound up.
     */
    boolean isWound(ItemStack stack);

    /**
     * Checks if the tool has a particular upgrade.
     * @param me The ItemStack version of the tool
     * @param check The item that is being checked against, or the upgrade
     * @return Whether it has any upgrades.
     */
    boolean hasUpgrade(ItemStack me, Item check);

    /**
     * @return The Vanilla tool class associated with this tool.
     */
    String toolClass();
}
