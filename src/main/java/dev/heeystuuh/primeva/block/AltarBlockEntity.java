package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.*;
import dev.heeystuuh.primeva.item.PhialItem;
import dev.heeystuuh.primeva.recipe.InfusionRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Altar de Infusão. O ritual tem três fases:
 * 1) Carga (o altar acende e junta energia), 2) Absorção (uma oferenda por segundo, com o rastro na cor da essência),
 * 3) Estabilização (o resultado se forma). Se o ritual for interrompido, tudo que já foi consumido é devolvido.
 */
public class AltarBlockEntity extends SyncedBlockEntity implements DisplayItem {
    private static final int RADIUS = 4;
    private static final int STEP_TICKS = 20, CHARGE_TICKS = 40, SETTLE_TICKS = 40;
    public static final int PHASE_IDLE = 0, PHASE_CHARGE = 1, PHASE_DRAW = 2, PHASE_SETTLE = 3, PHASE_DRAIN = 4;

    private ItemStack center = ItemStack.EMPTY;
    private boolean active;
    private int phase;
    private ResourceLocation recipeId;
    private final List<BlockPos> queue = new ArrayList<>();
    /** O que já foi consumido (para devolver se o ritual cair). Cada entrada: item + posição de origem. */
    private final List<CompoundTag> consumed = new ArrayList<>();
    private int step;
    private int ticks;
    private int instability;
    private int tier = 1;
    /** Essência que ainda falta drenar dos jarros. */
    private final EnumMap<Essence, Integer> drainLeft = new EnumMap<>(Essence.class);
    private int stalls;

    public int instability() {
        return instability;
    }

    private int collapseAt() {
        return 8 + tier * 4;
    }

