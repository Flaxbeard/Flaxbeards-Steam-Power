package eiteam.esteemedinnovation.api.tool;

import eiteam.esteemedinnovation.api.ChargableUtility;
import eiteam.esteemedinnovation.api.Constants;
import eiteam.esteemedinnovation.api.Engineerable;
import eiteam.esteemedinnovation.api.SteamChargable;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.TierSortingRegistry;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;

public abstract class ItemSteamTool extends DiggerItem implements Engineerable, SteamTool {
    private boolean hasBrokenBlock = false;
    protected static final ResourceLocation LARGE_ICONS = new ResourceLocation(Constants.EI_MODID + ":textures/gui/engineering2.png");
    private IdentityHashMap<ItemStack, MutablePair<Integer, Integer>> ticksSpeed = new IdentityHashMap<>();

    protected ItemSteamTool(float attackDamage, float attackSpeed, Tier tier, TagKey<Block> effectiveBlocks, Item.Properties properties) {
        super(attackDamage, attackSpeed, tier, effectiveBlocks, properties);
    }

    @Override
    public boolean isCorrectToolForDrops(@Nonnull ItemStack stack, BlockState state) {
        if (!state.is(blocks)) {
            return false;
        }
        for (ItemStack upgradeStack : UtilSteamTool.getUpgradeStacks(stack)) {
            SteamToolUpgrade upgrade = (SteamToolUpgrade) upgradeStack.getItem();
            if (upgrade.modifiesToolTier()) {
                Tier upgradedTier = upgrade.getToolTier(state, stack, upgradeStack);
                if (TierSortingRegistry.isCorrectTierForDrops(upgradedTier, state)) {
                    return true;
                }
            }
        }
        return TierSortingRegistry.isCorrectTierForDrops(getTier(), state);
    }

    @Override
    public boolean shouldCauseReequipAnimation(@Nonnull ItemStack oldStack, @Nonnull ItemStack newStack, boolean slotChanged) {
        /*
         We have to check the upgrades so that the models reload when you switch between two tools of the same type with
         different upgrades. Otherwise, it would appear to have oldStack's upgrades on it.
          */
        return !UtilSteamTool.getUpgrades(oldStack).equals(UtilSteamTool.getUpgrades(newStack));
    }

    @Override
    public void appendHoverText(@Nonnull ItemStack me, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flag) {
        super.appendHoverText(me, level, tooltip, flag);
        tooltip.add(Component.literal((me.getMaxDamage() - me.getDamageValue()) * steamPerDurability() + "/" + me.getMaxDamage() * steamPerDurability() + " SU").withStyle(ChatFormatting.WHITE));
        tooltip.addAll(UtilSteamTool.getUpgradeTooltipComponents(me, getRedSlot()));
    }

    @Override
    public boolean mineBlock(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockPos pos, @Nonnull LivingEntity entityLiving) {
        CompoundTag nbt = UtilSteamTool.checkNBT(stack);
        if (ticksSpeed.containsKey(stack)) {
            MutablePair<Integer, Integer> pair = ticksSpeed.get(stack);
            nbt.putInt("Ticks", pair.getLeft());
            nbt.putInt("Speed", pair.getRight());
            hasBrokenBlock = true;
            ticksSpeed.remove(stack);
        }
        return true;
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level level, @Nonnull Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player) {
            CompoundTag nbt = UtilSteamTool.checkNBT(stack);
            int ticks = nbt.getInt("Ticks");
            int speed = nbt.getInt("Speed");

            if (hasBrokenBlock) {
                speed -= 10;
                hasBrokenBlock = false;
            }
            int addedTicks = Math.min(((Double) Math.floor(speed / 1000D * 25D)).intValue(), 50);
            ticks += addedTicks;
            if (isWound(stack)) {
                speed--;
            } else if (ticks <= 0) {
                ticks = 0;
            } else {
                ticks--;
            }

            ticks %= 100;
            if (player.swinging) {
                if (ticksSpeed.containsKey(stack)) {
                    ticksSpeed.get(stack).setLeft(ticks);
                    ticksSpeed.get(stack).setRight(speed);
                } else {
                    ticksSpeed.put(stack, MutablePair.of(ticks, speed));
                }
            } else {
                nbt.putInt("Ticks", ticks);
                nbt.putInt("Speed", speed);
            }
        }
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level level, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag nbt = UtilSteamTool.checkNBT(stack);

        int ticks = nbt.getInt("Ticks");
        int speed = nbt.getInt("Speed");
        boolean result = false;
        if (speed <= 1000) {
            speed += Math.min(90, 1000 - speed);
            result = drainSteam(stack, steamPerDurability(), player);
        }

