package dev.heeystuuh.primeva;

import dev.heeystuuh.primeva.network.Network;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class CommonEvents {
    private static final ResourceLocation STEP = PrimevaMod.id("gale_step");
    private static final ResourceLocation SPEED = PrimevaMod.id("gale_speed");
    private static final ResourceLocation HEART = PrimevaMod.id("ascension_health");
    private static final String ASCENDED = "primeva_ascended";
    private static final String CROWN_FLIGHT = "primeva_crown_flight";

    // ---------------- Ascensão ----------------
    private static CompoundTag persisted(Player p) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(Player.PERSISTED_NBT_TAG)) root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return root.getCompound(Player.PERSISTED_NBT_TAG);
    }

    public static boolean isAscended(Player p) {
        return persisted(p).getBoolean(ASCENDED);
    }

    public static void ascend(ServerPlayer p) {
        persisted(p).putBoolean(ASCENDED, true);
        applyAscension(p);
        p.setHealth(p.getMaxHealth());
    }

    private static void applyAscension(Player p) {
        AttributeInstance hp = p.getAttribute(Attributes.MAX_HEALTH);
        if (hp != null && !hp.hasModifier(HEART)) hp.addPermanentModifier(new AttributeModifier(HEART, 10.0, AttributeModifier.Operation.ADD_VALUE));
    }

    public static boolean fullPrimevalSet(Player p) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.PRIMEVAL_HELMET.get()) || p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ARCHMAGE_CROWN.get())
                ? p.getItemBySlot(EquipmentSlot.CHEST).is(ModItems.PRIMEVAL_CHESTPLATE.get())
                && p.getItemBySlot(EquipmentSlot.LEGS).is(ModItems.PRIMEVAL_LEGGINGS.get())
                && (p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.PRIMEVAL_BOOTS.get()) || p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GALE_BOOTS.get()))
                : false;
    }

    /** Protegido dos efeitos do miasma (armadura completa ou ascendido). */
    public static boolean miasmaImmune(Player p) {
        return isAscended(p) || fullPrimevalSet(p);
    }

    // Picareta Primeva: mineração 3x3
    private static final ThreadLocal<Boolean> MINING = ThreadLocal.withInitial(() -> false);

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent e) {
        if (!(e.getPlayer() instanceof ServerPlayer sp) || MINING.get() || sp.isShiftKeyDown()) return;
        ItemStack tool = sp.getMainHandItem();
        if (!tool.is(ModItems.PRIMEVAL_PICKAXE.get()) || !tool.isCorrectToolForDrops(e.getState())) return;
        HitResult hit = sp.pick(sp.blockInteractionRange() + 1, 0f, false);
        if (!(hit instanceof BlockHitResult bh)) return;
        Direction face = bh.getDirection();
        BlockPos c = e.getPos();
        MINING.set(true);
        try {
            for (int a = -1; a <= 1; a++) {
                for (int b = -1; b <= 1; b++) {
                    if (a == 0 && b == 0) continue;
                    BlockPos p = switch (face.getAxis()) {
                        case X -> c.offset(0, a, b);
                        case Y -> c.offset(a, 0, b);
                        case Z -> c.offset(a, b, 0);
                    };
                    BlockState st = sp.level().getBlockState(p);
                    if (st.isAir() || st.getDestroySpeed(sp.level(), p) < 0 || !tool.isCorrectToolForDrops(st)) continue;
                    if (tool.isEmpty()) break;
                    sp.gameMode.destroyBlock(p);
                }
            }
        } finally {
            MINING.set(false);
        }
    }

    // Ferramentas Elementais: veio de minério, árvore inteira e pá 3x3
    @SubscribeEvent
    public static void onElementalBreak(BlockEvent.BreakEvent e) {
        if (!(e.getPlayer() instanceof ServerPlayer sp) || MINING.get() || sp.isShiftKeyDown()) return;
        ItemStack tool = sp.getMainHandItem();
        BlockPos c = e.getPos();
        BlockState origin = e.getState();
        java.util.List<BlockPos> extra = new java.util.ArrayList<>();
        if (tool.is(ModItems.EMBER_PICKAXE.get()) && origin.is(net.neoforged.neoforge.common.Tags.Blocks.ORES)) {
            flood(sp, c, st -> st.getBlock() == origin.getBlock(), Config.get(Config.VEIN_LIMIT), extra);
        } else if (tool.is(ModItems.TIDE_AXE.get()) && origin.is(net.minecraft.tags.BlockTags.LOGS)) {
            flood(sp, c, st -> st.is(net.minecraft.tags.BlockTags.LOGS), Config.get(Config.TREE_LIMIT), extra);
        } else if (tool.is(ModItems.TERRA_SHOVEL.get()) && tool.isCorrectToolForDrops(origin)) {
            HitResult hit = sp.pick(sp.blockInteractionRange() + 1, 0f, false);
            if (!(hit instanceof BlockHitResult bh)) return;
            Direction face = bh.getDirection();
            for (int a = -1; a <= 1; a++) for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) continue;
                BlockPos p = switch (face.getAxis()) {
                    case X -> c.offset(0, a, b);
                    case Y -> c.offset(a, 0, b);
                    case Z -> c.offset(a, b, 0);
                };
                if (tool.isCorrectToolForDrops(sp.level().getBlockState(p))) extra.add(p);
            }
        } else return;
        MINING.set(true);
        try {
            for (BlockPos p : extra) {
                if (tool.isEmpty()) break;
                BlockState st = sp.level().getBlockState(p);
                if (st.isAir() || st.getDestroySpeed(sp.level(), p) < 0) continue;
                sp.gameMode.destroyBlock(p);
            }
        } finally {
            MINING.set(false);
        }
    }

    private static void flood(ServerPlayer sp, BlockPos start, java.util.function.Predicate<BlockState> match, int max, java.util.List<BlockPos> out) {
        java.util.ArrayDeque<BlockPos> q = new java.util.ArrayDeque<>();
        java.util.Set<BlockPos> seen = new java.util.HashSet<>();
        q.add(start);
        seen.add(start);
        while (!q.isEmpty() && out.size() < max) {
            BlockPos p = q.poll();
            for (BlockPos n : BlockPos.betweenClosed(p.offset(-1, -1, -1), p.offset(1, 1, 1))) {
                if (seen.contains(n) || out.size() >= max) continue;
                BlockPos im = n.immutable();
                seen.add(im);
                if (match.test(sp.level().getBlockState(im))) {
                    out.add(im);
                    q.add(im);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onAdvancement(AdvancementEvent.AdvancementEarnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Network.sync(sp);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Network.sync(sp);
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Network.sync(sp);
    }

    @SubscribeEvent
    public static void onDimension(PlayerEvent.PlayerChangedDimensionEvent e) {
        if (e.getEntity() instanceof ServerPlayer sp) Network.sync(sp);
    }

    // Botas do Vendaval: passo alto e velocidade
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post e) {
        Player p = e.getEntity();
        if (p.level().isClientSide || p.tickCount % 10 != 0) return;
        if (isAscended(p)) applyAscension(p);

        // Armadura Primeva completa: Resistência I
        if (fullPrimevalSet(p)) p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 40, 0, true, false, true));

        // Coroa do Arquimago: voo + visão noturna
        boolean crown = p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ARCHMAGE_CROWN.get());
        CompoundTag pd = persisted(p);
        if (crown) {
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, true));
            if (!p.getAbilities().mayfly) {
                p.getAbilities().mayfly = true;
                pd.putBoolean(CROWN_FLIGHT, true);
                p.onUpdateAbilities();
            }
        } else if (pd.getBoolean(CROWN_FLIGHT)) {
            pd.putBoolean(CROWN_FLIGHT, false);
            if (!p.isCreative() && !p.isSpectator()) {
                p.getAbilities().mayfly = false;
                p.getAbilities().flying = false;
                p.onUpdateAbilities();
            }
        }

        trinkets(p);
        setBonuses(p);

        boolean wearing = p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.GALE_BOOTS.get());
        AttributeInstance step = p.getAttribute(Attributes.STEP_HEIGHT);
        AttributeInstance speed = p.getAttribute(Attributes.MOVEMENT_SPEED);
        if (step == null || speed == null) return;
        if (wearing) {
            if (!step.hasModifier(STEP)) step.addTransientModifier(new AttributeModifier(STEP, 0.6, AttributeModifier.Operation.ADD_VALUE));
            if (!speed.hasModifier(SPEED)) speed.addTransientModifier(new AttributeModifier(SPEED, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        } else {
            if (step.hasModifier(STEP)) step.removeModifier(STEP);
            if (speed.hasModifier(SPEED)) speed.removeModifier(SPEED);
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent e) {
        if (e.getEntity().getItemBySlot(EquipmentSlot.FEET).is(ModItems.GALE_BOOTS.get())) {
            e.setDamageMultiplier(e.getDamageMultiplier() * 0.25f);
        }
        if (e.getEntity().getItemBySlot(EquipmentSlot.HEAD).is(ModItems.ARCHMAGE_CROWN.get())) {
            e.setDamageMultiplier(0f);
        }
    }

    // ---------------- Anel Rúnico e Aço do Vazio ----------------
    private static final ResourceLocation RUNIC = PrimevaMod.id("runic_shield");
    private static final String LAST_HURT = "primeva_last_hurt";
    private static final ResourceLocation GIRDLE = PrimevaMod.id("girdle");
    private static final ResourceLocation TRAV_STEP = PrimevaMod.id("traveler_step");
    private static final ResourceLocation TRAV_SPEED = PrimevaMod.id("traveler_speed");
    private static final ResourceLocation TRAV_JUMP = PrimevaMod.id("traveler_jump");

    /** Amuleto da Essência no inventário: 30% de chance de magias não gastarem carga. */
    public static boolean saveCharge(Player p) {
        if (p.getRandom().nextDouble() >= Config.get(Config.AMULET_CHANCE)) return false;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) if (p.getInventory().getItem(i).is(ModItems.ESSENCE_AMULET.get())) return true;
        return false;
    }
    public static float runicMax() {
        return Config.get(Config.RUNIC_HEARTS) * 2f;
    }

    private static void trinkets(Player p) {
        boolean ring = false, girdle = false, verdant = false, cloud = false;
        boolean repairTick = p.tickCount % 40 == 0;
        for (int i = 0; i < p.getInventory().getContainerSize(); i++) {
            ItemStack s = p.getInventory().getItem(i);
            if (s.isEmpty()) continue;
            if (s.is(ModItems.RUNIC_RING.get())) ring = true;
            if (s.is(ModItems.STEADFAST_GIRDLE.get())) girdle = true;
            if (s.is(ModItems.VERDANT_CHARM.get())) verdant = true;
            if (s.is(ModItems.CLOUD_RING.get())) cloud = true;
            if (repairTick && s.isDamaged() && isVoidsteel(s) && !(p.isUsingItem() && p.getUseItem() == s)) s.setDamageValue(s.getDamageValue() - 1);
        }
        AttributeInstance kb = p.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (kb != null) {
            if (girdle && !kb.hasModifier(GIRDLE)) kb.addTransientModifier(new AttributeModifier(GIRDLE, 1.0, AttributeModifier.Operation.ADD_VALUE));
            if (!girdle && kb.hasModifier(GIRDLE)) kb.removeModifier(GIRDLE);
        }
        if (cloud) {
            p.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 1, true, false, true));
            if (p.fallDistance > 7f) p.fallDistance = 7f;
        }
        if (verdant && p.tickCount % 40 == 0) {
            p.removeEffect(MobEffects.POISON);
            p.removeEffect(MobEffects.WITHER);
            p.removeEffect(MobEffects.HUNGER);
            p.removeEffect(MobEffects.CONFUSION);
            if (p.tickCount % 400 == 0) p.getFoodData().eat(1, 0.5f);
        }
        boolean trav = p.getItemBySlot(EquipmentSlot.FEET).is(ModItems.TRAVELER_BOOTS.get());
        AttributeInstance st = p.getAttribute(Attributes.STEP_HEIGHT), sp = p.getAttribute(Attributes.MOVEMENT_SPEED), jp = p.getAttribute(Attributes.JUMP_STRENGTH);
        if (st != null && sp != null && jp != null) {
            if (trav) {
                if (!st.hasModifier(TRAV_STEP)) st.addTransientModifier(new AttributeModifier(TRAV_STEP, 0.6, AttributeModifier.Operation.ADD_VALUE));
                if (!sp.hasModifier(TRAV_SPEED)) sp.addTransientModifier(new AttributeModifier(TRAV_SPEED, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                if (!jp.hasModifier(TRAV_JUMP)) jp.addTransientModifier(new AttributeModifier(TRAV_JUMP, 0.12, AttributeModifier.Operation.ADD_VALUE));
            } else {
                st.removeModifier(TRAV_STEP);
                sp.removeModifier(TRAV_SPEED);
                jp.removeModifier(TRAV_JUMP);
            }
        }
        AttributeInstance maxAbs = p.getAttribute(Attributes.MAX_ABSORPTION);
        if (maxAbs == null) return;
        if (ring) {
            AttributeModifier cur = maxAbs.getModifier(RUNIC);
            if (cur == null || cur.amount() != runicMax()) maxAbs.addOrUpdateTransientModifier(new AttributeModifier(RUNIC, runicMax(), AttributeModifier.Operation.ADD_VALUE));
            long last = p.getPersistentData().getLong(LAST_HURT);
            if (p.tickCount % 20 == 0 && p.level().getGameTime() - last > 160 && p.getAbsorptionAmount() < runicMax()) {
                boolean was = p.getAbsorptionAmount() <= 0;
                p.setAbsorptionAmount(Math.min(runicMax(), p.getAbsorptionAmount() + 2f));
                if (was) p.level().playSound(null, p.blockPosition(), net.minecraft.sounds.SoundEvents.AMETHYST_BLOCK_CHIME,
                        net.minecraft.sounds.SoundSource.PLAYERS, 0.6f, 1.6f);
            }
        } else if (maxAbs.hasModifier(RUNIC)) {
            maxAbs.removeModifier(RUNIC);
        }
    }

    public static boolean isVoidsteel(ItemStack s) {
        return s.is(ModItems.VOIDSTEEL_SWORD.get()) || s.is(ModItems.VOIDSTEEL_PICKAXE.get())
                || s.is(ModItems.VOIDSTEEL_AXE.get()) || s.is(ModItems.VOIDSTEEL_SHOVEL.get())
                || s.is(ModItems.VOIDSTEEL_HELMET.get()) || s.is(ModItems.VOIDSTEEL_CHESTPLATE.get())
                || s.is(ModItems.VOIDSTEEL_LEGGINGS.get()) || s.is(ModItems.VOIDSTEEL_BOOTS.get());
    }

    // ---------------- Bônus de conjunto das armaduras ----------------
    private static final ResourceLocation SET_PRIMEVAL_HP = PrimevaMod.id("set_primeval_health");
    private static final ResourceLocation SET_ARCANITE_MINE = PrimevaMod.id("set_arcanite_mining");
    private static final ResourceLocation SET_ARCANITE_SPEED = PrimevaMod.id("set_arcanite_speed");
    private static final ResourceLocation SET_CRIMSON_DMG = PrimevaMod.id("set_crimson_damage");

    public static boolean fullSet(Player p, net.minecraft.world.item.Item h, net.minecraft.world.item.Item c, net.minecraft.world.item.Item l, net.minecraft.world.item.Item b) {
        return p.getItemBySlot(EquipmentSlot.HEAD).is(h) && p.getItemBySlot(EquipmentSlot.CHEST).is(c)
                && p.getItemBySlot(EquipmentSlot.LEGS).is(l) && p.getItemBySlot(EquipmentSlot.FEET).is(b);
    }

    private static void toggle(Player p, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> attr, ResourceLocation id,
                               boolean on, double amount, AttributeModifier.Operation op) {
        AttributeInstance a = p.getAttribute(attr);
        if (a == null) return;
        if (on && !a.hasModifier(id)) a.addTransientModifier(new AttributeModifier(id, amount, op));
        if (!on && a.hasModifier(id)) a.removeModifier(id);
    }

    private static void setBonuses(Player p) {
        // Primeva: +4 corações (além de Resistência I e imunidade ao miasma)
        toggle(p, Attributes.MAX_HEALTH, SET_PRIMEVAL_HP, fullPrimevalSet(p), 8.0, AttributeModifier.Operation.ADD_VALUE);
        // Arcanita: +25% velocidade de mineração e +10% velocidade
        boolean arc = fullSet(p, ModItems.ARCANITE_HELMET.get(), ModItems.ARCANITE_CHESTPLATE.get(), ModItems.ARCANITE_LEGGINGS.get(), ModItems.ARCANITE_BOOTS.get());
        toggle(p, Attributes.BLOCK_BREAK_SPEED, SET_ARCANITE_MINE, arc, 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        toggle(p, Attributes.MOVEMENT_SPEED, SET_ARCANITE_SPEED, arc, 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        // Carmesim: +3 de dano; à noite também Força I
        boolean cr = fullSet(p, ModItems.CRIMSON_HELMET.get(), ModItems.CRIMSON_CHESTPLATE.get(), ModItems.CRIMSON_LEGGINGS.get(), ModItems.CRIMSON_BOOTS.get());
        toggle(p, Attributes.ATTACK_DAMAGE, SET_CRIMSON_DMG, cr, 3.0, AttributeModifier.Operation.ADD_VALUE);
        if (cr && !p.level().isDay()) p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, false, true));
        // Aço do Vazio: Visão Noturna e Força I, mas o vazio sussurra (+1 de mente temporária a cada 10 min)
        boolean vd = fullSet(p, ModItems.VOIDSTEEL_HELMET.get(), ModItems.VOIDSTEEL_CHESTPLATE.get(), ModItems.VOIDSTEEL_LEGGINGS.get(), ModItems.VOIDSTEEL_BOOTS.get());
        if (vd) {
            p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, true, false, true));
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 0, true, false, true));
            if (p.tickCount % 12000 == 0 && p instanceof net.minecraft.server.level.ServerPlayer sp) Warp.addTemp(sp, 1);
        }
        if (p.getHealth() > p.getMaxHealth()) p.setHealth(p.getMaxHealth());
    }

    /** Armadura Carmesim completa: cada golpe cura 15% do dano causado. */
    @SubscribeEvent
    public static void onCrimsonHit(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post e) {
        if (e.getSource().getEntity() instanceof Player p && !p.level().isClientSide
                && fullSet(p, ModItems.CRIMSON_HELMET.get(), ModItems.CRIMSON_CHESTPLATE.get(), ModItems.CRIMSON_LEGGINGS.get(), ModItems.CRIMSON_BOOTS.get())) {
            p.heal(e.getNewDamage() * 0.15f);
        }
    }

    @SubscribeEvent
    public static void onHurt(LivingIncomingDamageEvent e) {
        if (e.getEntity() instanceof Player p && !p.level().isClientSide) p.getPersistentData().putLong(LAST_HURT, p.level().getGameTime());
    }

    private CommonEvents() {}
}
