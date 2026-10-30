package dev.heeystuuh.primeva;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Miasma: resíduo mágico que se acumula no chunk ao destilar e infundir.
 * Em excesso corrompe o solo, causa fome/fraqueza e atrai espíritos hostis.
 * O Farol Purificador limpa a área.
 */
public final class Miasma {

    public static int get(Level level, BlockPos pos) {
        return level.getChunkAt(pos).getData(ModAttachments.MIASMA);
    }

    public static void add(Level level, BlockPos pos, int amount) {
        if (amount > 0) {
            double scaled = amount * Config.get(Config.MIASMA);
            amount = (int) scaled + (level.random.nextDouble() < scaled - (int) scaled ? 1 : 0);
            if (amount <= 0) return;
        }
        LevelChunk chunk = level.getChunkAt(pos);
        addToChunk(chunk, amount);
    }

    /** Soma sem o multiplicador da configuração (focos naturais). */
    public static void addRaw(Level level, BlockPos pos, int amount) {
        addToChunk(level.getChunkAt(pos), amount);
    }

    public static void addToChunk(LevelChunk chunk, int amount) {
        int now = chunk.getData(ModAttachments.MIASMA);
        int next = Math.max(0, now + amount);
        if (next != now) {
            chunk.setData(ModAttachments.MIASMA, next);
            chunk.setUnsaved(true);
        }
    }

    public static Component describe(int m) {
        String key = m == 0 ? "none" : m < 20 ? "low" : m < 40 ? "moderate" : m < 70 ? "high" : "critical";
        ChatFormatting color = m == 0 ? ChatFormatting.GREEN : m < 20 ? ChatFormatting.YELLOW
                : m < 40 ? ChatFormatting.GOLD : m < 70 ? ChatFormatting.RED : ChatFormatting.DARK_PURPLE;
        return Component.translatable("msg.primeva.miasma", m, Component.translatable("miasma.primeva." + key)).withStyle(color);
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        if (level.getGameTime() % 100 != 0) return;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            affect(level, player);
            // a névoa do cliente acompanha o miasma do lugar
            int m = get(level, player.blockPosition());
            net.minecraft.nbt.CompoundTag pd = player.getPersistentData();
            if (pd.getInt("primeva_last_miasma") != m) {
                pd.putInt("primeva_last_miasma", m);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new dev.heeystuuh.primeva.network.MiasmaPayload(m));
            }
        }
    }

    private static void affect(ServerLevel level, ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        int m = get(level, pos);
        if (m <= 0) return;
        RandomSource r = level.random;

        // Pouco miasma se dissipa sozinho, devagar
        if (m < 20 && r.nextInt(12) == 0) add(level, pos, -1);

        if (m >= 15 && r.nextInt(100) < m) corruptNear(level, pos, r);
        if (m >= 25 && r.nextInt(100) < m) witherPlants(level, pos, r);
        if (CommonEvents.miasmaImmune(player)) return;

        if (m >= 40 && r.nextInt(100) < m / 2) {
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 200, 0));
            player.displayClientMessage(Component.translatable("msg.primeva.miasma_heavy").withStyle(ChatFormatting.DARK_PURPLE), true);
        }
        if (m >= 70 && r.nextInt(100) < m / 3) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
        }
        if (m >= 55 && r.nextInt(100) < (m - 45)) spawnWraith(level, pos, r);
        // Maré Miasmática: evento raro em áreas muito contaminadas (no máximo a cada 2 minutos por jogador)
        long now = level.getGameTime();
        if (m >= 80 && now - player.getPersistentData().getLong("primeva_surge") > 2400 && r.nextInt(4) == 0) {
            player.getPersistentData().putLong("primeva_surge", now);
            player.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 160, 0));
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.SCULK_SHRIEKER_SHRIEK, net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 0.6f);
            player.displayClientMessage(Component.translatable("msg.primeva.miasma_surge").withStyle(ChatFormatting.DARK_PURPLE), false);
            for (int i = 0; i < 2; i++) spawnWraith(level, pos, r);
        }
    }

    public static void corruptNear(ServerLevel level, BlockPos center, RandomSource r) {
        int x = center.getX() + r.nextInt(17) - 8;
        int z = center.getZ() + r.nextInt(17) - 8;
        BlockPos top = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, new BlockPos(x, 0, z)).below();
        BlockState state = level.getBlockState(top);
        if (state.is(BlockTags.DIRT) && !state.is(ModBlocks.CORRUPTED_SOIL.get())) {
            level.setBlockAndUpdate(top, ModBlocks.CORRUPTED_SOIL.get().defaultBlockState());
        }
    }

    /** Plantas morrem perto do jogador em áreas contaminadas (nunca blocos de construção). */
    public static void witherPlants(ServerLevel level, BlockPos center, RandomSource r) {
        int x = center.getX() + r.nextInt(17) - 8;
        int z = center.getZ() + r.nextInt(17) - 8;
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        BlockState st = level.getBlockState(top);
        if (st.is(net.minecraft.world.level.block.Blocks.SHORT_GRASS) || st.is(net.minecraft.tags.BlockTags.SMALL_FLOWERS)) {
            level.setBlockAndUpdate(top, net.minecraft.world.level.block.Blocks.DEAD_BUSH.defaultBlockState());
        } else if (level.getBlockState(top.below()).is(net.minecraft.world.level.block.Blocks.GRASS_BLOCK) && r.nextInt(3) == 0) {
            level.setBlockAndUpdate(top.below(), net.minecraft.world.level.block.Blocks.COARSE_DIRT.defaultBlockState());
        }
    }

    public static void spawnWraith(ServerLevel level, BlockPos near, RandomSource r) {
        if (level.getEntitiesOfClass(dev.heeystuuh.primeva.entity.MiasmaWraith.class, new net.minecraft.world.phys.AABB(near).inflate(24)).size() >= 4) return;
        dev.heeystuuh.primeva.entity.MiasmaWraith w = ModEntities.MIASMA_WRAITH.get().create(level);
        if (w == null) return;
        w.moveTo(near.getX() + r.nextInt(13) - 6 + 0.5, near.getY() + 3, near.getZ() + r.nextInt(13) - 6 + 0.5, 0, 0);
        level.addFreshEntity(w);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SCULK_SOUL, w.getX(), w.getY(), w.getZ(), 10, 0.3, 0.3, 0.3, 0.02);
    }

    public static void spawnVex(ServerLevel level, BlockPos near, RandomSource r) {
        Vex vex = EntityType.VEX.create(level);
        if (vex == null) return;
        vex.moveTo(near.getX() + r.nextInt(9) - 4 + 0.5, near.getY() + 3, near.getZ() + r.nextInt(9) - 4 + 0.5, 0, 0);
        vex.setLimitedLife(20 * 30);
        level.addFreshEntity(vex);
    }

    private Miasma() {}
}
