package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.*;
import dev.heeystuuh.primeva.recipe.CrucibleRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CrucibleBlockEntity extends SyncedBlockEntity {
    public static int capacity() {
        return dev.heeystuuh.primeva.Config.get(dev.heeystuuh.primeva.Config.CRUCIBLE_CAPACITY);
    }

    private boolean water;
    private final EnumMap<Essence, Integer> stored = new EnumMap<>(Essence.class);

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRUCIBLE.get(), pos, state);
    }

    public boolean hasWater() {
        return water;
    }

    public Map<Essence, Integer> essences() {
        return stored;
    }

    public int total() {
        int t = 0;
        for (int v : stored.values()) t += v;
        return t;
    }

    /** Cor da mistura (para o renderizador): média ponderada das essências, ou água. */
    public int color() {
        int t = total();
        if (t == 0) return 0x3F76E4;
        float r = 0, g = 0, b = 0;
        for (Map.Entry<Essence, Integer> e : stored.entrySet()) {
            float w = e.getValue() / (float) t;
            r += ((e.getKey().rgb >> 16) & 255) * w;
            g += ((e.getKey().rgb >> 8) & 255) * w;
            b += (e.getKey().rgb & 255) * w;
        }
        return ((int) r << 16) | ((int) g << 8) | (int) b;
    }

    public void fill() {
        water = true;
        sync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity be) {
        if (!be.water || level.getGameTime() % 5 != 0) return;
        if (!DistillerBlockEntity.heated(level, pos)) return;
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).deflate(0.1).setMaxY(pos.getY() + 1.0));
        for (ItemEntity ie : items) {
            if (!ie.isAlive() || ie.getItem().isEmpty()) continue;
            be.consume((ServerLevel) level, ie);
            return; // um item por vez
        }
    }

    private void consume(ServerLevel level, ItemEntity ie) {
        ItemStack stack = ie.getItem();
        Entity owner = ie.getOwner();
        ServerPlayer player = owner instanceof ServerPlayer sp ? sp : null;

        // 1) catalisador?
        boolean blocked = false;
        for (RecipeHolder<CrucibleRecipe> h : level.getRecipeManager().getAllRecipesFor(ModRecipes.CRUCIBLE_TYPE.get())) {
            CrucibleRecipe r = h.value();
            if (!r.catalyst.test(stack)) continue;
            if (player != null && !r.research.isEmpty() && !player.getData(ModAttachments.KNOWLEDGE).research.contains(r.research)) {
                if (!r.enough(stored)) continue;
                blocked = true;
                Research res = Research.get(r.research);
                player.displayClientMessage(Component.translatable("msg.primeva.need_research",
                        res != null ? res.title() : Component.literal(r.research)).withStyle(ChatFormatting.RED), true);
                continue;
            }
            if (!r.enough(stored) && !pullFromJars(r)) continue;
            r.essences.forEach((e, n) -> stored.put(e, get(e) - n));
            stored.values().removeIf(v -> v <= 0);
            stack.shrink(1);
            if (stack.isEmpty()) ie.discard();
            ItemEntity out = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.0, worldPosition.getZ() + 0.5, r.result.copy());
            out.setDeltaMovement(0, 0.3, 0);
            level.addFreshEntity(out);
            level.sendParticles(ParticleTypes.WITCH, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, 20, 0.2, 0.2, 0.2, 0.05);
            level.playSound(null, worldPosition, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1f, 1.4f);
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1f, 1f);
            if (level.random.nextInt(3) == 0) Miasma.add(level, worldPosition, 1);
            Discovery.near(level, worldPosition, "do:crucible");
            sync();
            return;
        }
        if (blocked) {
            eject(ie);
            return;
        }
        // 2) dissolver
        Map<Essence, Integer> ess = ModDataMaps.of(stack.getItem());
        if (ess.isEmpty()) {
            eject(ie);
            return;
        }
        int overflow = 0;
        for (Map.Entry<Essence, Integer> e : ess.entrySet()) {
            int add = e.getValue();
            int space = capacity() - total();
            int put = Math.min(space, add);
            if (put > 0) stored.put(e.getKey(), get(e.getKey()) + put);
            overflow += add - put;
        }
        if (overflow > 0) {
            Miasma.add(level, worldPosition, 1 + overflow / 4);
            level.sendParticles(ParticleTypes.SMOKE, worldPosition.getX() + 0.5, worldPosition.getY() + 1, worldPosition.getZ() + 0.5, 10, 0.2, 0.1, 0.2, 0.02);
        }
        stack.shrink(1);
        if (stack.isEmpty()) ie.discard();
        Essence dom = null;
        for (Map.Entry<Essence, Integer> e : ess.entrySet()) if (dom == null || e.getValue() > ess.get(dom)) dom = e.getKey();
        if (dom != null) level.sendParticles(new DustParticleOptions(dom.color(), 1.0f), worldPosition.getX() + 0.5, worldPosition.getY() + 0.9,
                worldPosition.getZ() + 0.5, 8, 0.2, 0.05, 0.2, 0);
        level.playSound(null, worldPosition, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.3f, 1.6f);
        Discovery.near(level, worldPosition, "do:crucible");
        sync();
    }

    /** Puxa dos jarros ligados por tubos só o que falta para a receita (tudo ou nada: nunca perde essência). */
    private boolean pullFromJars(CrucibleRecipe r) {
        Map<Essence, Integer> missing = new EnumMap<>(Essence.class);
        r.essences.forEach((e, n) -> {
            int lack = n - get(e);
            if (lack > 0) missing.put(e, lack);
        });
        Map<Essence, List<EssenceJarBlockEntity>> sources = new EnumMap<>(Essence.class);
        for (Map.Entry<Essence, Integer> m : missing.entrySet()) {
            List<EssenceJarBlockEntity> jars = EssenceNetwork.jars(level, worldPosition, m.getKey());
            int have = 0;
            for (EssenceJarBlockEntity j : jars) if (j.essence() == m.getKey()) have += j.amount();
            if (have < m.getValue()) return false;
            sources.put(m.getKey(), jars);
        }
        for (Map.Entry<Essence, Integer> m : missing.entrySet()) {
            int left = m.getValue();
            for (EssenceJarBlockEntity j : sources.get(m.getKey())) {
                if (left <= 0) break;
                if (j.essence() != m.getKey()) continue;
                int n = Math.min(left, j.amount());
                if (j.take(m.getKey(), n)) {
                    stored.put(m.getKey(), get(m.getKey()) + n);
                    left -= n;
                }
            }
        }
        return r.enough(stored);
    }

    private void eject(ItemEntity ie) {
        ie.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 1.1, worldPosition.getZ() + 0.5);
        ie.setDeltaMovement((level.random.nextDouble() - 0.5) * 0.3, 0.35, (level.random.nextDouble() - 0.5) * 0.3);
        ie.setPickUpDelay(20);
        level.playSound(null, worldPosition, SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.5f, 1.2f);
    }

    private int get(Essence e) {
        return stored.getOrDefault(e, 0);
    }

    public void dump(Player player) {
        int t = total();
        if (t > 0) Miasma.add(level, worldPosition, 1 + t / 10);
        stored.clear();
        water = false;
        level.playSound(null, worldPosition, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1f, 0.8f);
        player.displayClientMessage(Component.translatable("msg.primeva.crucible_dumped").withStyle(ChatFormatting.GRAY), true);
        sync();
    }

    public void report(Player player) {
        MutableComponent msg = Component.translatable("block.primeva.crucible").withStyle(ChatFormatting.GOLD).append(": ");
        if (!water) msg.append(Component.translatable("msg.primeva.crucible_no_water").withStyle(ChatFormatting.RED));
        else if (total() == 0) msg.append(Component.translatable("msg.primeva.empty").withStyle(ChatFormatting.GRAY));
        else msg.append(Essence.line(stored, "")).append(Component.literal("  (" + total() + "/" + capacity() + ")").withStyle(ChatFormatting.DARK_GRAY));
        if (!DistillerBlockEntity.heated(level, worldPosition)) msg.append(Component.translatable("msg.primeva.no_heat").withStyle(ChatFormatting.RED));
        player.displayClientMessage(msg, true);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("water", water);
        CompoundTag ess = new CompoundTag();
        stored.forEach((e, n) -> ess.putInt(e.id, n));
        tag.put("essences", ess);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        water = tag.getBoolean("water");
        stored.clear();
        CompoundTag ess = tag.getCompound("essences");
        for (Essence e : Essence.values()) if (ess.contains(e.id)) stored.put(e, ess.getInt(e.id));
    }
}
