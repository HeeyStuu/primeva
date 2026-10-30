package dev.heeystuuh.primeva.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;

/** Espectro Miasmático: espírito que nasce onde o miasma é muito forte. Atravessa paredes e some quando a área é purificada. */
public class MiasmaWraith extends Vex {
    public MiasmaWraith(EntityType<? extends Vex> type, Level level) {
        super(type, level);
        xpReward = 8;
    }

    public static AttributeSupplier.Builder createWraithAttributes() {
        return Vex.createAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.ATTACK_DAMAGE, 5);
    }

    @Override
    public void tick() {
        super.tick();
        // longe do miasma ele se desfaz
        if (!level().isClientSide && tickCount % 100 == 0 && dev.heeystuuh.primeva.Miasma.get(level(), blockPosition()) < 20) {
            hurt(damageSources().magic(), 4f);
        }
    }
}
