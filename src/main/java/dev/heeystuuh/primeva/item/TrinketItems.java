package dev.heeystuuh.primeva.item;

import dev.heeystuuh.primeva.*;
import dev.heeystuuh.primeva.network.Network;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public final class TrinketItems {

    /** Espelho de Bolso: agachado num baú para vincular; depois envia o item da outra mão para ele, de qualquer lugar. */
    public static class Mirror extends Item {
        public Mirror(Properties p) { super(p); }

        @Override
        public InteractionResult useOn(UseOnContext ctx) {
            Player p = ctx.getPlayer();
            if (p == null || !p.isShiftKeyDown()) return InteractionResult.PASS;
            Level level = ctx.getLevel();
            if (HopperBlockEntity.getContainerAt(level, ctx.getClickedPos()) == null) return InteractionResult.PASS;
            if (!level.isClientSide) {
                CompoundTag tag = new CompoundTag();
                tag.put("pos", NbtUtils.writeBlockPos(ctx.getClickedPos()));
                tag.putString("dim", level.dimension().location().toString());
                ctx.getItemInHand().set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                p.displayClientMessage(Component.translatable("msg.primeva.mirror_bound").withStyle(ChatFormatting.AQUA), true);
                level.playSound(null, ctx.getClickedPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1f, 1.4f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
            ItemStack mirror = p.getItemInHand(hand);
            if (!(level instanceof ServerLevel sl) || p.isShiftKeyDown()) return InteractionResultHolder.pass(mirror);
            CustomData data = mirror.get(DataComponents.CUSTOM_DATA);
            if (data == null) {
                p.displayClientMessage(Component.translatable("msg.primeva.mirror_unbound").withStyle(ChatFormatting.GRAY), true);
                return InteractionResultHolder.fail(mirror);
            }
            CompoundTag tag = data.copyTag();
            BlockPos pos = NbtUtils.readBlockPos(tag, "pos").orElse(null);
            ServerLevel target = sl.getServer().getLevel(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dim"))));
            InteractionHand other = hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack send = p.getItemInHand(other);
            if (pos == null || target == null || send.isEmpty()) return InteractionResultHolder.fail(mirror);
            Container c = target.isLoaded(pos) ? HopperBlockEntity.getContainerAt(target, pos) : null;
            if (c == null) {
                p.displayClientMessage(Component.translatable("msg.primeva.mirror_lost").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(mirror);
            }
            ItemStack rest = HopperBlockEntity.addItem(null, c, send.copy(), null);
            p.setItemInHand(other, rest);
            sl.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1, p.getZ(), 20, 0.3, 0.4, 0.3, 0.05);
            sl.playSound(null, p.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.6f, 1.6f);
            p.getCooldowns().addCooldown(this, 10);
            return InteractionResultHolder.success(mirror);
        }

        @Override
        public boolean isFoil(ItemStack stack) {
            return stack.has(DataComponents.CUSTOM_DATA);
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.pocket_mirror").withStyle(ChatFormatting.GRAY));
            CustomData d = s.get(DataComponents.CUSTOM_DATA);
            if (d != null) NbtUtils.readBlockPos(d.copyTag(), "pos").ifPresent(bp ->
                    t.add(Component.literal(bp.getX() + ", " + bp.getY() + ", " + bp.getZ()).withStyle(ChatFormatting.DARK_AQUA)));
        }
    }

    /** Gancho Arcano: puxa você até o bloco mirado (32 blocos). */
    public static class Grapple extends Item {
        public Grapple(Properties p) { super(p.durability(300)); }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
            ItemStack stack = p.getItemInHand(hand);
            HitResult hit = p.pick(32, 0f, false);
            if (!(hit instanceof BlockHitResult) || hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.fail(stack);
            Vec3 to = hit.getLocation().subtract(p.position());
            double dist = to.length();
            Vec3 v = to.normalize().scale(Math.min(2.6, 0.6 + dist * 0.12));
            p.setDeltaMovement(v.x, v.y + 0.35, v.z);
            p.fallDistance = 0;
            p.hurtMarked = true;
            if (level instanceof ServerLevel sl) {
                Vec3 a = p.getEyePosition();
                for (int i = 0; i <= 20; i++) {
                    Vec3 q = a.add(hit.getLocation().subtract(a).scale(i / 20.0));
                    sl.sendParticles(ParticleTypes.CRIT, q.x, q.y, q.z, 1, 0, 0, 0, 0);
                }
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, false, false));
                sl.playSound(null, p.blockPosition(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1f, 1.4f);
                stack.hurtAndBreak(1, p, LivingEntity.getSlotForHand(hand));
            }
            p.getCooldowns().addCooldown(this, 25);
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva.grapple").withStyle(ChatFormatting.GRAY));
        }
    }

    /** Itens que se leem/consomem para ganhar percepção (curiosidades, Ritos Carmesins). */
    public static class Insight extends Item {
        private final Map<Essence, Integer> gain;
        private final String key;

        public Insight(String key, Map<Essence, Integer> gain, Properties p) {
            super(p);
            this.key = key;
            this.gain = gain;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
            ItemStack stack = p.getItemInHand(hand);
            if (p instanceof ServerPlayer sp) {
                Knowledge k = sp.getData(ModAttachments.KNOWLEDGE);
                gain.forEach(k::add);
                if (key.equals("crimson_rites")) Warp.addTemp(sp, 4);
                Network.sync(sp);
                sp.displayClientMessage(Component.translatable("msg.primeva.insight_gained").withStyle(ChatFormatting.LIGHT_PURPLE)
                        .append(" ").append(Essence.line(gain, "+")), false);
                level.playSound(null, p.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1f, 0.8f);
                ((ServerLevel) level).sendParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1.5, p.getZ(), 40, 0.4, 0.4, 0.4, 0.5);
                if (!p.getAbilities().instabuild) stack.shrink(1);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        @Override
        public void appendHoverText(ItemStack s, TooltipContext c, List<Component> t, TooltipFlag f) {
            t.add(Component.translatable("tooltip.primeva." + key).withStyle(ChatFormatting.GRAY));
            t.add(Essence.line(gain, "+"));
        }
    }

    /** Sacola de Espólio: abra para sortear itens. */
    public static class LootBag extends Item {
        private final String tier;

        public LootBag(String tier, Properties p) {
            super(p);
            this.tier = tier;
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player p, InteractionHand hand) {
            ItemStack stack = p.getItemInHand(hand);
            if (level instanceof ServerLevel sl) {
                LootTable table = sl.getServer().reloadableRegistries().getLootTable(
                        ResourceKey.create(Registries.LOOT_TABLE, PrimevaMod.id("loot_bag/" + tier)));
                LootParams params = new LootParams.Builder(sl).withParameter(LootContextParams.ORIGIN, p.position())
                        .withParameter(LootContextParams.THIS_ENTITY, p).withLuck(p.getLuck()).create(LootContextParamSets.GIFT);
                for (ItemStack out : table.getRandomItems(params)) {
                    if (!p.getInventory().add(out)) p.drop(out, false);
                }
                sl.playSound(null, p.blockPosition(), SoundEvents.BUNDLE_DROP_CONTENTS, SoundSource.PLAYERS, 1f, 1f);
                if (!p.getAbilities().instabuild) stack.shrink(1);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    }

    private TrinketItems() {}
}
