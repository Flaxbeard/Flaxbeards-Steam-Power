package eiteam.esteemedinnovation.api.enhancement;

import eiteam.esteemedinnovation.api.entity.EntityRocket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public interface EnhancementRocketLauncher extends Enhancement {
    float getAccuracyChange(Item weapon);

    float getExplosionChange(Item weapon);

    int getReloadChange(Item weapon);

    int getFireDelayChange(ItemStack weapon);

    int getClipSizeChange(Item weapon);

    /**
     * Called to make the upgrade use shots
     * <p>
     * This can be used to change features of the base EntityMusketBall bullet
     */
    EntityRocket changeBullet(EntityRocket bullet);

    @Override
    default void afterRoundFired(@Nonnull ItemStack weaponStack, Level level, Player player) {
        if (player.getAbilities().flying && !player.onGround() && weaponStack.hasTag()) {
            // todo: Make constants for tag keys.
            int timeBetweenFire = weaponStack.getTag().getInt("FireDelay");
            weaponStack.getTag().putInt("FireDelay", timeBetweenFire + getFireDelayChange(weaponStack));
        }
    }
}