    private int countStabilizers() {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-6, -2, -6), worldPosition.offset(6, 3, 6))) {
            if (level.getBlockState(p).is(ModBlocks.RUNIC_STABILIZER.get())) n++;
        }
        return Math.min(n, 8);
    }

    private List<EssenceJarBlockEntity> nearbyJars() {
        List<EssenceJarBlockEntity> out = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-RADIUS - 2, -1, -RADIUS - 2), worldPosition.offset(RADIUS + 2, 2, RADIUS + 2))) {
            if (level.getBlockEntity(p) instanceof EssenceJarBlockEntity jar) out.add(jar);
        }
        return out;
    }

    public AltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR.get(), pos, state);
    }

    private int countPillars() {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-6, -2, -6), worldPosition.offset(6, 3, 6))) {
            if (level.getBlockState(p).is(ModBlocks.CONTAINMENT_PILLAR.get())) n++;
        }
        return n;
    }

    public ItemStack getCenter() {
        return center;
    }

    public void setCenter(ItemStack stack) {
        center = stack;
        sync();
    }

    public boolean isActive() {
        return active;
    }

    public int phase() {
        return phase;
    }

    @Override
    public ItemStack displayItem() {
        return center;
    }

    @Override
    public double displayHeight() {
        return active ? 1.3 + Math.min(1, ticks / 80.0) * 0.4 : 1.3;
    }

    private List<PedestalBlockEntity> findPedestals() {
        List<PedestalBlockEntity> out = new ArrayList<>();
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-RADIUS, -1, -RADIUS), worldPosition.offset(RADIUS, 1, RADIUS))) {
            if (level.getBlockEntity(p) instanceof PedestalBlockEntity pb && !pb.getItem().isEmpty()) out.add(pb);
        }
        return out;
    }

    private void tell(ServerPlayer player, String key, ChatFormatting color, Object... args) {
        player.displayClientMessage(Component.translatable(key, args).withStyle(color), true);
    }

    private void setLit(boolean lit) {
        BlockState st = getBlockState();
        if (st.hasProperty(AltarBlock.ACTIVE) && st.getValue(AltarBlock.ACTIVE) != lit) level.setBlock(worldPosition, st.setValue(AltarBlock.ACTIVE, lit), Block.UPDATE_ALL);
    }

    /** Quantas essências (em frascos) a receita pede. */
    private static Map<Essence, Integer> essenceCost(InfusionRecipe r) {
        Map<Essence, Integer> out = new EnumMap<>(Essence.class);
        for (Ingredient ing : r.pedestals) {
            for (ItemStack s : ing.getItems()) {
                if (s.getItem() instanceof PhialItem p) {
                    out.merge(p.essence, PhialItem.AMOUNT, Integer::sum);
                    break;
                }
            }
        }
        return out;
    }

    public void tryStart(ServerPlayer player) {
        if (active) {
            tell(player, "msg.primeva.altar_busy", ChatFormatting.GRAY);
            return;
        }
        if (center.isEmpty()) {
            tell(player, "msg.primeva.altar_empty", ChatFormatting.GRAY);
            return;
        }
        List<PedestalBlockEntity> peds = findPedestals();
        // jarros de essência por perto valem como frascos
        List<ItemStack> items = new ArrayList<>();
        List<BlockPos> sources = new ArrayList<>();
        for (PedestalBlockEntity pb : peds) {
            items.add(pb.getItem());
            sources.add(pb.getBlockPos());
        }
        for (BlockPos p : BlockPos.betweenClosed(worldPosition.offset(-RADIUS - 2, -1, -RADIUS - 2), worldPosition.offset(RADIUS + 2, 2, RADIUS + 2))) {
            if (level.getBlockEntity(p) instanceof EssenceJarBlockEntity jar && jar.essence() != null) {
                int n = Math.min(8, jar.amount() / PhialItem.AMOUNT);
                for (int i = 0; i < n; i++) {
                    items.add(new ItemStack(ModItems.phial(jar.essence())));
                    sources.add(p.immutable());
                }
            }
        }
        Knowledge k = player.getData(ModAttachments.KNOWLEDGE);

        InfusionRecipe closest = null;
        int closestScore = -1;
        for (RecipeHolder<InfusionRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.INFUSION_TYPE.get())) {
            InfusionRecipe r = holder.value();
            int[] assign = r.match(center, items);
            if (assign == null) {
                if (r.center.test(center)) {
                    int score = matchedCount(r, items);
                    if (score > closestScore) {
                        closestScore = score;
                        closest = r;
                    }
                }
                continue;
            }
            if (!k.research.contains(r.research)) {
                Research res = Research.get(r.research);
                Component name = res != null ? res.title() : Component.literal(r.research);
                tell(player, "msg.primeva.need_research", ChatFormatting.RED, name);
                return;
            }
            int pillars = countPillars();
            int need = InfusionRecipe.pillarsNeeded(r.tier);
            if (pillars < need) {
                tell(player, "msg.primeva.need_pillars", ChatFormatting.RED, need, pillars);
                return;
            }
            if (r.tier >= 3 && !level.getBlockState(worldPosition.below()).is(ModBlocks.CATALYST.get())) {
                tell(player, "msg.primeva.need_catalyst", ChatFormatting.RED);
                return;
            }
            queue.clear();
            consumed.clear();
            int usedPeds = 0;
            for (int idx : assign) {
                queue.add(sources.get(idx));
                if (idx < peds.size()) usedPeds++;
            }
            // Instabilidade: base da receita + pedestais assimétricos + oferendas sobrando - pilares
            // cada pedestal precisa de um par espelhado do outro lado do altar
            int unpaired = 0;
            for (PedestalBlockEntity pb : peds) {
                BlockPos m = new BlockPos(2 * worldPosition.getX() - pb.getBlockPos().getX(), pb.getBlockPos().getY(), 2 * worldPosition.getZ() - pb.getBlockPos().getZ());
                if (!(level.getBlockEntity(m) instanceof PedestalBlockEntity mp) || mp.getItem().isEmpty()) unpaired++;
            }
            int stab = countStabilizers();
            int miasma = Miasma.get(level, worldPosition) / 20;
            instability = Math.max(0, r.instability + tier + unpaired * 2 + (peds.size() - usedPeds) + miasma - Math.min(pillars, 12) / 3 - stab);
            drainLeft.clear();
            drainLeft.putAll(r.essenceDrain());
            stalls = 0;
            // avisa se os jarros por perto não têm essência suficiente
            Map<Essence, Integer> avail = new EnumMap<>(Essence.class);
            for (EssenceJarBlockEntity j : nearbyJars()) if (j.essence() != null) avail.merge(j.essence(), j.amount(), Integer::sum);
            boolean short_ = false;
            for (Map.Entry<Essence, Integer> e : drainLeft.entrySet()) if (avail.getOrDefault(e.getKey(), 0) < e.getValue()) short_ = true;
            if (short_) player.displayClientMessage(Component.translatable("msg.primeva.altar_low_jars").withStyle(ChatFormatting.RED), false);
            tier = r.tier;
            recipeId = holder.id();
            active = true;
            phase = PHASE_CHARGE;
            step = 0;
            ticks = 0;
            setLit(true);
            level.playSound(null, worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1f, 1.3f);
            MutableComponent msg = Component.translatable("msg.primeva.altar_start", instability).withStyle(ChatFormatting.LIGHT_PURPLE);
            msg.append(Component.literal("  ")).append(Component.translatable("msg.primeva.altar_drain").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(" ")).append(Essence.line(drainLeft, ""));
            player.displayClientMessage(msg, true);
            sync();
            return;
        }
        if (closest != null) {
            // mostra exatamente o que falta
            List<Component> missing = missing(closest, items);
            MutableComponent msg = Component.translatable("msg.primeva.altar_missing").withStyle(ChatFormatting.GOLD);
            for (int i = 0; i < Math.min(4, missing.size()); i++) {
                msg.append(Component.literal(i == 0 ? " " : ", ")).append(missing.get(i).copy().withStyle(ChatFormatting.WHITE));
            }
            if (missing.size() > 4) msg.append(Component.literal(" +" + (missing.size() - 4)));
            player.displayClientMessage(msg, true);
            return;
        }
        tell(player, "msg.primeva.altar_nomatch", ChatFormatting.GRAY);
    }

    private static int matchedCount(InfusionRecipe r, List<ItemStack> items) {
        List<ItemStack> pool = new ArrayList<>(items);
        int n = 0;
        for (Ingredient ing : r.pedestals) {
            for (int i = 0; i < pool.size(); i++) {
                if (ing.test(pool.get(i))) {
                    pool.remove(i);
                    n++;
                    break;
                }
            }
        }
        return n;
    }

    private static List<Component> missing(InfusionRecipe r, List<ItemStack> items) {
        List<ItemStack> pool = new ArrayList<>(items);
        List<Component> out = new ArrayList<>();
        for (Ingredient ing : r.pedestals) {
            boolean found = false;
            for (int i = 0; i < pool.size(); i++) {
                if (ing.test(pool.get(i))) {
                    pool.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                ItemStack[] opts = ing.getItems();
                out.add(opts.length > 0 ? opts[0].getHoverName() : Component.literal("?"));
            }
        }
        return out;
    }

    private InfusionRecipe recipe() {
        if (recipeId == null) return null;
        return level.getRecipeManager().byKey(recipeId)
                .map(RecipeHolder::value)
                .filter(v -> v instanceof InfusionRecipe)
                .map(v -> (InfusionRecipe) v)
                .orElse(null);
    }

    private static Vector3f colorOf(ItemStack s) {
        if (s.getItem() instanceof PhialItem p) return p.essence.color();
        Map<Essence, Integer> e = ModDataMaps.of(s.getItem());
        Essence best = null;
        for (Map.Entry<Essence, Integer> x : e.entrySet()) if (best == null || x.getValue() > e.get(best)) best = x.getKey();
        return best != null ? best.color() : new Vector3f(0.7f, 0.4f, 1f);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AltarBlockEntity be) {
        if (!be.active || !(level instanceof ServerLevel sl)) return;
        be.ticks++;
        double cx = pos.getX() + 0.5, cy = pos.getY() + 1.3, cz = pos.getZ() + 0.5;
        InfusionRecipe r = be.recipe();
        if (r == null) {
            be.fail(sl);
            return;
        }

        if (be.phase != PHASE_CHARGE && be.ticks % 40 == 0) {
            float grow = 0.12f * be.tier - 0.04f * be.countStabilizers();
            if (level.random.nextFloat() < grow) be.instability++;
            if (be.phase == PHASE_DRAIN && level.random.nextInt(100) < be.instability * 3) be.mishap(sl);
            if (be.instability >= be.collapseAt()) {
                be.collapse(sl);
                return;
            }
        }
        switch (be.phase) {
            case PHASE_CHARGE -> {
                // espiral subindo ao redor do altar
                double a = be.ticks * 0.35;
                for (int i = 0; i < 3; i++) {
                    double ang = a + i * 2.094;
                    double rad = 1.2 - be.ticks / (double) CHARGE_TICKS * 0.6;
                    sl.sendParticles(ParticleTypes.ENCHANT, cx + Math.cos(ang) * rad, pos.getY() + 0.3 + be.ticks / 40.0, cz + Math.sin(ang) * rad, 1, 0, 0, 0, 0);
                }
                if (be.ticks % 20 == 0) level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.8f, 1.2f);
                if (be.ticks >= CHARGE_TICKS) {
                    be.phase = PHASE_DRAW;
                    be.ticks = 0;
                    be.sync();
                }
            }
            case PHASE_DRAW -> {
                if (be.ticks % 3 == 0 && be.step < be.queue.size()) {
                    BlockPos p = be.queue.get(be.step);
                    ItemStack shown = be.level.getBlockEntity(p) instanceof PedestalBlockEntity pb ? pb.getItem()
                            : be.level.getBlockEntity(p) instanceof EssenceJarBlockEntity jar && jar.essence() != null ? new ItemStack(ModItems.phial(jar.essence())) : ItemStack.EMPTY;
                    DustParticleOptions dust = new DustParticleOptions(colorOf(shown), 1.0f);
                    double t = (be.ticks % STEP_TICKS) / (double) STEP_TICKS;
                    for (int i = 0; i < 3; i++) {
                        double tt = Math.min(1, t + i * 0.08);
                        sl.sendParticles(dust, p.getX() + 0.5 + (cx - p.getX() - 0.5) * tt, p.getY() + 1.2 + (cy - p.getY() - 1.2) * tt + Math.sin(tt * Math.PI) * 0.6,
                                p.getZ() + 0.5 + (cz - p.getZ() - 0.5) * tt, 1, 0.02, 0.02, 0.02, 0);
                    }
                }
                if (be.ticks % STEP_TICKS != 0) return;
                if (be.step < be.queue.size()) {
                    if (!be.consumeStep(sl, r)) {
                        be.fail(sl);
                        return;
                    }
                    if (level.random.nextInt(100) < be.instability * 4) be.mishap(sl);
                    be.step++;
                    be.sync();
                    return;
                }
                be.phase = PHASE_DRAIN;
                be.ticks = 0;
                level.playSound(null, pos, SoundEvents.PORTAL_TRIGGER, SoundSource.BLOCKS, 0.3f, 1.8f);
                be.sync();
            }
            case PHASE_DRAIN -> {
                if (be.ticks % 10 != 0) return;
                boolean progress = false;
                for (EssenceJarBlockEntity j : be.nearbyJars()) {
                    Essence e = j.essence();
                    if (e == null) continue;
                    int left = be.drainLeft.getOrDefault(e, 0);
                    if (left <= 0) continue;
                    int n = Math.min(Math.min(left, 4 * be.tier), j.amount());
                    if (n > 0 && j.take(e, n)) {
                        be.drainLeft.put(e, left - n);
                        progress = true;
                        BlockPos jp = j.getBlockPos();
                        DustParticleOptions dust = new DustParticleOptions(e.color(), 1.2f);
                        for (int i = 0; i <= 8; i++) {
                            double t = i / 8.0;
                            sl.sendParticles(dust, jp.getX() + 0.5 + (cx - jp.getX() - 0.5) * t, jp.getY() + 0.8 + (cy - jp.getY() - 0.8) * t,
                                    jp.getZ() + 0.5 + (cz - jp.getZ() - 0.5) * t, 1, 0.02, 0.02, 0.02, 0);
                        }
                    }
                }
                be.drainLeft.values().removeIf(v -> v <= 0);
                if (be.drainLeft.isEmpty()) {
                    be.phase = PHASE_SETTLE;
                    be.ticks = 0;
                    be.sync();
                    return;
                }
                if (progress) {
                    be.stalls = 0;
                    if (be.ticks % 20 == 0) level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.6f, 1.6f);
                } else if (++be.stalls % 4 == 0) {
                    // sem essência: o ritual fica instável
                    be.instability++;
                    sl.sendParticles(ParticleTypes.SMOKE, cx, cy, cz, 10, 0.3, 0.3, 0.3, 0.02);
                    for (Player p : sl.getEntitiesOfClass(Player.class, new AABB(pos).inflate(10))) {
                        p.displayClientMessage(Component.translatable("msg.primeva.altar_starving", Essence.line(be.drainLeft, "")).withStyle(ChatFormatting.RED), true);
                    }
                }
                if (be.stalls >= 40) {
                    be.collapse(sl);
                    return;
                }
                be.sync();
            }
            case PHASE_SETTLE -> {
                double rad = 0.8 * (1 - be.ticks / (double) SETTLE_TICKS);
                for (int i = 0; i < 6; i++) {
                    double ang = be.ticks * 0.5 + i * 1.047;
                    sl.sendParticles(ParticleTypes.END_ROD, cx + Math.cos(ang) * rad, cy, cz + Math.sin(ang) * rad, 1, 0, 0, 0, 0);
                }
                if (be.ticks >= SETTLE_TICKS) be.complete(sl, r);
            }
            default -> be.fail(sl);
        }
    }

    /** Consome a oferenda do passo atual. Retorna false se ela sumiu. */
    private boolean consumeStep(ServerLevel sl, InfusionRecipe r) {
        BlockPos p = queue.get(step);
        Ingredient need = r.pedestals.get(step);
        CompoundTag rec = new CompoundTag();
        rec.putLong("pos", p.asLong());
        if (sl.getBlockEntity(p) instanceof EssenceJarBlockEntity jar) {
            Essence e = jar.essence();
            if (e == null || !need.test(new ItemStack(ModItems.phial(e))) || !jar.take(e, PhialItem.AMOUNT)) return false;
            rec.putString("essence", e.id);
        } else if (sl.getBlockEntity(p) instanceof PedestalBlockEntity pb && need.test(pb.getItem())) {
            rec.put("item", pb.getItem().save(sl.registryAccess()));
            pb.setItem(ItemStack.EMPTY);
        } else {
            return false;
        }
        consumed.add(rec);
        sl.sendParticles(ParticleTypes.END_ROD, p.getX() + 0.5, p.getY() + 1.1, p.getZ() + 0.5, 12, 0.15, 0.15, 0.15, 0.03);
        level.playSound(null, p, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1f, 0.8f + step * 0.08f);
        return true;
    }

    private void complete(ServerLevel sl, InfusionRecipe r) {
        BlockPos pos = worldPosition;
        double cx = pos.getX() + 0.5, cy = pos.getY() + 1.3, cz = pos.getZ() + 0.5;
        center = r.result.copy();
        active = false;
        phase = PHASE_IDLE;
        queue.clear();
        consumed.clear();
        drainLeft.clear();
        recipeId = null;
        setLit(false);
        Miasma.add(sl, pos, Math.max(1, instability));
        sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, cx, cy, cz, 60, 0.3, 0.4, 0.3, 0.3);
        sl.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1f, 0.8f);
        if (tier >= 3) {
            sl.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.BLOCKS, 0.6f, 1.2f);
            sl.sendParticles(ParticleTypes.REVERSE_PORTAL, cx, cy, cz, 200, 1.5, 1.5, 1.5, 0.05);
        }
        Discovery.near(sl, pos, "do:infusion");
        sync();
    }

    /** Devolve tudo o que o ritual já tinha consumido. */
    private void refund(Level lv) {
        for (CompoundTag rec : consumed) {
            BlockPos from = BlockPos.of(rec.getLong("pos"));
            if (rec.contains("essence")) {
                Essence e = Essence.byId(rec.getString("essence"));
                if (e == null) continue;
                if (lv.getBlockEntity(from) instanceof EssenceJarBlockEntity jar && jar.insert(e, PhialItem.AMOUNT) == PhialItem.AMOUNT) continue;
                Block.popResource(lv, worldPosition.above(), new ItemStack(ModItems.phial(e)));
            } else if (rec.contains("item")) {
                ItemStack s = ItemStack.parseOptional(lv.registryAccess(), rec.getCompound("item"));
                if (s.isEmpty()) continue;
                if (lv.getBlockEntity(from) instanceof PedestalBlockEntity pb && pb.getItem().isEmpty()) pb.setItem(s);
                else Block.popResource(lv, worldPosition.above(), s);
            }
        }
        consumed.clear();
    }

    /** Ritual interrompido: devolve as oferendas, mantém o item central e aplica uma punição previsível (sem destruir blocos). */
    private void fail(ServerLevel sl) {
        refund(sl);
        active = false;
        phase = PHASE_IDLE;
        queue.clear();
        recipeId = null;
        setLit(false);
        Miasma.add(sl, worldPosition, 5 * tier * tier);
        sl.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5, 1.0f + tier * 0.5f, Level.ExplosionInteraction.NONE);
        for (int i = 1; i < tier * 2; i++) Miasma.spawnVex(sl, worldPosition, sl.random);
        for (Player p : sl.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(10))) {
            p.displayClientMessage(Component.translatable("msg.primeva.altar_fail").withStyle(ChatFormatting.DARK_RED), true);
        }
        sync();
    }

    /** Colapso por instabilidade: metade das oferendas se perde e a explosão de miasma machuca quem está perto. */
    private void collapse(ServerLevel sl) {
        RandomSource rnd = sl.random;
        consumed.removeIf(c -> rnd.nextBoolean());
        refund(sl);
        active = false;
        phase = PHASE_IDLE;
        queue.clear();
        drainLeft.clear();
        recipeId = null;
        setLit(false);
        Miasma.add(sl, worldPosition, 10 * tier * tier);
        sl.explode(null, worldPosition.getX() + 0.5, worldPosition.getY() + 1.2, worldPosition.getZ() + 0.5, 2.0f + tier, Level.ExplosionInteraction.NONE);
        for (int i = 0; i < tier * 2; i++) Miasma.spawnWraith(sl, worldPosition, rnd);
        for (Player p : sl.getEntitiesOfClass(Player.class, new AABB(worldPosition).inflate(10))) {
            p.hurt(sl.damageSources().magic(), 3f * tier);
            p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
            if (p instanceof ServerPlayer sp) Warp.addTemp(sp, 2 * tier);
            p.displayClientMessage(Component.translatable("msg.primeva.altar_collapse").withStyle(ChatFormatting.DARK_RED), false);
        }
        sync();
    }

    /** Altar quebrado no meio do ritual: devolve tudo sem punição extra. */
    public void abort() {
        if (level == null || level.isClientSide) return;
        if (active) refund(level);
        active = false;
        queue.clear();
        recipeId = null;
    }

    private void mishap(ServerLevel sl) {
        RandomSource r = sl.random;
        BlockPos pos = worldPosition;
        switch (r.nextInt(6)) {
            case 4 -> { // raio de miasma (só visual + dano mágico, não queima nada)
                for (Player p : sl.getEntitiesOfClass(Player.class, new AABB(pos).inflate(8))) {
                    net.minecraft.world.entity.LightningBolt bolt = net.minecraft.world.entity.EntityType.LIGHTNING_BOLT.create(sl);
                    if (bolt != null) {
                        bolt.moveTo(p.getX(), p.getY(), p.getZ());
                        bolt.setVisualOnly(true);
                        sl.addFreshEntity(bolt);
                    }
                    p.hurt(sl.damageSources().magic(), 2f * tier);
                }
            }
            case 5 -> Miasma.spawnWraith(sl, pos, r);
            case 0 -> { // repele uma oferenda que ainda não foi consumida (cai no chão, não some)
                if (step + 1 < queue.size()) {
                    BlockPos p = queue.get(step + 1 + r.nextInt(queue.size() - step - 1));
                    if (sl.getBlockEntity(p) instanceof PedestalBlockEntity pb && !pb.getItem().isEmpty()) {
                        Block.popResource(sl, p.above(), pb.getItem());
                        pb.setItem(ItemStack.EMPTY);
                    }
                }
            }
            case 1 -> {
                for (Player p : sl.getEntitiesOfClass(Player.class, new AABB(pos).inflate(8))) {
                    p.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
                    p.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
                }
            }
            case 2 -> Miasma.spawnVex(sl, pos, r);
            default -> {
                Miasma.add(sl, pos, 5);
                sl.sendParticles(ParticleTypes.SQUID_INK, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 30, 0.6, 0.4, 0.6, 0.02);
            }
        }
        sl.playSound(null, pos, SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.BLOCKS, 0.4f, 1.6f);
        for (Player p : sl.getEntitiesOfClass(Player.class, new AABB(pos).inflate(10))) {
            p.displayClientMessage(Component.translatable("msg.primeva.altar_unstable").withStyle(ChatFormatting.RED), true);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        putStack(tag, "center", center, registries);
        tag.putBoolean("active", active);
        tag.putInt("phase", phase);
        if (recipeId != null) tag.putString("recipe", recipeId.toString());
        ListTag q = new ListTag();
        queue.forEach(p -> q.add(LongTag.valueOf(p.asLong())));
        tag.put("queue", q);
        ListTag c = new ListTag();
        c.addAll(consumed);
        tag.put("consumed", c);
        tag.putInt("step", step);
        tag.putInt("ticks", ticks);
        tag.putInt("instability", instability);
        tag.putInt("tier", tier);
        CompoundTag dl = new CompoundTag();
        drainLeft.forEach((e, n) -> dl.putInt(e.id, n));
        tag.put("drain", dl);
        tag.putInt("stalls", stalls);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        center = getStack(tag, "center", registries);
        active = tag.getBoolean("active");
        phase = tag.contains("phase") ? tag.getInt("phase") : (active ? PHASE_DRAW : PHASE_IDLE);
        recipeId = tag.contains("recipe") ? ResourceLocation.tryParse(tag.getString("recipe")) : null;
        queue.clear();
        ListTag q = tag.getList("queue", Tag.TAG_LONG);
        for (int i = 0; i < q.size(); i++) queue.add(BlockPos.of(((LongTag) q.get(i)).getAsLong()));
        consumed.clear();
        ListTag c = tag.getList("consumed", Tag.TAG_COMPOUND);
        for (int i = 0; i < c.size(); i++) consumed.add(c.getCompound(i));
        step = tag.getInt("step");
        ticks = tag.getInt("ticks");
        instability = tag.getInt("instability");
        tier = Math.max(1, tag.getInt("tier"));
        drainLeft.clear();
        CompoundTag dl = tag.getCompound("drain");
        for (Essence e : Essence.values()) if (dl.contains(e.id)) drainLeft.put(e, dl.getInt(e.id));
        stalls = tag.getInt("stalls");
    }
}
