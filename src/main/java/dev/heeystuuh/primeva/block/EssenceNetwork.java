package dev.heeystuuh.primeva.block;

import dev.heeystuuh.primeva.Essence;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/** Rede de essência: busca os jarros ligados a um bloco pelos tubos, respeitando os tubos filtrantes. */
public final class EssenceNetwork {
    public static final int MAX_NODES = 128;

    /** Jarros alcançáveis para uma essência (tubos filtrantes de outra cor bloqueiam o caminho). */
    public static List<EssenceJarBlockEntity> jars(Level level, BlockPos origin, Essence e) {
        List<EssenceJarBlockEntity> out = new ArrayList<>();
        Deque<BlockPos> q = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        seen.add(origin);
        for (Direction d : Direction.values()) q.add(origin.relative(d));
        int steps = 0;
        while (!q.isEmpty() && steps++ < MAX_NODES) {
            BlockPos p = q.poll();
            if (!seen.add(p) || !level.isLoaded(p)) continue;
            if (level.getBlockEntity(p) instanceof EssenceJarBlockEntity jar) {
                out.add(jar);
                continue;
            }
            BlockState st = level.getBlockState(p);
            if (st.getBlock() instanceof EssenceTubeBlock) {
                if (st.hasProperty(EssenceFilterTubeBlock.FILTER)) {
                    int f = st.getValue(EssenceFilterTubeBlock.FILTER);
                    if (f != 0 && f - 1 != e.ordinal()) continue;
                }
                for (Direction d : Direction.values()) q.add(p.relative(d));
            }
        }
        return out;
    }

    /** Empurra até 'max' de cada essência guardada para os jarros. */
    public static boolean push(Level level, BlockPos origin, Map<Essence, Integer> stored, int max) {
        if (stored.isEmpty()) return false;
        boolean moved = false;
        for (Essence e : Essence.values()) {
            int have = stored.getOrDefault(e, 0);
            if (have <= 0) continue;
            List<EssenceJarBlockEntity> jars = jars(level, origin, e);
            if (jars.isEmpty()) continue;
            int budget = Math.min(have, max);
            jars.sort(Comparator.comparingInt(j -> j.essence() == e ? 0 : j.essence() == null ? 1 : 2));
            for (EssenceJarBlockEntity j : jars) {
                if (budget <= 0) break;
                int put = j.insert(e, budget);
                if (put > 0) {
                    budget -= put;
                    stored.put(e, stored.get(e) - put);
                    moved = true;
                    if (level instanceof ServerLevel sl) {
                        BlockPos jp = j.getBlockPos();
                        sl.sendParticles(new DustParticleOptions(e.color(), 0.7f), jp.getX() + 0.5, jp.getY() + 0.9, jp.getZ() + 0.5, 3, 0.15, 0.05, 0.15, 0);
                    }
                }
            }
        }
        stored.values().removeIf(v -> v <= 0);
        return moved;
    }

    private EssenceNetwork() {}
}
