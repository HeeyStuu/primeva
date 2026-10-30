package dev.heeystuuh.primeva;

import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public final class ModArmor {
    public static final DeferredRegister<ArmorMaterial> ARMOR = DeferredRegister.create(Registries.ARMOR_MATERIAL, PrimevaMod.MODID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GALE = ARMOR.register("gale", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.BOOTS, 4);
                m.put(ArmorItem.Type.LEGGINGS, 6);
                m.put(ArmorItem.Type.CHESTPLATE, 8);
                m.put(ArmorItem.Type.HELMET, 3);
                m.put(ArmorItem.Type.BODY, 8);
            }),
            20, SoundEvents.ARMOR_EQUIP_ELYTRA,
            () -> Ingredient.of(ModItems.PRIMEVAL_INGOT.get()),
            List.of(new ArmorMaterial.Layer(PrimevaMod.id("gale"))),
            2.0f, 0.05f));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> PRIMEVAL = ARMOR.register("primeval", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.BOOTS, 4);
                m.put(ArmorItem.Type.LEGGINGS, 8);
                m.put(ArmorItem.Type.CHESTPLATE, 10);
                m.put(ArmorItem.Type.HELMET, 4);
                m.put(ArmorItem.Type.BODY, 11);
            }),
            24, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(ModItems.PRIMEVAL_INGOT.get()),
            List.of(new ArmorMaterial.Layer(PrimevaMod.id("primeval"))),
            4.0f, 0.2f));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARCHMAGE = ARMOR.register("archmage", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.BOOTS, 4);
                m.put(ArmorItem.Type.LEGGINGS, 8);
                m.put(ArmorItem.Type.CHESTPLATE, 10);
                m.put(ArmorItem.Type.HELMET, 6);
                m.put(ArmorItem.Type.BODY, 12);
            }),
            30, SoundEvents.ARMOR_EQUIP_GOLD,
            () -> Ingredient.of(ModItems.PRIMEVAL_INGOT.get()),
            List.of(new ArmorMaterial.Layer(PrimevaMod.id("archmage"))),
            4.5f, 0.2f));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> ARCANITE = ARMOR.register("arcanite", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.BOOTS, 3);
                m.put(ArmorItem.Type.LEGGINGS, 6);
                m.put(ArmorItem.Type.CHESTPLATE, 7);
                m.put(ArmorItem.Type.HELMET, 3);
                m.put(ArmorItem.Type.BODY, 7);
            }),
            25, SoundEvents.ARMOR_EQUIP_IRON,
            () -> Ingredient.of(ModItems.ARCANITE_INGOT.get()),
            List.of(new ArmorMaterial.Layer(PrimevaMod.id("arcanite"))),
            1.5f, 0.0f));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> REVEALING = ARMOR.register("revealing", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.BOOTS, 1);
                m.put(ArmorItem.Type.LEGGINGS, 2);
                m.put(ArmorItem.Type.CHESTPLATE, 3);
                m.put(ArmorItem.Type.HELMET, 2);
                m.put(ArmorItem.Type.BODY, 3);
            }),
            25, SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(ModItems.ARCANITE_INGOT.get()),
            List.of(new ArmorMaterial.Layer(PrimevaMod.id("revealing"))),
            0.5f, 0.0f));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> TRAVELER = ARMOR.register("traveler", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.BOOTS, 3);
                m.put(ArmorItem.Type.LEGGINGS, 3);
                m.put(ArmorItem.Type.CHESTPLATE, 4);
                m.put(ArmorItem.Type.HELMET, 2);
                m.put(ArmorItem.Type.BODY, 4);
            }),
            20, SoundEvents.ARMOR_EQUIP_LEATHER,
            () -> Ingredient.of(net.minecraft.world.item.Items.LEATHER),
            List.of(new ArmorMaterial.Layer(PrimevaMod.id("traveler"))),
            0.5f, 0.0f));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CRIMSON = ARMOR.register("crimson", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.BOOTS, 2);
                m.put(ArmorItem.Type.LEGGINGS, 6);
                m.put(ArmorItem.Type.CHESTPLATE, 7);
                m.put(ArmorItem.Type.HELMET, 3);
                m.put(ArmorItem.Type.BODY, 6);
            }),
            18, SoundEvents.ARMOR_EQUIP_CHAIN,
            () -> Ingredient.of(ModItems.CRIMSON_CLOTH.get()),
            List.of(new ArmorMaterial.Layer(PrimevaMod.id("crimson"))),
            1.5f, 0.05f));

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> VOIDSTEEL = ARMOR.register("voidsteel", () -> new ArmorMaterial(
            Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                m.put(ArmorItem.Type.BOOTS, 3);
                m.put(ArmorItem.Type.LEGGINGS, 7);
                m.put(ArmorItem.Type.CHESTPLATE, 9);
                m.put(ArmorItem.Type.HELMET, 3);
                m.put(ArmorItem.Type.BODY, 10);
            }),
            22, SoundEvents.ARMOR_EQUIP_NETHERITE,
            () -> Ingredient.of(ModItems.VOIDSTEEL_INGOT.get()),
            List.of(new ArmorMaterial.Layer(PrimevaMod.id("voidsteel"))),
            3.0f, 0.1f));

    private ModArmor() {}
}
