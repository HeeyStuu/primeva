package dev.heeystuuh.primeva.network;

import dev.heeystuuh.primeva.ClientKnowledge;
import dev.heeystuuh.primeva.ModAttachments;
import dev.heeystuuh.primeva.Research;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class Network {
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("1");
        r.playToClient(SyncKnowledgePayload.TYPE, SyncKnowledgePayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> ClientKnowledge.update(payload.tag())));
        r.playToClient(ScanResultPayload.TYPE, ScanResultPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> dev.heeystuuh.primeva.ScanToast.show(payload.name(), payload.essences(), payload.isNew())));
        r.playToClient(MiasmaPayload.TYPE, MiasmaPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> dev.heeystuuh.primeva.ClientMiasma.level = payload.level()));
        r.playToServer(ResearchPayload.TYPE, ResearchPayload.CODEC,
                (payload, ctx) -> ctx.enqueueWork(() -> {
                    if (ctx.player() instanceof ServerPlayer sp) Research.tryResearch(sp, payload.id(), payload.placements());
                }));
    }

    public static void sync(ServerPlayer player) {
        Research.refreshGates(player);
        PacketDistributor.sendToPlayer(player,
                new SyncKnowledgePayload(player.getData(ModAttachments.KNOWLEDGE).serializeNBT(player.registryAccess())));
    }

    private Network() {}
}
