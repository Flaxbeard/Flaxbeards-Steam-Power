package eiteam.esteemedinnovation.api.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;

public class EntityRocket extends AbstractArrow {
    public float explosionSize;

    protected EntityRocket(EntityType<? extends AbstractArrow> type, Level level) {
        super(type, level, ItemStack.EMPTY);
    }

    protected EntityRocket(EntityType<? extends AbstractArrow> type, double x, double y, double z, Level level) {
        super(type, x, y, z, level, ItemStack.EMPTY);
    }

    protected EntityRocket(EntityType<? extends AbstractArrow> type, LivingEntity shooter, Level level) {
        super(type, shooter, level, ItemStack.EMPTY);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel serverLevel) {
            Vec3 motion = getDeltaMovement();
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX() - motion.x() * 3f, getY() - motion.y() * 3f, getZ() - motion.z() * 3f, 1, 0, 0, 0, 0);
            serverLevel.sendParticles(ParticleTypes.SMOKE, getX() - motion.x() * 1.5f, getY() - motion.y() * 1.5f, getZ() - motion.z() * 1.5f, 1, 0, 0, 0, 0);
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (!level().isClientSide()) {
            level().explode(getOwner(), getX(), getY(), getZ(), explosionSize, true, Level.ExplosionInteraction.BLOCK);
            discard();
        }
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean hurt(@Nonnull DamageSource source, float amount) {
        if (isInvulnerableTo(source)) {
            return false;
        } else {
            Entity entity = source.getEntity();
            if (entity != null) {
                setDeltaMovement(entity.getLookAngle());
                setOwner(entity);
                markHurt();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean ignoreExplosion(Explosion explosion) {
        return true;
    }
}