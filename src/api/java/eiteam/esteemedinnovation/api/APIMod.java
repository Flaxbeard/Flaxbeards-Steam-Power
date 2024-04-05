package eiteam.esteemedinnovation.api;

import com.mojang.serialization.Codec;
import eiteam.esteemedinnovation.api.exosuit.ExosuitEventDelegator;
import eiteam.esteemedinnovation.api.tool.ItemSteamTool;
import eiteam.esteemedinnovation.api.tool.SteamToolLootModifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import static eiteam.esteemedinnovation.api.Constants.API_MODID;

@Mod(API_MODID)
public class APIMod {
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM_REGISTRAR = DeferredRegister.create(NeoForgeRegistries.GLOBAL_LOOT_MODIFIER_SERIALIZERS, API_MODID);
    private static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<SteamToolLootModifier>> STEAM_TOOL_LOOT_MODIFIER = GLM_REGISTRAR.register("steam_tool_loot_modifier", SteamToolLootModifier.CODEC);

    public APIMod(IEventBus modBus) {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, APIConfig.SPEC);
        NeoForge.EVENT_BUS.register(new ItemSteamTool.ToolUpgradeEventDelegator());
        NeoForge.EVENT_BUS.register(new ExosuitEventDelegator());

        GLM_REGISTRAR.register(modBus);
    }
}
