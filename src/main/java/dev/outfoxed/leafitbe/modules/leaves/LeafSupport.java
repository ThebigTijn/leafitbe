package dev.outfoxed.leafitbe.modules.leaves;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Breadth-first search from a leaf through connected leaves for a log. Vanilla only walks across faces; diagonal also walks across edges and corners.
public final class LeafSupport {
	public enum Kind { LOG, LEAF, OTHER }

	@FunctionalInterface
	public interface Blocks {
		Kind kind(int x, int y, int z);
	}

	private record Pos(int x, int y, int z) {}

	private static final int[][] FACES = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
	private static final int[][] ALL = all();

	// True when a log is at most maxDistance steps away. Vanilla's distance counts the same way, and it decays at 7, so 6 matches it.
	public static boolean isSupported(Blocks blocks, int x, int y, int z, int maxDistance, boolean diagonal) {
		int[][] offsets = diagonal ? ALL : FACES;
		Pos start = new Pos(x, y, z);
		Set<Pos> seen = new HashSet<>();
		ArrayDeque<Pos> queue = new ArrayDeque<>();
		seen.add(start);
		queue.add(start);

		for (int distance = 1; distance <= maxDistance && !queue.isEmpty(); distance++) {
			for (int i = queue.size(); i > 0; i--) {
				Pos pos = queue.poll();
				for (int[] offset : offsets) {
					Pos next = new Pos(pos.x + offset[0], pos.y + offset[1], pos.z + offset[2]);
					if (!seen.add(next)) continue;
					switch (blocks.kind(next.x, next.y, next.z)) {
						case LOG -> { return true; }
						case LEAF -> queue.add(next);
						case OTHER -> {}
					}
				}
			}
		}
		return false;
	}

	private static int[][] all() {
		List<int[]> offsets = new ArrayList<>();
		for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int dz = -1; dz <= 1; dz++) {
			if (dx != 0 || dy != 0 || dz != 0) offsets.add(new int[]{dx, dy, dz});
		}
		return offsets.toArray(int[][]::new);
	}
}
