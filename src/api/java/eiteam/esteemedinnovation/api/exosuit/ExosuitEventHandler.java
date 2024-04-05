package eiteam.esteemedinnovation.api.exosuit;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.event.entity.living.LivingAttackEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

import javax.annotation.Nonnull;

/**
 * This represents a thing that can be put into an {@link ExosuitArmor} implementer (plates, upgrades, for example, but
 * it is only limited by your imagination) and be handled by the various provided events.
 * <p>
 * Implementers of this interface will be handled by the {@link ExosuitEventDelegator}. These methods do not check
 * for steam existing in the exosuit before calling the methods. So if you require steam for your handler, you must
 * manually check within your handler.
 */
public interface ExosuitEventHandler {
    /**
     * Called when the player jumps while wearing an exosuit with this installed.
     * @param event The event.
     * @param jumper The player who jumped and is wearing the suit.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerJump(LivingEvent.LivingJumpEvent event, Player jumper, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player gets attacked while wearing an exosuit with this installed.
     * @param event The event.
     * @param victim The player who is being attacked and is wearing the suit.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerAttacked(LivingAttackEvent event, Player victim, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player attacks another entity while wearing an exosuit with this installed.
     * @param event The event.
     * @param attacker The player who is attacking and is wearing the suit.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerAttacksOther(LivingAttackEvent event, Player attacker, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player is hurt from anything (not necessarily another entity, but it is called for that too)
     * while wearing an exosuit with this installed.
     * @param event The event.
     * @param victim The player who is being hurt (not necessarily attacked by another entity) and is wearing the suit.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerHurt(LivingHurtEvent event, Player victim, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player picks up some experience while wearing the exosuit with this installed.
     * @param event The event. This contains the player, so it is not passed as an additional argument.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerPickupXP(PlayerXpEvent.PickupXp event, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player interacts with an entity a block while wearing the exosuit with this installed.
     * @param event The event. This contains the player, so it is not passed as an additional argument.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerInteractsWithEntitySpecific(PlayerInteractEvent.EntityInteractSpecific event, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player interacts with an entity while wearing the exosuit with this installed.
     * @param event The event. This contains the player, so it is not passed as an additional argument.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerInteractsWithEntity(PlayerInteractEvent.EntityInteract event, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player right clicks a block while wearing the exosuit with this installed.
     * @param event The event. This contains the player, so it is not passed as an additional argument.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player right clicks an item while wearing the exosuit with this installed.
     * @param event The event. This contains the player, so it is not passed as an additional argument.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerRightClickItem(PlayerInteractEvent.RightClickItem event, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player right clicks nothing while wearing the exosuit with this installed.
     * @param event The event. This contains the player, so it is not passed as an additional argument.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerRightClickEmpty(PlayerInteractEvent.RightClickEmpty event, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player left clicks a block while wearing the exosuit with this installed.
     * @param event The event. This contains the player, so it is not passed as an additional argument.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerLeftClickBlock(PlayerInteractEvent.LeftClickBlock event, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player left clicks nothing while wearing the exosuit with this installed.
     * @param event The event. This contains the player, so it is not passed as an additional argument.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called when the player picks up an item from the world (an {@link net.minecraft.world.entity.item.ItemEntity}) while
     * wearing an exosuit with this installed.
     * @param event The event. This contains the player, so it is not passed as an additional argument.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerPickupItem(PlayerEvent.ItemPickupEvent event, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}

    /**
     * Called each player tick for players who are wearing an exosuit with this installed.
     * @param event The event. Contains the player, so it is not passed as an additional argument.
     * @param armorStack The ItemStack containing the armor piece that this is installed in.
     * @param slot The EntityEquipmentSlot that the armorStack is contained in.
     */
    default void onPlayerTick(TickEvent.PlayerTickEvent event, @Nonnull ItemStack armorStack, EquipmentSlot slot) {}
}