        if (result) {
            nbt.putInt("Ticks", ticks);
            nbt.putInt("Speed", speed);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean hurtEnemy(@Nonnull ItemStack me, @Nonnull LivingEntity victim, @Nonnull LivingEntity attacker) {
        return true;
    }

    /**
     * @param itemStack The tool's ItemStack
     * @return The mining speed against a valid block
     */
    protected float getSpeed(ItemStack itemStack) {
        return getSpeed(UtilSteamTool.checkNBT(itemStack).getInt("Speed"));
    }

    /**
     * @param speed The speed value in the tool's NBT
     * @return The mining speed against a valid block
     */
    protected float getSpeed(int speed) {
        return 11F * (speed / 1000F);
    }

    @Override
    public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull BlockState state) {
        CompoundTag nbt = UtilSteamTool.checkNBT(stack);
        int speed = nbt.getInt("Speed");
        return getTier().getSpeed() != 1f && speed > 0 ? getSpeed(speed) : 0f;
    }

    @Override
    public boolean canCharge(ItemStack me) {
        return true;
    }

    @Override
    public boolean addSteam(ItemStack me, int amount, LivingEntity entity) {
        int trueAmount = -amount / steamPerDurability();
        int newAmount = me.getDamageValue() + trueAmount;
        if (newAmount <= 0) {
            me.setDamageValue(0);
            return false;
        }
        if (me.getMaxDamage() >= newAmount) {
            me.setDamageValue(newAmount);
            return true;
        }
        if (amount < 0) {
            ItemStack armor = ChargableUtility.findFirstChargableArmor(entity);
            if (armor != null) {
                SteamChargable chargable = (SteamChargable) armor.getItem();
                if (chargable.hasPower(armor, amount)) {
                    int armorAmount = (-amount) / chargable.steamPerDurability();
                    chargable.drainSteam(armor, armorAmount, entity);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasPower(ItemStack me, int powerNeeded) {
        int truePowerNeeded = -powerNeeded / steamPerDurability();
        return me.getDamageValue() >= truePowerNeeded;
    }

    @Override
    public boolean needsPower(ItemStack me, int powerNeeded) {
        int truePowerNeeded = -powerNeeded / steamPerDurability();
        return truePowerNeeded >= (me.getMaxDamage() - me.getDamageValue());
    }

    @Override
    public boolean drainSteam(ItemStack me, int amountToDrain, LivingEntity entity) {
        return addSteam(me, -amountToDrain, entity);
    }

    @Override
    public Pair<Integer, Integer>[] engineerCoordinates() {
        return UtilSteamTool.ENGINEER_COORDINATES;
    }

    @Override
    public ItemStack getStackInSlot(ItemStack me, int slot) {
        if (me.hasTag() && me.getTag().contains("upgrades") &&
          me.getTag().getCompound("upgrades").contains(Integer.toString(slot))) {
            return ItemStack.of(me.getTag().getCompound("upgrades").getCompound(Integer.toString(slot)));
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setInventorySlotContents(ItemStack me, int slot, ItemStack stack) {
        UtilSteamTool.setNBTInventory(me, slot, stack);
    }

    @Override
    public boolean isItemValidForSlot(ItemStack me, int slot, ItemStack var2) {
        return true;
    }

    @Override
    public ItemStack decrStackSize(ItemStack me, int slot, int amount) {
        if (!getStackInSlot(me, slot).isEmpty()) {
            ItemStack stack;
            if (getStackInSlot(me, slot).getCount() <= amount) {
                stack = getStackInSlot(me, slot);
                setInventorySlotContents(me, slot, ItemStack.EMPTY);
            } else {
                stack = getStackInSlot(me, slot).split(amount);
                setInventorySlotContents(me, slot, getStackInSlot(me, slot));

                if (getStackInSlot(me, slot).isEmpty()) {
                    setInventorySlotContents(me, slot, ItemStack.EMPTY);
                }
            }
            return stack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void drawSlot(ContainerScreen gui, int slotnum, int i, int j) {
//        todo
//        gui.getMinecraft().getTextureManager().bindTexture(Constants.ENG_GUI_TEXTURES);
//        switch (slotnum) {
//            case 0: {
//                gui.drawTexturedModalRect(i, j, 176, 0, 18, 18);
//                break;
//            }
//            case 1: {
//                gui.drawTexturedModalRect(i, j, 176, 0, 18, 18);
//                break;
//            }
//        }
    }

    @Override
    public boolean canPutInSlot(ItemStack me, int slotNum, ItemStack upgrade) {
        if (upgrade != null && upgrade.getItem() instanceof SteamToolUpgrade) {
            SteamToolUpgrade upgradeItem = (SteamToolUpgrade) upgrade.getItem();
            return ((upgradeItem.getToolSlot().tool == getToolInteger() &&
              upgradeItem.getToolSlot().slot == slotNum) ||
              upgradeItem.getToolSlot() == SteamToolSlot.TOOL_CORE);
        }
        return false;
    }

    @Override
    public boolean isWound(ItemStack stack) {
        return UtilSteamTool.checkNBT(stack).getInt("Speed") > 0;
    }

    @Override
    public boolean hasUpgrade(ItemStack me, Item check) {
        return UtilSteamTool.hasUpgrade(me, check);
    }

    /**
     * @return the according integer from {@link SteamToolSlot} that corresponds with this tool.
     */
    public abstract int getToolInteger();

    /**
     * @return the according {@link SteamToolSlot} that shows red text for upgrades in the tooltip.
     */
    @Nonnull
    public abstract SteamToolSlot getRedSlot();

    /**
     * Delegates events to {@link SteamToolUpgrade}'s according methods.
     */
    public static final class ToolUpgradeEventDelegator {
        /**
         * @param tool The ItemStack to check.
         * @return Whether the provided ItemStack contains a steam tool that is wound up.
         */
        private boolean isToolOkay(@Nonnull ItemStack tool) {
            return tool.getItem() instanceof SteamTool && ((SteamTool) tool.getItem()).isWound(tool);
        }

        /**
         * Calls {@link SteamToolUpgrade#onUpdateBreakSpeedWithTool(PlayerEvent.BreakSpeed, ItemStack, ItemStack)}
         * for every upgrade in the tool.
         */
        @SubscribeEvent
        public void onBlockBreakSpeedUpdate(PlayerEvent.BreakSpeed event) {
            ItemStack equipped = event.getEntity().getMainHandItem();
            BlockPos pos = event.getPosition().orElseThrow();
            Level level = event.getEntity().level();
            BlockState state = level.getBlockState(pos);
            if (state.isAir() || !isToolOkay(equipped)) {
                return;
            }

            for (ItemStack upgradeStack : UtilSteamTool.getUpgradeStacks(equipped)) {
                SteamToolUpgrade upgrade = (SteamToolUpgrade) upgradeStack.getItem();
                upgrade.onUpdateBreakSpeedWithTool(event, equipped, upgradeStack);
            }
        }

        /**
         * Calls {@link SteamToolUpgrade#onBlockBreakWithTool(BlockEvent.BreakEvent, ItemStack, ItemStack)}
         * for every upgrade in the tool.
         */
        @SubscribeEvent
        public void onBlockBreak(BlockEvent.BreakEvent event) {
            Player player = event.getPlayer();
            ItemStack equipped = player.getMainHandItem();
            if (!isToolOkay(equipped)) {
                return;
            }
            for (ItemStack upgradeStack : UtilSteamTool.getUpgradeStacks(equipped)) {
                SteamToolUpgrade upgrade = (SteamToolUpgrade) upgradeStack.getItem();
                if (!upgrade.onBlockBreakWithTool(event, equipped, upgradeStack)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        /**
         * Calls {@link SteamToolUpgrade#onAttackWithTool(Player, LivingEntity, DamageSource, ItemStack, ItemStack)}
         * for every upgrade in the tool.
         */
        @SubscribeEvent
        public void onAttack(LivingAttackEvent event) {
            DamageSource dSource = event.getSource();
            Entity source = dSource.getEntity();
            if (!(source instanceof Player player)) {
                return;
            }
            ItemStack equipped = player.getMainHandItem();
            if (!isToolOkay(equipped)) {
                return;
            }

            for (ItemStack upgradeStack : UtilSteamTool.getUpgradeStacks(equipped)) {
                SteamToolUpgrade upgrade = (SteamToolUpgrade) upgradeStack.getItem();
                if (!upgrade.onAttackWithTool(player, event.getEntity(), dSource, equipped, upgradeStack)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        /**
         * Calls {@link SteamToolUpgrade#onRightClickBlockWithTool(PlayerInteractEvent.RightClickBlock, ItemStack, ItemStack)}
         * for every upgrade in the tool.
         */
        @SubscribeEvent
        public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
            ItemStack equipped = event.getItemStack();
            if (!isToolOkay(equipped)) {
                return;
            }

            for (ItemStack upgradeStack : UtilSteamTool.getUpgradeStacks(equipped)) {
                SteamToolUpgrade upgrade = (SteamToolUpgrade) upgradeStack.getItem();
                if (!upgrade.onRightClickBlockWithTool(event, equipped, upgradeStack)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        @SubscribeEvent
        public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
            ItemStack equipped = event.getItemStack();
            if (!isToolOkay(equipped)) {
                return;
            }

            for (ItemStack upgradeStack : UtilSteamTool.getUpgradeStacks(equipped)) {
                SteamToolUpgrade upgrade = (SteamToolUpgrade) upgradeStack.getItem();
                if (!upgrade.onLeftClickBlockWithTool(event, equipped, upgradeStack)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }

        /**
         * Calls {@link SteamToolUpgrade#onRightClickWithTool(PlayerInteractEvent.RightClickItem, ItemStack, ItemStack)}
         * for every upgrade in the tool.
         */
        @SubscribeEvent
        public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
            ItemStack equipped = event.getItemStack();
            if (!isToolOkay(equipped)) {
                return;
            }

            for (ItemStack upgradeStack : UtilSteamTool.getUpgradeStacks(equipped)) {
                SteamToolUpgrade upgrade = (SteamToolUpgrade) upgradeStack.getItem();
                if (!upgrade.onRightClickWithTool(event, equipped, upgradeStack)) {
                    event.setCanceled(true);
                    return;
                }
            }
        }
    }
}
