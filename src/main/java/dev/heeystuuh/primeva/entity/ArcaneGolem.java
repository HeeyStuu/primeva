package dev.heeystuuh.primeva.entity;

import dev.heeystuuh.primeva.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.HopperBlockEntity;

/** Golem Arcano: trabalha ao redor da sua "casa" (o bloco onde foi colocado) conforme o selo que recebeu. */
public class ArcaneGolem extends PathfinderMob {
    public enum Task { NONE, HARVEST, PICKUP, GUARD, LUMBER, STOCK }
    /** Núcleos: especializações instaladas no golem. */
    public enum Core { NONE, SPEED, REACH, CAPACITY, STRENGTH }

    private static final net.minecraft.resources.ResourceLocation CORE_SPEED = dev.heeystuuh.primeva.PrimevaMod.id("golem_core_speed");
    private static final net.minecraft.resources.ResourceLocation CORE_DMG = dev.heeystuuh.primeva.PrimevaMod.id("golem_core_damage");
    private static final net.minecraft.resources.ResourceLocation CORE_HP = dev.heeystuuh.primeva.PrimevaMod.id("golem_core_health");
    private Core core = Core.NONE;
    /** Filtro de itens (vazio = aceita tudo). Agachado + clique com um item adiciona/remove. */
    public final java.util.List<net.minecraft.world.item.Item> filter = new java.util.ArrayList<>();

    private static final EntityDataAccessor<Integer> TASK = SynchedEntityData.defineId(ArcaneGolem.class, EntityDataSerializers.INT);
    public static int range() {
        return dev.heeystuuh.primeva.Config.get(dev.heeystuuh.primeva.Config.GOLEM_RANGE);
    }

    private BlockPos home;
    public final SimpleContainer inv = new SimpleContainer(18);

    public Core core() {
        return core;
    }

    public int workRange() {
        return range() + (core == Core.REACH ? 6 : 0);
    }

    public int capacity() {
        return core == Core.CAPACITY ? 18 : 9;
    }

    public boolean allows(ItemStack s) {
        return filter.isEmpty() || filter.contains(s.getItem());
    }

    /** Guarda respeitando a capacidade; devolve o que não coube. */
    public ItemStack store(ItemStack stack) {
        ItemStack rest = stack.copy();
        for (int i = 0; i < capacity() && !rest.isEmpty(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) {
                inv.setItem(i, rest);
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameComponents(s, rest) && s.getCount() < s.getMaxStackSize()) {
                int n = Math.min(rest.getCount(), s.getMaxStackSize() - s.getCount());
                s.grow(n);
                rest.shrink(n);
            }
        }
        return rest;
    }

    public boolean full() {
        for (int i = 0; i < capacity(); i++) if (inv.getItem(i).isEmpty()) return false;
        return true;
    }

