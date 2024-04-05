package eiteam.esteemedinnovation.api.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

import javax.annotation.Nonnull;

/**
 * AnimalTradeEvent is fired when a player trades with a cat or a dog using the Frequency Transmitter.
 */
public class AnimalTradeEvent extends Event {
    /**
     * The ocelot or wolf selling the items.
     */
    public final LivingEntity salesperson;

    /**
     * The customer buying items.
     */
    public final Player customer;

    /**
     * The ItemStack to buy.
     */
    @Nonnull
    private final ItemStack toBuy;

    /**
     * The ItemStack to sell.
     */
    @Nonnull
    private final ItemStack toSell;

    /**
     * The constructor for the AnimalTradeEvent.
     * @param entity The EntityLiving that is selling items.
     * @param customer The EntityPlayer that is buying items.
     * @param toBuy The ItemStack to buy.
     * @param toSell The ItemStack to sell.
     */
    public AnimalTradeEvent(LivingEntity entity, Player customer, @Nonnull ItemStack toBuy, @Nonnull ItemStack toSell) {
        this.salesperson = entity;
        this.customer = customer;
        this.toBuy = toBuy;
        this.toSell = toSell;
    }
}
