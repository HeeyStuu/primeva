package dev.heeystuuh.primeva.entity;

import dev.heeystuuh.primeva.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vindicator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Cultista Carmesim: adorador do miasma que vaga à noite armado com a Lâmina Carmesim. */
public class CrimsonCultist extends Vindicator {
    public CrimsonCultist(EntityType<? extends Vindicator> type, Level level) {
        super(type, level);
    }

    /** Sempre segurando a foice (não cruza os braços). */
    @Override
    public IllagerArmPose getArmPose() {
        return isCelebrating() ? IllagerArmPose.CELEBRATING : IllagerArmPose.ATTACKING;
    }

    public static AttributeSupplier.Builder createCultistAttributes() {
        return Vindicator.createAttributes().add(Attributes.MAX_HEALTH, 32).add(Attributes.ATTACK_DAMAGE, 6).add(Attributes.ARMOR, 4);
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource r, DifficultyInstance diff) {
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ModItems.CRIMSON_SCYTHE.get()));
        setDropChance(EquipmentSlot.MAINHAND, 0.06f);
        Item[] armor = {ModItems.CRIMSON_HELMET.get(), ModItems.CRIMSON_CHESTPLATE.get(), ModItems.CRIMSON_LEGGINGS.get(), ModItems.CRIMSON_BOOTS.get()};
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < 4; i++) {
            if (r.nextFloat() < 0.35f) {
                setItemSlot(slots[i], new ItemStack(armor[i]));
                setDropChance(slots[i], 0.08f);
            }
        }
    }
}