    private void applyCore() {
        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        var dmg = getAttribute(Attributes.ATTACK_DAMAGE);
        var hp = getAttribute(Attributes.MAX_HEALTH);
        if (speed != null) {
            speed.removeModifier(CORE_SPEED);
            if (core == Core.SPEED) speed.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(CORE_SPEED, 0.4,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
        if (dmg != null) {
            dmg.removeModifier(CORE_DMG);
            if (core == Core.STRENGTH) dmg.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(CORE_DMG, 6,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
        }
        if (hp != null) {
            hp.removeModifier(CORE_HP);
            if (core == Core.STRENGTH) hp.addPermanentModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(CORE_HP, 20,
                    net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
            if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
        }
        if (home != null) restrictTo(home, workRange() + 4);
    }

    public void setCore(Core c) {
        core = c;
        applyCore();
    }

    public ArcaneGolem(EntityType<? extends ArcaneGolem> type, Level level) {
        super(type, level);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.MOVEMENT_SPEED, 0.28)
                .add(Attributes.ATTACK_DAMAGE, 6).add(Attributes.KNOCKBACK_RESISTANCE, 0.6).add(Attributes.FOLLOW_RANGE, 24).add(Attributes.ARMOR, 6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder b) {
        super.defineSynchedData(b);
        b.define(TASK, 0);
    }

    public Task task() {
        return Task.values()[Math.floorMod(entityData.get(TASK), Task.values().length)];
    }

    public void setTask(Task t) {
        entityData.set(TASK, t.ordinal());
    }

    public BlockPos home() {
        return home;
    }

    public void setHome(BlockPos p) {
        home = p.immutable();
        restrictTo(home, workRange() + 4);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.15, true));
        goalSelector.addGoal(2, new GolemWorkGoal(this));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0));
        goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.7));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 6f));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Mob.class, 10, true, false,
                e -> e instanceof Enemy && task() == Task.GUARD && home != null && e.blockPosition().closerThan(home, workRange() + 2)));
    }

    /** Entrega o inventário no baú da casa (ou deixa no chão ao lado). */
    public void deposit() {
        if (home == null) return;
        Container c = HopperBlockEntity.getContainerAt(level(), home);
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty()) continue;
            ItemStack rest = c != null ? HopperBlockEntity.addItem(null, c, s, null) : s;
            if (!rest.isEmpty()) spawnAtLocation(rest, 0.5f);
            inv.setItem(i, ItemStack.EMPTY);
        }
        dev.heeystuuh.primeva.Discovery.near(level(), home, "do:golem_work");
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (level().isClientSide) return InteractionResult.SUCCESS;
        if (held.getItem() instanceof dev.heeystuuh.primeva.item.SealItem seal) {
            if (task() != Task.NONE) spawnAtLocation(new ItemStack(ModItems.seal(task())));
            setTask(seal.task);
            setTarget(null);
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.displayClientMessage(Component.translatable("msg.primeva.golem_task", Component.translatable("item.primeva.seal_" + seal.task.name().toLowerCase())).withStyle(ChatFormatting.AQUA), true);
            playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1f, 1.3f);
            return InteractionResult.CONSUME;
        }
        if (held.getItem() instanceof dev.heeystuuh.primeva.item.GolemCoreItem gc && gc.core != Core.NONE) {
            if (core != Core.NONE) spawnAtLocation(new ItemStack(ModItems.golemCore(core)));
            setCore(gc.core);
            if (!player.getAbilities().instabuild) held.shrink(1);
            player.displayClientMessage(Component.translatable("msg.primeva.golem_core",
                    Component.translatable("item.primeva.golem_core_" + gc.core.name().toLowerCase())).withStyle(ChatFormatting.AQUA), true);
            playSound(SoundEvents.BEACON_POWER_SELECT, 1f, 1.5f);
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown() && !held.isEmpty()) {
            net.minecraft.world.item.Item it = held.getItem();
            if (filter.contains(it)) filter.remove(it);
            else if (filter.size() < 9) filter.add(it);
            net.minecraft.network.chat.MutableComponent msg = Component.translatable("msg.primeva.golem_filter").withStyle(ChatFormatting.GOLD);
            if (filter.isEmpty()) msg.append(Component.translatable("msg.primeva.golem_filter_any").withStyle(ChatFormatting.GRAY));
            for (int i = 0; i < filter.size(); i++) msg.append(Component.literal(i == 0 ? " " : ", ")).append(filter.get(i).getDescription().copy().withStyle(ChatFormatting.WHITE));
            player.displayClientMessage(msg, true);
            playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.6f, 1.4f);
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown() && held.isEmpty()) {
            deposit();
            if (task() != Task.NONE) spawnAtLocation(new ItemStack(ModItems.seal(task())));
            if (core != Core.NONE) spawnAtLocation(new ItemStack(ModItems.golemCore(core)));
            ItemStack golem = new ItemStack(ModItems.ARCANE_GOLEM.get());
            if (!player.getInventory().add(golem)) spawnAtLocation(golem);
            playSound(SoundEvents.ITEM_PICKUP, 1f, 0.6f);
            discard();
            return InteractionResult.CONSUME;
        }
        Component t = task() == Task.NONE ? Component.translatable("msg.primeva.golem_idle")
                : Component.translatable("item.primeva.seal_" + task().name().toLowerCase());
        Component coreName = core == Core.NONE ? Component.translatable("msg.primeva.golem_no_core") : Component.translatable("item.primeva.golem_core_" + core.name().toLowerCase());
        player.displayClientMessage(Component.translatable("msg.primeva.golem_status", t, (int) getHealth(), (int) getMaxHealth()).withStyle(ChatFormatting.GRAY)
                .append(Component.literal(" | ")).append(coreName), true);
        return InteractionResult.CONSUME;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) return;
        if (tickCount % 40 == 0 && getHealth() < getMaxHealth()) heal(1f);
        if (tickCount % 5 == 0) {
            // mostra na mão o que está carregando
            ItemStack show = ItemStack.EMPTY;
            for (int i = 0; i < inv.getContainerSize(); i++) if (!inv.getItem(i).isEmpty()) { show = inv.getItem(i).copyWithCount(1); break; }
            if (!ItemStack.isSameItemSameComponents(show, getMainHandItem())) {
                setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, show);
                setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0f);
            }
        }
    }

    @Override
    public boolean removeWhenFarAway(double d) {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource src, boolean recent) {
        super.dropCustomDeathLoot(level, src, recent);
        for (int i = 0; i < inv.getContainerSize(); i++) if (!inv.getItem(i).isEmpty()) spawnAtLocation(inv.getItem(i));
        if (task() != Task.NONE) spawnAtLocation(new ItemStack(ModItems.seal(task())));
        if (core != Core.NONE) spawnAtLocation(new ItemStack(ModItems.golemCore(core)));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource src) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("task", task().ordinal());
        if (home != null) tag.put("home", NbtUtils.writeBlockPos(home));
        tag.put("inv", inv.createTag(registryAccess()));
        tag.putString("core", core.name());
        net.minecraft.nbt.ListTag fl = new net.minecraft.nbt.ListTag();
        for (net.minecraft.world.item.Item it : filter) fl.add(net.minecraft.nbt.StringTag.valueOf(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(it).toString()));
        tag.put("filter", fl);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setTask(Task.values()[Math.floorMod(tag.getInt("task"), Task.values().length)]);
        NbtUtils.readBlockPos(tag, "home").ifPresent(this::setHome);
        inv.fromTag(tag.getList("inv", 10), registryAccess());
        try {
            core = Core.valueOf(tag.getString("core"));
        } catch (Exception e) {
            core = Core.NONE;
        }
        applyCore();
        filter.clear();
        net.minecraft.nbt.ListTag fl = tag.getList("filter", 8);
        for (int i = 0; i < fl.size(); i++) {
            net.minecraft.resources.ResourceLocation rl = net.minecraft.resources.ResourceLocation.tryParse(fl.getString(i));
            if (rl != null && net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(rl)) filter.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.get(rl));
        }
    }
}
