package dev.heeystuuh.primeva;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Modificador de loot "primeva:add_table": adiciona uma tabela inteira a baús de estruturas. */
public final class ModLoot {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, PrimevaMod.MODID);

    public static class AddTable extends LootModifier {
        public static final MapCodec<AddTable> CODEC = RecordCodecBuilder.mapCodec(i -> codecStart(i)
                .and(ResourceLocation.CODEC.fieldOf("table").forGetter(m -> m.table)).apply(i, AddTable::new));
        private final ResourceLocation table;

        public AddTable(LootItemCondition[] conditions, ResourceLocation table) {
            super(conditions);
            this.table = table;
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
            ctx.getResolver().get(Registries.LOOT_TABLE, ResourceKey.create(Registries.LOOT_TABLE, table))
                    .ifPresent(h -> h.value().getRandomItemsRaw(ctx, loot::add));
            return loot;
        }

        @Override
        public MapCodec<? extends IGlobalLootModifier> codec() {
            return CODEC;
        }
    }

    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<AddTable>> ADD_TABLE =
            MODIFIERS.register("add_table", () -> AddTable.CODEC);

    private ModLoot() {}
}
