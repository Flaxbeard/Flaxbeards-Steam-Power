package eiteam.esteemedinnovation.api.entity;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class EntityMusketBall extends AbstractArrow {
    protected EntityMusketBall(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level, ItemStack.EMPTY);
    }

    protected EntityMusketBall(EntityType<? extends AbstractArrow> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level, ItemStack.EMPTY);
    }

    protected EntityMusketBall(EntityType<? extends AbstractArrow> type, LivingEntity shooter, Level level) {
        super(type, shooter, level, ItemStack.EMPTY);
        pickup = Pickup.DISALLOWED;
    }

    @Override
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        Vec3 vec3 = new Vec3(pX, pY, pZ)
          .normalize()
          .add(
            // The pMax value is the only change in this method from Project#shoot(d, d, d, f, f).
            random.triangle(0.0, 0.007499999832361937D * pInaccuracy),
            random.triangle(0.0, 0.007499999832361937D * pInaccuracy),
            random.triangle(0.0, 0.007499999832361937D * pInaccuracy)
          )
          .scale((double)pVelocity);
        setDeltaMovement(vec3);
        double d0 = vec3.horizontalDistance();
        setYRot((float)(Mth.atan2(vec3.x, vec3.z) * 180.0F / (float) Math.PI));
        setXRot((float)(Mth.atan2(vec3.y, d0) * 180.0F / (float) Math.PI));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    protected float getWaterInertia() {
        return 0.8f;
    }
}
