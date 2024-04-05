package eiteam.esteemedinnovation.api.enhancement;

import eiteam.esteemedinnovation.api.entity.EntityMusketBall;
import net.minecraft.world.item.Item;

public interface EnhancementFirearm extends Enhancement {
    float getAccuracyChange(Item weapon);

    float getKnockbackChange(Item weapon);

    float getDamageChange(Item weapon);

    int getReloadChange(Item weapon);

    int getClipSizeChange(Item weapon);

    /**
     * Called to make the upgrade use shots
     * <p>
     * This can be used to change features of the base EntityMusketBall bullet
     */
    EntityMusketBall changeBullet(EntityMusketBall bullet);
}
