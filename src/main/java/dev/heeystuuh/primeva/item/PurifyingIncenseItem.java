package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.Discovery;
import dev.heeystuuh.primeva.ModBlocks;
import dev.heeystuuh.primeva.block.CorruptedSoilBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.List;

/** Incenso Purificador: limpa o miasma do chunk e dos vizinhos e cura o solo corrompido num raio de 10 blocos. */
public class PurifyingIncenseItem extends Item {
    public PurifyingIncenseItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
        ItemStack stack = p.getItemInHand(hand);
        if (level instanceof ServerLevel sl) {
            BlockPos c = p.blockPosition();
            int cx = c.getX() >> 4, cz = c.getZ() >> 4;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (!sl.hasChunk(cx + dx, cz + dz)) continue;
                    LevelChunk ch = sl.getChunk(cx + dx, cz + dz);
                    dev.heeystuuh.primeva.Miasma.addToChunk(ch, dx == 0 && dz == 0 ? -40 : -20);
                }
            }
            int cleaned = 0;
            for (BlockPos q : BlockPos.betweenClosed(c.offset(-10, -4, -10), c.offset(10, 4, 10))) {
                if (sl.getBlockState(q).is(ModBlocks.CORRUPTED_SOIL.get())) {
                    CorruptedSoilBlock.purify(sl, q);
                    if (++cleaned >= 120) break;
                }
            }
            sl.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 80, 4, 1, 4, 0.02);
            sl.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, p.getX(), p.getY() + 0.5, p.getZ(), 20, 1.5, 0.2, 1.5, 0.01);
            sl.playSound(null, c, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1f, 1.6f);
            p.displayClientMessage(Component.translatable("msg.primeva.incense_used").withStyle(ChatFormatting.AQUA), true);
            if (p instanceof ServerPlayer sp) Discovery.grant(sp, "do:purify");
            if (!p.getAbilities().instabuild) stack.shrink(1);
            p.getCooldowns().addCooldown(this, 40);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
        t.add(Component.translatable("tooltip.primeva.purifying_incense").withStyle(ChatFormatting.GRAY));
    }
}
