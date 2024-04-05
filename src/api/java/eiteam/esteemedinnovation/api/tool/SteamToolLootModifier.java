package eiteam.esteemedinnovation.api.tool;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

/**
 * Triggered when an item tagged as {@link eiteam.esteemedinnovation.api.Constants#UPGRADEABLE_TOOLS} breaks a block.
 * Delegates to the contained {@link SteamToolUpgrade}s to get modified loot.
 */
public class SteamToolLootModifier extends LootModifier {
    public static final Supplier<Codec<SteamToolLootModifier>> CODEC = Suppliers.memoize(() -> RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, SteamToolLootModifier::new)));

    public SteamToolLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        ItemStack tool = context.getParam(LootContextParams.TOOL);
        if (tool.getItem() instanceof SteamTool && ((SteamTool) tool.getItem()).isWound(tool)) {
            for (ItemStack upgradeStack : UtilSteamTool.getUpgradeStacks(tool)) {
                ((SteamToolUpgrade) upgradeStack.getItem()).modifyLoot(generatedLoot, context);
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC.get();
    }
}
