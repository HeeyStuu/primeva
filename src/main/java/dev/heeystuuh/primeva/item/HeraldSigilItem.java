package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.Config;
import dev.heeystuuh.primeva.ModEntities;
import dev.heeystuuh.primeva.entity.CrimsonHerald;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

/** Sigilo do Arauto: use no chão para invocar o Arauto Carmesim. */
public class HeraldSigilItem extends Item {
    public HeraldSigilItem(Properties p) {
        super(p);
    }

    @Override
    public InteractionResult useOn(UseOnContext ctx) {
        if (!(ctx.getLevel() instanceof ServerLevel sl)) return InteractionResult.SUCCESS;
        if (!Config.get(Config.HERALD_ENABLED)) {
            if (ctx.getPlayer() != null) ctx.getPlayer().displayClientMessage(Component.translatable("msg.primeva.herald_disabled").withStyle(ChatFormatting.GRAY), true);
            return InteractionResult.FAIL;
        }
        BlockPos at = ctx.getClickedPos().relative(ctx.getClickedFace());
        CrimsonHerald h = ModEntities.CRIMSON_HERALD.get().create(sl, null, at, MobSpawnType.MOB_SUMMONED, true, false);
        if (h == null) return InteractionResult.FAIL;
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(sl);
        if (bolt != null) {
            bolt.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
            bolt.setVisualOnly(true);
            sl.addFreshEntity(bolt);
        }
        sl.addFreshEntity(h);
        if (ctx.getPlayer() == null || !ctx.getPlayer().getAbilities().instabuild) ctx.getItemInHand().shrink(1);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
        t.add(Component.translatable("tooltip.primeva.herald_sigil").withStyle(ChatFormatting.DARK_RED));
    }
}
