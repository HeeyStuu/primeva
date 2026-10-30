package dev.heeystuuh.primeva.entity;

import dev.heeystuuh.primeva.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Evoker;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** Arauto Carmesim: líder da seita. Conjura presas, chama cultistas e se teletransporta quando ferido. */
public class CrimsonHerald extends Evoker {
    private final ServerBossEvent bar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_10);
    private int summonCooldown = 200;

    public CrimsonHerald(EntityType<? extends Evoker> type, Level level) {
        super(type, level);
        setPersistenceRequired();
        xpReward = 120;
    }

    public static AttributeSupplier.Builder createHeraldAttributes() {
        return Evoker.createAttributes().add(Attributes.MAX_HEALTH, 240).add(Attributes.ARMOR, 10)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8).add(Attributes.FOLLOW_RANGE, 40).add(Attributes.MOVEMENT_SPEED, 0.55);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        bar.setProgress(getHealth() / getMaxHealth());
        LivingEntity t = getTarget();
        if (t != null && --summonCooldown <= 0 && level() instanceof ServerLevel sl) {
            summonCooldown = 300;
            for (int i = 0; i < 2; i++) {
                BlockPos p = blockPosition().offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3);
                CrimsonCultist c = ModEntities.CRIMSON_CULTIST.get().create(sl, null, p, MobSpawnType.MOB_SUMMONED, true, false);
                if (c != null) {
                    c.setTarget(t);
                    sl.addFreshEntity(c);
                    sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.getX(), c.getY() + 1, c.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
                }
            }
            sl.playSound(null, blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 2f, 0.6f);
        }
    }

    @Override
    public boolean hurt(DamageSource src, float amount) {
        boolean r = super.hurt(src, amount);
        if (r && !level().isClientSide && random.nextFloat() < 0.18f) blink();
        return r;
    }

    private void blink() {
        for (int i = 0; i < 12; i++) {
            Vec3 to = position().add((random.nextDouble() - 0.5) * 16, random.nextInt(5) - 2, (random.nextDouble() - 0.5) * 16);
            Vec3 from = position();
            if (randomTeleport(to.x, to.y, to.z, true)) {
                if (level() instanceof ServerLevel sl) {
                    sl.sendParticles(ParticleTypes.LARGE_SMOKE, from.x, from.y + 1, from.z, 30, 0.3, 0.8, 0.3, 0.02);
                }
                level().playSound(null, blockPosition(), SoundEvents.ILLUSIONER_MIRROR_MOVE, SoundSource.HOSTILE, 1.5f, 0.7f);
                return;
            }
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer p) {
        super.startSeenByPlayer(p);
        bar.addPlayer(p);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p) {
        super.stopSeenByPlayer(p);
        bar.removePlayer(p);
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    public boolean canJoinPatrol() {
        return false;
    }
}
