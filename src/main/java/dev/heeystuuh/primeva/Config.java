package dev.heeystuuh.primeva;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuração do Primeva (serverconfig/primeva-server.toml dentro de cada mundo).
 * Valores de servidor são sincronizados com os jogadores.
 */
public final class Config {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.DoubleValue RESEARCH_COST, MIASMA, CULTIST_SPAWN, AMULET_CHANCE;
    public static final ModConfigSpec.IntValue GOLEM_RANGE, VEIN_LIMIT, TREE_LIMIT, RUNIC_HEARTS, CRUCIBLE_CAPACITY, LAMP_RADIUS, JAR_CAPACITY, SEXTANT_COOLDOWN, WARD_RADIUS, GROWTH_RADIUS;
    public static final ModConfigSpec.BooleanValue HERALD_ENABLED;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.comment("Progressao").push("progression");
        RESEARCH_COST = b.comment("Multiplicador do custo das pesquisas do Grimorio (1.0 = normal, 0.5 = metade, 2.0 = dobro)")
                .defineInRange("researchCostMultiplier", 1.0, 0.1, 10.0);
        MIASMA = b.comment("Multiplicador do miasma gerado por destilacao, cadinho e rituais (0 = desliga o miasma)")
                .defineInRange("miasmaMultiplier", 1.0, 0.0, 5.0);
        b.pop();
        b.comment("Criaturas").push("mobs");
        CULTIST_SPAWN = b.comment("Chance (0 a 1) de um Cultista Carmesim realmente aparecer quando o jogo tenta gera-lo (0 = desliga)")
                .defineInRange("cultistSpawnChance", 1.0, 0.0, 1.0);
        HERALD_ENABLED = b.comment("Permite invocar o chefe Arauto Carmesim com o Sigilo do Arauto")
                .define("heraldEnabled", true);
        GOLEM_RANGE = b.comment("Raio de trabalho dos Golens Arcanos ao redor da casa")
                .defineInRange("golemRange", 10, 4, 32);
        b.pop();
        b.comment("Itens e blocos").push("items");
        VEIN_LIMIT = b.comment("Maximo de blocos extras minerados de uma vez pela Picareta da Brasa")
                .defineInRange("veinMineLimit", 32, 0, 256);
        TREE_LIMIT = b.comment("Maximo de troncos extras derrubados de uma vez pelo Machado da Mare")
                .defineInRange("treeFellLimit", 128, 0, 512);
        RUNIC_HEARTS = b.comment("Coracoes do escudo do Anel Runico")
                .defineInRange("runicShieldHearts", 4, 1, 10);
        AMULET_CHANCE = b.comment("Chance (0 a 1) do Amuleto da Essencia economizar a carga de varinhas e focos")
                .defineInRange("essenceAmuletChance", 0.3, 0.0, 1.0);
        CRUCIBLE_CAPACITY = b.comment("Quantidade maxima de essencia no Cadinho Arcano")
                .defineInRange("crucibleCapacity", 100, 20, 1000);
        JAR_CAPACITY = b.comment("Quantidade maxima de essencia num Jarro de Essencia")
                .defineInRange("jarCapacity", 256, 16, 4096);
        LAMP_RADIUS = b.comment("Raio iluminado pela Lampada Arcana")
                .defineInRange("arcaneLampRadius", 14, 4, 32);
        SEXTANT_COOLDOWN = b.comment("Espera (em ticks) entre usos do Sextante Astral")
                .defineInRange("sextantCooldown", 2400, 20, 72000);
        WARD_RADIUS = b.comment("Raio em que a Pedra de Proteção impede monstros de nascer")
                .defineInRange("wardingRadius", 24, 4, 64);
        GROWTH_RADIUS = b.comment("Raio das Lâmpadas do Crescimento e da Fertilidade")
                .defineInRange("growthLampRadius", 6, 2, 16);
        b.pop();
        SPEC = b.build();
    }

    public static double get(ModConfigSpec.DoubleValue v) {
        try {
            return v.get();
        } catch (Exception e) {
            return v.getDefault();
        }
    }

    public static int get(ModConfigSpec.IntValue v) {
        try {
            return v.get();
        } catch (Exception e) {
            return v.getDefault();
        }
    }

    public static boolean get(ModConfigSpec.BooleanValue v) {
        try {
            return v.get();
        } catch (Exception e) {
            return v.getDefault();
        }
    }

    private Config() {}
}
