package dev.heeystuuh.primeva;

import com.mojang.serialization.Codec;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

import java.util.EnumMap;
import java.util.Map;

/**
 * Essências de itens e criaturas, definidas em data/<ns>/data_maps/(item|entity_type)/essences.json.
 * Sincronizado com o cliente automaticamente. Modpacks/KubeJS podem adicionar valores.
 */
public final class ModDataMaps {
    private static final Codec<Map<String, Integer>> VALUE = Codec.unboundedMap(Codec.STRING, Codec.INT);

    public static final DataMapType<Item, Map<String, Integer>> ITEM_ESSENCES =
            DataMapType.builder(PrimevaMod.id("essences"), Registries.ITEM, VALUE).synced(VALUE, false).build();

    public static final DataMapType<EntityType<?>, Map<String, Integer>> ENTITY_ESSENCES =
            DataMapType.builder(PrimevaMod.id("essences"), Registries.ENTITY_TYPE, VALUE).synced(VALUE, false).build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(ITEM_ESSENCES);
        event.register(ENTITY_ESSENCES);
    }

    @SuppressWarnings("deprecation")
    public static Map<Essence, Integer> of(Item item) {
        return convert(item.builtInRegistryHolder().getData(ITEM_ESSENCES));
    }

    @SuppressWarnings("deprecation")
    public static Map<Essence, Integer> of(EntityType<?> type) {
        return convert(type.builtInRegistryHolder().getData(ENTITY_ESSENCES));
    }

    private static Map<Essence, Integer> convert(Map<String, Integer> raw) {
        Map<Essence, Integer> out = new EnumMap<>(Essence.class);
        if (raw == null) return out;
        raw.forEach((k, v) -> {
            Essence e = Essence.byId(k);
            if (e != null && v != null && v > 0) out.put(e, v);
        });
        return out;
    }

    private ModDataMaps() {}
}
