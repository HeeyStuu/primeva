package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.item.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(PrimevaMod.MODID);

    public static final Tier PRIMEVAL_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1400, 8.5f, 3.5f, 20, () -> Ingredient.of(ModItems.PRIMEVAL_INGOT.get()));

    public static final DeferredItem<Item> ARCANE_DUST = ITEMS.registerSimpleItem("arcane_dust");
    public static final DeferredItem<Item> EMPTY_PHIAL = ITEMS.registerSimpleItem("empty_phial");

    public static final Map<Essence, DeferredItem<Item>> PHIALS = new EnumMap<>(Essence.class);
    static {
        for (Essence e : Essence.values()) {
            PHIALS.put(e, ITEMS.register("phial_" + e.id, () -> new PhialItem(e, new Item.Properties().stacksTo(16))));
        }
    }

    public static final DeferredItem<Item> LENS = ITEMS.register("lens",
            () -> new LensItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> GRIMOIRE = ITEMS.register("grimoire",
            () -> new GrimoireItem(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> PRIMEVAL_INGOT = ITEMS.registerSimpleItem("primeval_ingot",
            new Item.Properties().rarity(Rarity.UNCOMMON));

    public static final DeferredItem<Item> UMBRAL_BLADE = ITEMS.register("umbral_blade",
            () -> new UmbralBladeItem(PRIMEVAL_TIER, new Item.Properties().rarity(Rarity.RARE)
                    .attributes(SwordItem.createAttributes(PRIMEVAL_TIER, 3, -2.4f))));

    public static final DeferredItem<Item> GALE_BOOTS = ITEMS.register("gale_boots",
            () -> new ArmorItem(ModArmor.GALE, ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.RARE)
                    .durability(ArmorItem.Type.BOOTS.getDurability(30))));

    // ---------------- Capítulo II / III ----------------
    public static final Map<Essence, DeferredItem<Item>> CRYSTALS = new EnumMap<>(Essence.class);
    static {
        for (Essence e : Essence.values()) {
            CRYSTALS.put(e, ITEMS.register("crystal_" + e.id, () -> new CrystalItem(e, new Item.Properties().rarity(Rarity.RARE))));
        }
    }

    public static final DeferredItem<Item> PRIMEVAL_HELMET = armor("primeval_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<Item> PRIMEVAL_CHESTPLATE = armor("primeval_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<Item> PRIMEVAL_LEGGINGS = armor("primeval_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<Item> PRIMEVAL_BOOTS = armor("primeval_boots", ArmorItem.Type.BOOTS);

    private static DeferredItem<Item> armor(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ArmorItem(ModArmor.PRIMEVAL, type, new Item.Properties().rarity(Rarity.RARE)
                .fireResistant().durability(type.getDurability(50))));
    }

    public static final Tier ECLIPSE_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            4000, 10f, 6f, 25, () -> Ingredient.of(ModItems.PRIMEVAL_INGOT.get()));

    public static final DeferredItem<Item> ECLIPSE_REAPER = ITEMS.register("eclipse_reaper",
            () -> new EclipseReaperItem(ECLIPSE_TIER, new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                    .attributes(SwordItem.createAttributes(ECLIPSE_TIER, 9, -2.6f))));

    public static final DeferredItem<Item> ARCHMAGE_CROWN = ITEMS.register("archmage_crown",
            () -> new ArmorItem(ModArmor.ARCHMAGE, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.EPIC)
                    .fireResistant().durability(ArmorItem.Type.HELMET.getDurability(60))));

    public static final DeferredItem<Item> PRIMEVAL_HEART = ITEMS.register("primeval_heart",
            () -> new PrimevalHeartItem(new Item.Properties().rarity(Rarity.EPIC).fireResistant().stacksTo(1)));

    // ---------------- Varinhas, elixires e ferramentas ----------------
    public static final Map<Essence, DeferredItem<Item>> WANDS = new EnumMap<>(Essence.class);
    static {
        for (Essence e : Essence.values()) {
            Rarity r = (e == Essence.EMBER || e == Essence.TIDE || e == Essence.GALE) ? Rarity.UNCOMMON : Rarity.RARE;
            WANDS.put(e, ITEMS.register("wand_" + e.id, () -> new WandItem(e, new Item.Properties().rarity(r))));
        }
    }

    public static final DeferredItem<Item> ELIXIR_VIGOR = ITEMS.register("elixir_vigor", () -> new ElixirItem("elixir_vigor", () -> java.util.List.of(
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_BOOST, 3600, 1),
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 3600, 1),
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DIG_SPEED, 3600, 1)),
            new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> ELIXIR_ABYSS = ITEMS.register("elixir_abyss", () -> new ElixirItem("elixir_abyss", () -> java.util.List.of(
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.NIGHT_VISION, 9600, 0),
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 9600, 0),
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.CONDUIT_POWER, 9600, 0),
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DOLPHINS_GRACE, 3600, 0)),
            new Item.Properties().stacksTo(16).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> ELIXIR_PRIMEVAL = ITEMS.register("elixir_primeval", () -> new ElixirItem("elixir_primeval", () -> java.util.List.of(
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, 1200, 1),
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, 2400, 3),
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 2400, 1),
            new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 4800, 0)),
            new Item.Properties().stacksTo(16).rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> PRIMEVAL_PICKAXE = ITEMS.register("primeval_pickaxe",
            () -> new PrimevalPickaxeItem(ECLIPSE_TIER, new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                    .attributes(PickaxeItem.createAttributes(ECLIPSE_TIER, 1f, -2.8f))));

    public static Item wand(Essence e) {
        return WANDS.get(e).get();
    }

    public static final DeferredItem<BlockItem> DISTILLER = ITEMS.registerSimpleBlockItem("distiller", ModBlocks.DISTILLER, new Item.Properties());
    public static final DeferredItem<BlockItem> PEDESTAL = ITEMS.registerSimpleBlockItem("pedestal", ModBlocks.PEDESTAL, new Item.Properties());
    public static final DeferredItem<BlockItem> ALTAR = ITEMS.registerSimpleBlockItem("altar", ModBlocks.ALTAR, new Item.Properties());
    public static final DeferredItem<BlockItem> PURIFIER = ITEMS.registerSimpleBlockItem("purifier", ModBlocks.PURIFIER, new Item.Properties());
    public static final DeferredItem<BlockItem> CORRUPTED_SOIL = ITEMS.registerSimpleBlockItem("corrupted_soil", ModBlocks.CORRUPTED_SOIL, new Item.Properties());

    public static final DeferredItem<BlockItem> CONTAINMENT_PILLAR = ITEMS.registerSimpleBlockItem("containment_pillar", ModBlocks.CONTAINMENT_PILLAR, new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredItem<BlockItem> CATALYST = ITEMS.registerSimpleBlockItem("catalyst", ModBlocks.CATALYST, new Item.Properties().rarity(Rarity.EPIC).fireResistant());
    public static final DeferredItem<BlockItem> STUDY_TABLE = ITEMS.registerSimpleBlockItem("study_table", ModBlocks.STUDY_TABLE, new Item.Properties());

    // ================= Alquimia (inspirado no Thaumcraft) =================
    public static final Tier ARCANITE_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL,
            650, 7.0f, 2.5f, 22, () -> Ingredient.of(ModItems.ARCANITE_INGOT.get()));
    public static final Tier VOIDSTEEL_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            300, 8.5f, 3.5f, 12, () -> Ingredient.of(ModItems.VOIDSTEEL_INGOT.get()));

    public static final DeferredItem<Item> QUICKSILVER = ITEMS.registerSimpleItem("quicksilver");
    public static final DeferredItem<Item> AMBER = ITEMS.registerSimpleItem("amber");
    public static final Map<Essence, DeferredItem<Item>> SHARDS = new EnumMap<>(Essence.class);
    static {
        for (Essence e : Essence.values()) SHARDS.put(e, ITEMS.registerSimpleItem("shard_" + e.id));
    }
    public static final DeferredItem<Item> PRIMORDIAL_SALT = ITEMS.register("primordial_salt",
            () -> new SaltItem(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> ARCANITE_INGOT = ITEMS.registerSimpleItem("arcanite_ingot");
    public static final DeferredItem<Item> ARCANITE_NUGGET = ITEMS.registerSimpleItem("arcanite_nugget");
    public static final DeferredItem<Item> VOID_SEED = ITEMS.register("void_seed",
            () -> new TooltipItem("void_seed", false, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> VOIDSTEEL_INGOT = ITEMS.register("voidsteel_ingot",
            () -> new TooltipItem("voidsteel", false, new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> ARCANE_FUEL = ITEMS.register("arcane_fuel",
            () -> new TooltipItem("arcane_fuel", false, new Item.Properties()));

    public static final DeferredItem<Item> ARCANITE_SWORD = ITEMS.register("arcanite_sword",
            () -> new SwordItem(ARCANITE_TIER, new Item.Properties().attributes(SwordItem.createAttributes(ARCANITE_TIER, 3, -2.4f))));
    public static final DeferredItem<Item> ARCANITE_PICKAXE = ITEMS.register("arcanite_pickaxe",
            () -> new PickaxeItem(ARCANITE_TIER, new Item.Properties().attributes(PickaxeItem.createAttributes(ARCANITE_TIER, 1f, -2.8f))));
    public static final DeferredItem<Item> ARCANITE_AXE = ITEMS.register("arcanite_axe",
            () -> new AxeItem(ARCANITE_TIER, new Item.Properties().attributes(AxeItem.createAttributes(ARCANITE_TIER, 6f, -3.1f))));
    public static final DeferredItem<Item> ARCANITE_SHOVEL = ITEMS.register("arcanite_shovel",
            () -> new ShovelItem(ARCANITE_TIER, new Item.Properties().attributes(ShovelItem.createAttributes(ARCANITE_TIER, 1.5f, -3f))));
    public static final DeferredItem<Item> ARCANITE_HOE = ITEMS.register("arcanite_hoe",
            () -> new HoeItem(ARCANITE_TIER, new Item.Properties().attributes(HoeItem.createAttributes(ARCANITE_TIER, -2f, -1f))));

    public static final DeferredItem<Item> ARCANITE_HELMET = arcanite("arcanite_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<Item> ARCANITE_CHESTPLATE = arcanite("arcanite_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<Item> ARCANITE_LEGGINGS = arcanite("arcanite_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<Item> ARCANITE_BOOTS = arcanite("arcanite_boots", ArmorItem.Type.BOOTS);

    private static DeferredItem<Item> arcanite(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ArmorItem(ModArmor.ARCANITE, type, new Item.Properties().durability(type.getDurability(28))));
    }

    public static final DeferredItem<Item> VOIDSTEEL_SWORD = ITEMS.register("voidsteel_sword",
            () -> new VoidsteelSwordItem(VOIDSTEEL_TIER, new Item.Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(VOIDSTEEL_TIER, 3, -2.4f))));
    public static final DeferredItem<Item> VOIDSTEEL_PICKAXE = ITEMS.register("voidsteel_pickaxe",
            () -> new PickaxeItem(VOIDSTEEL_TIER, new Item.Properties().rarity(Rarity.RARE).attributes(PickaxeItem.createAttributes(VOIDSTEEL_TIER, 1f, -2.8f))));
    public static final DeferredItem<Item> VOIDSTEEL_AXE = ITEMS.register("voidsteel_axe",
            () -> new AxeItem(VOIDSTEEL_TIER, new Item.Properties().rarity(Rarity.RARE).attributes(AxeItem.createAttributes(VOIDSTEEL_TIER, 6f, -3.0f))));
    public static final DeferredItem<Item> VOIDSTEEL_SHOVEL = ITEMS.register("voidsteel_shovel",
            () -> new ShovelItem(VOIDSTEEL_TIER, new Item.Properties().rarity(Rarity.RARE).attributes(ShovelItem.createAttributes(VOIDSTEEL_TIER, 1.5f, -3f))));

    public static final DeferredItem<Item> REVEALING_GOGGLES = ITEMS.register("revealing_goggles",
            () -> new ArmorItem(ModArmor.REVEALING, ArmorItem.Type.HELMET, new Item.Properties().rarity(Rarity.UNCOMMON)
                    .durability(ArmorItem.Type.HELMET.getDurability(20))));
    public static final DeferredItem<Item> RUNIC_RING = ITEMS.register("runic_ring",
            () -> new TooltipItem("runic_ring", false, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));

    public static final Map<FocusItem.Kind, DeferredItem<Item>> FOCI = new EnumMap<>(FocusItem.Kind.class);
    static {
        for (FocusItem.Kind k : FocusItem.Kind.values()) {
            Rarity r = k == FocusItem.Kind.HOLE || k == FocusItem.Kind.EXCHANGE || k == FocusItem.Kind.RADIANCE ? Rarity.RARE : Rarity.UNCOMMON;
            FOCI.put(k, ITEMS.register("focus_" + k.name().toLowerCase(), () -> new FocusItem(k, new Item.Properties().rarity(r))));
        }
    }

    public static final DeferredItem<BlockItem> VERMILION_ORE = block(ModBlocks.VERMILION_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_VERMILION_ORE = block(ModBlocks.DEEPSLATE_VERMILION_ORE);
    public static final DeferredItem<BlockItem> AMBER_ORE = block(ModBlocks.AMBER_ORE);
    public static final Map<Essence, DeferredItem<BlockItem>> CLUSTER_ITEMS = new EnumMap<>(Essence.class);
    static {
        for (Essence e : Essence.values()) CLUSTER_ITEMS.put(e, block(ModBlocks.CLUSTERS.get(e)));
    }
    public static final DeferredItem<BlockItem> ARGENT_LOG = block(ModBlocks.ARGENT_LOG);
    public static final DeferredItem<BlockItem> ARGENT_PLANKS = block(ModBlocks.ARGENT_PLANKS);
    public static final DeferredItem<BlockItem> ARGENT_LEAVES = block(ModBlocks.ARGENT_LEAVES);
    public static final DeferredItem<BlockItem> ARGENT_SAPLING = block(ModBlocks.ARGENT_SAPLING);
    public static final DeferredItem<BlockItem> ELDER_LOG = block(ModBlocks.ELDER_LOG);
    public static final DeferredItem<BlockItem> ELDER_PLANKS = block(ModBlocks.ELDER_PLANKS);
    public static final DeferredItem<BlockItem> ELDER_LEAVES = block(ModBlocks.ELDER_LEAVES);
    public static final DeferredItem<BlockItem> ELDER_SAPLING = block(ModBlocks.ELDER_SAPLING);
    public static final DeferredItem<BlockItem> ARCANITE_BLOCK = block(ModBlocks.ARCANITE_BLOCK);
    public static final DeferredItem<BlockItem> CRUCIBLE = block(ModBlocks.CRUCIBLE);
    public static final DeferredItem<BlockItem> HUNGRY_CHEST = block(ModBlocks.HUNGRY_CHEST);
    public static final DeferredItem<BlockItem> ARCANE_LAMP = block(ModBlocks.ARCANE_LAMP);
    public static final DeferredItem<BlockItem> ETERNAL_FLAME = block(ModBlocks.ETERNAL_FLAME);

    public static final DeferredItem<BlockItem> ESSENCE_JAR = block(ModBlocks.ESSENCE_JAR);
    public static final DeferredItem<BlockItem> ESSENCE_TUBE = block(ModBlocks.ESSENCE_TUBE);

    public static final DeferredItem<BlockItem> MIASMA_CRYSTAL = block(ModBlocks.MIASMA_CRYSTAL);
    public static final DeferredItem<BlockItem> RUNIC_STABILIZER = block(ModBlocks.RUNIC_STABILIZER);
    public static final DeferredItem<Item> LUCIDITY_TONIC = ITEMS.register("lucidity_tonic",
            () -> new LucidityTonicItem(new Item.Properties().stacksTo(8).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> RESEARCH_SCROLL = ITEMS.register("research_scroll",
            () -> new TooltipItem("research_scroll", false, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<BlockItem> ESSENCE_FILTER_TUBE = block(ModBlocks.ESSENCE_FILTER_TUBE);
    public static final DeferredItem<Item> MIASMA_SHARD = ITEMS.register("miasma_shard",
            () -> new TooltipItem("miasma_shard", false, new Item.Properties()));
    public static final DeferredItem<Item> MIASMIC_RESIDUE = ITEMS.register("miasmic_residue",
            () -> new TooltipItem("miasmic_residue", false, new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> PURIFYING_INCENSE = ITEMS.register("purifying_incense",
            () -> new PurifyingIncenseItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> WRAITH_SPAWN_EGG = ITEMS.register("miasma_wraith_spawn_egg",
            () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.MIASMA_WRAITH, 0x1A0A2A, 0xB070FF, new Item.Properties()));

    // ---------------- novidades ----------------
    public static final Map<Essence, DeferredItem<Item>> ARROWS = new EnumMap<>(Essence.class);
    static {
        for (Essence e : Essence.values()) ARROWS.put(e, ITEMS.register("arrow_" + e.id, () -> new dev.heeystuuh.primeva.item.NewItems.EssenceArrow(e, new Item.Properties())));
    }
    public static final DeferredItem<Item> ELDER_BOW = ITEMS.register("elder_bow", () -> new dev.heeystuuh.primeva.item.NewItems.ElderBow(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> VOIDSTEEL_HELMET = ITEMS.register("voidsteel_helmet", () -> new dev.heeystuuh.primeva.item.NewItems.VoidArmor(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final DeferredItem<Item> VOIDSTEEL_CHESTPLATE = ITEMS.register("voidsteel_chestplate", () -> new dev.heeystuuh.primeva.item.NewItems.VoidArmor(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final DeferredItem<Item> VOIDSTEEL_LEGGINGS = ITEMS.register("voidsteel_leggings", () -> new dev.heeystuuh.primeva.item.NewItems.VoidArmor(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final DeferredItem<Item> VOIDSTEEL_BOOTS = ITEMS.register("voidsteel_boots", () -> new dev.heeystuuh.primeva.item.NewItems.VoidArmor(ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final DeferredItem<Item> CLOUD_RING = ITEMS.register("cloud_ring",
            () -> new TooltipItem("cloud_ring", false, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> MIND_MIRROR = ITEMS.register("mind_mirror", () -> new dev.heeystuuh.primeva.item.NewItems.MindMirror(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> CLEANSING_SOAP = ITEMS.register("cleansing_soap", () -> new dev.heeystuuh.primeva.item.NewItems.CleansingSoap(new Item.Properties()));
    public static final DeferredItem<Item> ASTRAL_SEXTANT = ITEMS.register("astral_sextant", () -> new dev.heeystuuh.primeva.item.NewItems.AstralSextant(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> DECAYING_BRAIN = ITEMS.register("decaying_brain", () -> new dev.heeystuuh.primeva.item.NewItems.DecayingBrain(new Item.Properties()));
    public static final DeferredItem<BlockItem> GROWTH_LAMP = block(ModBlocks.GROWTH_LAMP);
    public static final DeferredItem<BlockItem> FERTILITY_LAMP = block(ModBlocks.FERTILITY_LAMP);
    public static final DeferredItem<BlockItem> LEVITATOR = block(ModBlocks.LEVITATOR);
    public static final DeferredItem<BlockItem> WARDING_STONE = block(ModBlocks.WARDING_STONE);
    public static final DeferredItem<BlockItem> ARCANE_STONE = block(ModBlocks.ARCANE_STONE);
    public static final DeferredItem<BlockItem> ARCANE_BRICKS = block(ModBlocks.ARCANE_BRICKS);
    public static final DeferredItem<BlockItem> ARCANE_BRICK_STAIRS = block(ModBlocks.ARCANE_BRICK_STAIRS);
    public static final DeferredItem<BlockItem> ARCANE_BRICK_SLAB = block(ModBlocks.ARCANE_BRICK_SLAB);
    public static final DeferredItem<BlockItem> ELDER_STAIRS = block(ModBlocks.ELDER_STAIRS);
    public static final DeferredItem<BlockItem> ELDER_SLAB = block(ModBlocks.ELDER_SLAB);
    public static final DeferredItem<BlockItem> ARGENT_STAIRS = block(ModBlocks.ARGENT_STAIRS);
    public static final DeferredItem<BlockItem> ARGENT_SLAB = block(ModBlocks.ARGENT_SLAB);

    private static <B extends net.minecraft.world.level.block.Block> DeferredItem<BlockItem> block(net.neoforged.neoforge.registries.DeferredBlock<B> b) {
        return ITEMS.registerSimpleBlockItem(b);
    }

    // ================= Engenhos: ferramentas elementais, golens, amuletos, seita carmesim =================
    public static final Tier ELEMENTAL_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1300, 9.0f, 3.0f, 18, () -> Ingredient.of(ModItems.ARCANITE_INGOT.get()));
    public static final Tier CRIMSON_TIER = new SimpleTier(BlockTags.INCORRECT_FOR_IRON_TOOL,
            500, 6.5f, 2.5f, 16, () -> Ingredient.of(ModItems.CRIMSON_CLOTH.get()));

    public static final DeferredItem<Item> EMBER_PICKAXE = ITEMS.register("ember_pickaxe",
            () -> new ElementalToolItems.Pick(ELEMENTAL_TIER, new Item.Properties().rarity(Rarity.RARE).attributes(PickaxeItem.createAttributes(ELEMENTAL_TIER, 1f, -2.8f))));
    public static final DeferredItem<Item> TIDE_AXE = ITEMS.register("tide_axe",
            () -> new ElementalToolItems.Axe(ELEMENTAL_TIER, new Item.Properties().rarity(Rarity.RARE).attributes(AxeItem.createAttributes(ELEMENTAL_TIER, 5f, -3.0f))));
    public static final DeferredItem<Item> TERRA_SHOVEL = ITEMS.register("terra_shovel",
            () -> new ElementalToolItems.Shovel(ELEMENTAL_TIER, new Item.Properties().rarity(Rarity.RARE).attributes(ShovelItem.createAttributes(ELEMENTAL_TIER, 1.5f, -3f))));
    public static final DeferredItem<Item> GALE_SWORD = ITEMS.register("gale_sword",
            () -> new ElementalToolItems.Sword(ELEMENTAL_TIER, new Item.Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(ELEMENTAL_TIER, 3, -2.2f))));
    public static final DeferredItem<Item> LUMEN_HOE = ITEMS.register("lumen_hoe",
            () -> new ElementalToolItems.Hoe(ELEMENTAL_TIER, new Item.Properties().rarity(Rarity.RARE).attributes(HoeItem.createAttributes(ELEMENTAL_TIER, -3f, 0f))));

    public static final DeferredItem<Item> TRAVELER_BOOTS = ITEMS.register("traveler_boots",
            () -> new ArmorItem(ModArmor.TRAVELER, ArmorItem.Type.BOOTS, new Item.Properties().rarity(Rarity.UNCOMMON).durability(ArmorItem.Type.BOOTS.getDurability(20))));
    public static final DeferredItem<Item> STEADFAST_GIRDLE = ITEMS.register("steadfast_girdle",
            () -> new TooltipItem("steadfast_girdle", false, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> ESSENCE_AMULET = ITEMS.register("essence_amulet",
            () -> new TooltipItem("essence_amulet", false, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> VERDANT_CHARM = ITEMS.register("verdant_charm",
            () -> new TooltipItem("verdant_charm", false, new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> POCKET_MIRROR = ITEMS.register("pocket_mirror",
            () -> new TrinketItems.Mirror(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> GRAPPLE = ITEMS.register("grapple",
            () -> new TrinketItems.Grapple(new Item.Properties().rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<Item> ARCANE_GOLEM = ITEMS.register("arcane_golem",
            () -> new GolemItem(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> SEAL_HARVEST = ITEMS.register("seal_harvest",
            () -> new SealItem(dev.heeystuuh.primeva.entity.ArcaneGolem.Task.HARVEST, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> SEAL_PICKUP = ITEMS.register("seal_pickup",
            () -> new SealItem(dev.heeystuuh.primeva.entity.ArcaneGolem.Task.PICKUP, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> SEAL_GUARD = ITEMS.register("seal_guard",
            () -> new SealItem(dev.heeystuuh.primeva.entity.ArcaneGolem.Task.GUARD, new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> SEAL_LUMBER = ITEMS.register("seal_lumber",
            () -> new SealItem(dev.heeystuuh.primeva.entity.ArcaneGolem.Task.LUMBER, new Item.Properties().stacksTo(16)));
    public static final DeferredItem<Item> SEAL_STOCK = ITEMS.register("seal_stock",
            () -> new SealItem(dev.heeystuuh.primeva.entity.ArcaneGolem.Task.STOCK, new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> GOLEM_CORE_BLANK = core("golem_core_blank", dev.heeystuuh.primeva.entity.ArcaneGolem.Core.NONE);
    public static final DeferredItem<Item> GOLEM_CORE_SPEED = core("golem_core_speed", dev.heeystuuh.primeva.entity.ArcaneGolem.Core.SPEED);
    public static final DeferredItem<Item> GOLEM_CORE_REACH = core("golem_core_reach", dev.heeystuuh.primeva.entity.ArcaneGolem.Core.REACH);
    public static final DeferredItem<Item> GOLEM_CORE_CAPACITY = core("golem_core_capacity", dev.heeystuuh.primeva.entity.ArcaneGolem.Core.CAPACITY);
    public static final DeferredItem<Item> GOLEM_CORE_STRENGTH = core("golem_core_strength", dev.heeystuuh.primeva.entity.ArcaneGolem.Core.STRENGTH);

    private static DeferredItem<Item> core(String id, dev.heeystuuh.primeva.entity.ArcaneGolem.Core c) {
        return ITEMS.register(id, () -> new GolemCoreItem(c, new Item.Properties().stacksTo(16).rarity(c == dev.heeystuuh.primeva.entity.ArcaneGolem.Core.NONE ? Rarity.COMMON : Rarity.UNCOMMON)));
    }

    public static Item golemCore(dev.heeystuuh.primeva.entity.ArcaneGolem.Core c) {
        return switch (c) {
            case SPEED -> GOLEM_CORE_SPEED.get();
            case REACH -> GOLEM_CORE_REACH.get();
            case CAPACITY -> GOLEM_CORE_CAPACITY.get();
            case STRENGTH -> GOLEM_CORE_STRENGTH.get();
            default -> GOLEM_CORE_BLANK.get();
        };
    }

    public static Item seal(dev.heeystuuh.primeva.entity.ArcaneGolem.Task t) {
        return switch (t) {
            case HARVEST -> SEAL_HARVEST.get();
            case PICKUP -> SEAL_PICKUP.get();
            case LUMBER -> SEAL_LUMBER.get();
            case STOCK -> SEAL_STOCK.get();
            default -> SEAL_GUARD.get();
        };
    }

    public static final DeferredItem<Item> CRIMSON_CLOTH = ITEMS.registerSimpleItem("crimson_cloth");
    public static final DeferredItem<Item> CRIMSON_BLADE = ITEMS.register("crimson_blade",
            () -> new CrimsonBladeItem(CRIMSON_TIER, new Item.Properties().rarity(Rarity.UNCOMMON).attributes(SwordItem.createAttributes(CRIMSON_TIER, 3, -2.4f))));
    public static final DeferredItem<Item> CRIMSON_SCYTHE = ITEMS.register("crimson_scythe",
            () -> new CrimsonBladeItem(CRIMSON_TIER, new Item.Properties().rarity(Rarity.RARE).attributes(SwordItem.createAttributes(CRIMSON_TIER, 5, -2.9f))));
    public static final DeferredItem<Item> HERALD_SIGIL = ITEMS.register("herald_sigil",
            () -> new HeraldSigilItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> CRIMSON_SCEPTER = ITEMS.register("crimson_scepter",
            () -> new CrimsonScepterItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> CRIMSON_HELMET = crimson("crimson_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<Item> CRIMSON_CHESTPLATE = crimson("crimson_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<Item> CRIMSON_LEGGINGS = crimson("crimson_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<Item> CRIMSON_BOOTS = crimson("crimson_boots", ArmorItem.Type.BOOTS);

    private static DeferredItem<Item> crimson(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ArmorItem(ModArmor.CRIMSON, type, new Item.Properties().rarity(Rarity.UNCOMMON).durability(type.getDurability(22))));
    }

    private static Map<Essence, Integer> ess(Object... kv) {
        Map<Essence, Integer> m = new EnumMap<>(Essence.class);
        for (int i = 0; i < kv.length; i += 2) m.put((Essence) kv[i], (Integer) kv[i + 1]);
        return m;
    }

    public static final DeferredItem<Item> CRIMSON_RITES = ITEMS.register("crimson_rites",
            () -> new TrinketItems.Insight("crimson_rites", ess(Essence.UMBRA, 40, Essence.EMBER, 20), new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> CURIO_ANCIENT = ITEMS.register("curio_ancient",
            () -> new TrinketItems.Insight("curio_ancient", ess(Essence.TERRA, 15, Essence.LUMEN, 10), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> CURIO_ABYSS = ITEMS.register("curio_abyss",
            () -> new TrinketItems.Insight("curio_abyss", ess(Essence.UMBRA, 20, Essence.TIDE, 8), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> CURIO_GEAR = ITEMS.register("curio_gear",
            () -> new TrinketItems.Insight("curio_gear", ess(Essence.EMBER, 12, Essence.TERRA, 12), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> CURIO_SPECIMEN = ITEMS.register("curio_specimen",
            () -> new TrinketItems.Insight("curio_specimen", ess(Essence.TIDE, 12, Essence.GALE, 12), new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> LOOT_BAG_COMMON = ITEMS.register("loot_bag_common",
            () -> new TrinketItems.LootBag("common", new Item.Properties()));
    public static final DeferredItem<Item> LOOT_BAG_UNCOMMON = ITEMS.register("loot_bag_uncommon",
            () -> new TrinketItems.LootBag("uncommon", new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> LOOT_BAG_RARE = ITEMS.register("loot_bag_rare",
            () -> new TrinketItems.LootBag("rare", new Item.Properties().rarity(Rarity.RARE)));
    public static final DeferredItem<Item> CULTIST_SPAWN_EGG = ITEMS.register("crimson_cultist_spawn_egg",
            () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.CRIMSON_CULTIST, 0x5A0A14, 0xD8A030, new Item.Properties()));

    public static final DeferredItem<Item> HERALD_SPAWN_EGG = ITEMS.register("crimson_herald_spawn_egg",
            () -> new net.neoforged.neoforge.common.DeferredSpawnEggItem(ModEntities.CRIMSON_HERALD, 0x2A0408, 0xFFD858, new Item.Properties()));

    public static Item shard(Essence e) {
        return SHARDS.get(e).get();
    }

    public static Item crystal(Essence e) {
        return CRYSTALS.get(e).get();
    }

    public static Item phial(Essence e) {
        return PHIALS.get(e).get();
    }

    private ModItems() {}
}
