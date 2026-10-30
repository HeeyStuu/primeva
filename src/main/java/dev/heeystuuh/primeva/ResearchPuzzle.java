package dev.heeystuuh.primeva;

import java.util.*;

/**
 * Quebra-cabeça de pesquisa: um tabuleiro hexagonal com essências-âncora.
 * O jogador liga todas as âncoras colocando essências em casas vizinhas.
 * Duas essências só se ligam se forem iguais ou vizinhas no Ciclo Primordial:
 * Brasa - Lume - Vendaval - Maré - Terra - Umbra - (volta para Brasa).
 * O tabuleiro é o mesmo no cliente e no servidor (gerado pela pesquisa + jogador).
 */
public final class ResearchPuzzle {
    public static final int RADIUS = 3;
    public static final int PLACE_COST = 2;
    public static final Essence[] CYCLE = {Essence.EMBER, Essence.LUMEN, Essence.GALE, Essence.TIDE, Essence.TERRA, Essence.UMBRA};
    private static final int[][] DIRS = {{1, 0}, {1, -1}, {0, -1}, {-1, 0}, {-1, 1}, {0, 1}};

    /** Casas em coordenadas axiais (q, r). */
    public static final List<int[]> CELLS = new ArrayList<>();
    private static final Map<Long, Integer> INDEX = new HashMap<>();

    static {
        for (int q = -RADIUS; q <= RADIUS; q++) {
            for (int r = -RADIUS; r <= RADIUS; r++) {
                if (Math.abs(q + r) > RADIUS) continue;
                INDEX.put(key(q, r), CELLS.size());
                CELLS.add(new int[]{q, r});
            }
        }
    }

    private static long key(int q, int r) {
        return ((long) q << 32) ^ (r & 0xffffffffL);
    }

    public static int indexOf(int q, int r) {
        Integer i = INDEX.get(key(q, r));
        return i == null ? -1 : i;
    }

    public static int ring(int idx) {
        int[] c = CELLS.get(idx);
        return Math.max(Math.abs(c[0]), Math.max(Math.abs(c[1]), Math.abs(c[0] + c[1])));
    }

    public static List<Integer> neighbors(int idx) {
        int[] c = CELLS.get(idx);
        List<Integer> out = new ArrayList<>(6);
        for (int[] d : DIRS) {
            int n = indexOf(c[0] + d[0], c[1] + d[1]);
            if (n >= 0) out.add(n);
        }
        return out;
    }

    public static boolean compatible(Essence a, Essence b) {
        if (a == b) return true;
        int ia = 0, ib = 0;
        for (int i = 0; i < CYCLE.length; i++) {
            if (CYCLE[i] == a) ia = i;
            if (CYCLE[i] == b) ib = i;
        }
        int d = Math.abs(ia - ib);
        return d == 1 || d == CYCLE.length - 1;
    }

    public record Board(Map<Integer, Essence> anchors, Set<Integer> voids) {}

    public static Board generate(String researchId, UUID player, Collection<Essence> essences) {
        Random rnd = new Random(researchId.hashCode() * 31L + player.getLeastSignificantBits());
        List<Essence> ess = new ArrayList<>(essences);
        ess.sort(Comparator.comparingInt(Enum::ordinal));
        if (ess.isEmpty()) ess.add(Essence.LUMEN);
        while (ess.size() > 4) ess.remove(rnd.nextInt(ess.size()));
        if (ess.size() == 1) ess.add(ess.get(0));
        List<Integer> outer = new ArrayList<>();
        for (int i = 0; i < CELLS.size(); i++) if (ring(i) == RADIUS) outer.add(i);
        // âncoras espalhadas pelo anel de fora
        int start = rnd.nextInt(outer.size());
        Map<Integer, Essence> anchors = new LinkedHashMap<>();
        for (int i = 0; i < ess.size(); i++) {
            int idx = outer.get((start + i * outer.size() / ess.size()) % outer.size());
            anchors.put(idx, ess.get(i));
        }
        // casas vazias (bloqueadas), sem isolar o tabuleiro
        for (int attempt = 0; attempt < 20; attempt++) {
            Set<Integer> voids = new HashSet<>();
            int n = 3 + rnd.nextInt(3);
            while (voids.size() < n) {
                int idx = rnd.nextInt(CELLS.size());
                if (anchors.containsKey(idx) || ring(idx) == 0) continue;
                boolean nearAnchor = false;
                for (int nb : neighbors(idx)) if (anchors.containsKey(nb)) nearAnchor = true;
                if (!nearAnchor) voids.add(idx);
            }
            if (connected(voids)) return new Board(anchors, voids);
        }
        return new Board(anchors, Set.of());
    }

    private static boolean connected(Set<Integer> voids) {
        int first = -1;
        int free = 0;
        for (int i = 0; i < CELLS.size(); i++) if (!voids.contains(i)) { free++; if (first < 0) first = i; }
        Deque<Integer> q = new ArrayDeque<>(List.of(first));
        Set<Integer> seen = new HashSet<>(List.of(first));
        while (!q.isEmpty()) for (int n : neighbors(q.poll())) if (!voids.contains(n) && seen.add(n)) q.add(n);
        return seen.size() == free;
    }

    /** Todas as âncoras estão ligadas numa única rede? */
    public static boolean solved(Board b, Map<Integer, Essence> placed) {
        Map<Integer, Essence> all = new HashMap<>(placed);
        for (Integer v : b.voids()) all.remove(v);
        all.putAll(b.anchors());
        Integer first = b.anchors().keySet().iterator().next();
        Set<Integer> seen = connectedFrom(all, first);
        return seen.containsAll(b.anchors().keySet());
    }

    public static Set<Integer> connectedFrom(Map<Integer, Essence> all, int start) {
        Deque<Integer> q = new ArrayDeque<>(List.of(start));
        Set<Integer> seen = new HashSet<>(List.of(start));
        while (!q.isEmpty()) {
            int c = q.poll();
            for (int n : neighbors(c)) {
                if (seen.contains(n) || !all.containsKey(n)) continue;
                if (compatible(all.get(c), all.get(n))) {
                    seen.add(n);
                    q.add(n);
                }
            }
        }
        return seen;
    }

    /** Custo extra em percepção das essências colocadas. */
    public static Map<Essence, Integer> placementCost(Map<Integer, Essence> placed) {
        Map<Essence, Integer> out = new EnumMap<>(Essence.class);
        for (Essence e : placed.values()) out.merge(e, PLACE_COST, Integer::sum);
        return out;
    }

    public static List<Integer> encode(Map<Integer, Essence> placed) {
        List<Integer> out = new ArrayList<>();
        placed.forEach((i, e) -> out.add(i * 8 + e.ordinal()));
        return out;
    }

    public static Map<Integer, Essence> decode(List<Integer> data) {
        Map<Integer, Essence> out = new HashMap<>();
        for (int v : data) {
            int idx = v / 8, e = v % 8;
            if (idx >= 0 && idx < CELLS.size() && e < Essence.values().length) out.put(idx, Essence.values()[e]);
        }
        return out;
    }

    private ResearchPuzzle() {}
}
