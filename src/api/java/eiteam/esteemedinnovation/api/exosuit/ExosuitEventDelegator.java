package eiteam.esteemedinnovation.api.exosuit;

import eiteam.esteemedinnovation.api.util.ItemStackUtility;
import eiteam.esteemedinnovation.api.util.TriConsumer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

/**
 * Do not register this class to the event bus, or you will cause the ExosuitUpgrade methods to get called multiple times (bad).
 * This is registered in the static initializer for {@link ExosuitRegistry}.
 */
public class ExosuitEventDelegator {
    /**
     * Iterates over each armor slot, gets the exosuit armor in that slot, and calls the provided function for every
     * installed {@link ExosuitEventHandler}.
     * @param player The player whose armor to iterate through.
     * @param func A void function that passes the current ExosuitEventHandler, the armor ItemStack, and EquipmentSlot
     *             containing this armor.
     */
    private static void doMethodForEachUpgrade(Player player, TriConsumer<ExosuitEventHandler, ItemStack, EquipmentSlot> func) {
        for (EquipmentSlot slot : ItemStackUtility.ARMOR_SLOTS) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.getItem() instanceof ExosuitArmor) {
                for (ExosuitEventHandler thing : ((ExosuitArmor) armor.getItem()).getInstalledEventHandlers(armor)) {
                    func.accept(thing, armor, slot);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity jumper = event.getEntity();
        if (jumper instanceof Player) {
            Player player = (Player) jumper;
            doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerJump(event, player, armor, slot));
        }
    }

    @SubscribeEvent
    public void onPlayerAttack(LivingAttackEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player playerVictim) {
            doMethodForEachUpgrade(playerVictim, (handler, armor, slot) -> handler.onPlayerAttacked(event, playerVictim, armor, slot));
        }
        DamageSource source = event.getSource();
        Entity entitySource = source.getEntity();
        if (entitySource instanceof Player playerAttacker) {
            doMethodForEachUpgrade(playerAttacker, (handler, armor, slot) -> handler.onPlayerAttacksOther(event, playerAttacker, armor, slot));
        }
    }

    @SubscribeEvent
    public void onPlayerHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim instanceof Player playerVictim) {
            doMethodForEachUpgrade(playerVictim, (handler, armor, slot) -> handler.onPlayerHurt(event, playerVictim, armor, slot));
        }
    }

    @SubscribeEvent
    public void onPickupXP(PlayerXpEvent.PickupXp event) {
        Player player = event.getEntity();
        doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerPickupXP(event, armor, slot));
    }

    @SubscribeEvent
    public void onPlayerInteractsWithEntitySpecific(PlayerInteractEvent.EntityInteractSpecific  event) {
        Player player = event.getEntity();
        doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerInteractsWithEntitySpecific(event, armor, slot));
    }

    @SubscribeEvent
    public void onPlayerInteractsWithEntity(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerInteractsWithEntity(event, armor, slot));
    }

    @SubscribeEvent
    public void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerRightClickBlock(event, armor, slot));
    }

    @SubscribeEvent
    public void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerRightClickItem(event, armor, slot));
    }

    @SubscribeEvent
    public void onPlayerRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        Player player = event.getEntity();
        doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerRightClickEmpty(event, armor, slot));
    }

    @SubscribeEvent
    public void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        Player player = event.getEntity();
        doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerLeftClickBlock(event, armor, slot));
    }

    @SubscribeEvent
    public void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        Player player = event.getEntity();
        doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerLeftClickEmpty(event, armor, slot));
    }

    @SubscribeEvent
    public void onEntityItemPickedUp(PlayerEvent.ItemPickupEvent event) {
        Player player = event.getEntity();
        doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerPickupItem(event, armor, slot));
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        doMethodForEachUpgrade(player, (handler, armor, slot) -> handler.onPlayerTick(event, armor, slot));
    }
}
