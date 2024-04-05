package eiteam.esteemedinnovation.api;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.Item;

public class Constants {
    public static final String API_MODID = "esteemedinnovationapi";
    /**
     * Items being in this tag will cause steam_tool_upgrade_modifier global loot modifier to be executed.
     */
    public static final TagKey<Item> UPGRADEABLE_TOOLS = ItemTags.create(new ResourceLocation(API_MODID, "upgradeable_tools"));

    public static final String EI_MODID = "esteemedinnovation";
    public static final ResourceLocation ENG_GUI_TEXTURES = new ResourceLocation(EI_MODID + ":textures/gui/engineering.png");
    public static final ResourceLocation ENG_ARMOR_TEXTURES = new ResourceLocation(EI_MODID + ":textures/gui/engineering2.png");

    private static final ResourceKey<DamageType> STEAMED_DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(API_MODID, "steamed"));

    /**
     * @param access Can generally be obtained from the Level object.
     * @return A generic steamed DamageSource.
     */
    public static DamageSource steamedDamage(RegistryAccess access) {
        return new DamageSource(access.registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(STEAMED_DAMAGE_TYPE));
    }

}
