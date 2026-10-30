package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.ModEntities;
import dev.heeystuuh.primeva.entity.ArcaneGolem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

/** Golem Arcano (item): use num baú/bloco para criar o golem. Esse bloco vira a casa dele. */
public class GolemItem extends Item {
    public GolemItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel sl)) return InteractionResult.SUCCESS;
        BlockPos home = ctx.getClickedPos();
        BlockPos at = home.relative(ctx.getClickedFace());
        ArcaneGolem g = ModEntities.ARCANE_GOLEM.get().create(sl, null, at, MobSpawnType.SPAWN_EGG, true, false);
        if (g == null) return InteractionResult.FAIL;
        g.setHome(home);
        sl.addFreshEntity(g);
        if (ctx.getPlayer() instanceof net.minecraft.server.level.ServerPlayer sp) dev.heeystuuh.primeva.Discovery.grant(sp, "do:golem");
        sl.playSound(null, at, SoundEvents.IRON_GOLEM_REPAIR, SoundSource.NEUTRAL, 1f, 1.3f);
        if (ctx.getPlayer() == null || !ctx.getPlayer().getAbilities().instabuild) ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
        t.add(Component.translatable("tooltip.primeva.golem_1").withStyle(ChatFormatting.GRAY));
        t.add(Component.translatable("tooltip.primeva.golem_2").withStyle(ChatFormatting.GRAY));
        t.add(Component.translatable("tooltip.primeva.golem_3").withStyle(ChatFormatting.DARK_GRAY));
    }
}
