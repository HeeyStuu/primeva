package dev.heeystuuh.primeva;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Buracos do Foco do Vazio Portátil: os blocos somem por 10 s e depois voltam. */
public final class PortableHoles {
    private record Hole(ResourceKey<Level> dim, BlockPos pos, BlockState state, long until) {}

    private static final List<Hole> HOLES = new ArrayList<>();
    public static final int DURATION = 200;

    public static boolean open(ServerLevel sl, ServerPlayer p, BlockPos pos) {
        BlockState st = sl.getBlockState(pos);
        if (st.isAir() || st.hasBlockEntity() || st.getDestroySpeed(sl, pos) < 0 || !st.getFluidState().isEmpty()) return false;
        if (st.is(Blocks.BEDROCK) || !sl.mayInteract(p, pos)) return false;
        for (Hole h : HOLES) if (h.pos.equals(pos) && h.dim == sl.dimension()) return false;
        HOLES.add(new Hole(sl.dimension(), pos.immutable(), st, sl.getGameTime() + DURATION));
        sl.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        return true;
    }

    private static void restore(MinecraftServer server, Hole h) {
        ServerLevel sl = server.getLevel(h.dim);
        if (sl == null) return;
        BlockState now = sl.getBlockState(h.pos);
        if (now.isAir() || now.canBeReplaced()) sl.setBlockAndUpdate(h.pos, h.state);
        else Block.dropResources(h.state, sl, h.pos);
    }

    @SubscribeEvent
    public static void tick(ServerTickEvent.Post e) {
        if (HOLES.isEmpty()) return;
        MinecraftServer server = e.getServer();
        long now = server.overworld().getGameTime();
        Iterator<Hole> it = HOLES.iterator();
        while (it.hasNext()) {
            Hole h = it.next();
            if (now >= h.until) {
                restore(server, h);
                it.remove();
            }
        }
    }

    @SubscribeEvent
    public static void stopping(ServerStoppingEvent e) {
        for (Hole h : HOLES) restore(e.getServer(), h);
        HOLES.clear();
    }

    private PortableHoles() {}
}
