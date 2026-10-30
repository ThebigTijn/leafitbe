package dev.outfoxed.leafitbe.modules.leaves;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

// Breadth-first search from a leaf through connected leaves for a log. Vanilla only walks across faces; diagonal also walks across edges and corners.
public final class LeafSupport {
	public enum Kind { LOG, LEAF, OTHER }

	@FunctionalInterface
	public interface Blocks {
		Kind kind(int x, int y, int z);
	}

	private static final int[][] FACES = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
	private static final int[][] ALL = all();
	// Shared between searches: they only run on the main thread and never nest. Not Folia safe.
	private static final LongOpenHashSet SEEN = new LongOpenHashSet();
	private static final LongArrayFIFOQueue QUEUE = new LongArrayFIFOQueue();

	// True when a log is at most maxDistance steps away. Vanilla's distance counts the same way, and it decays at 7, so 6 matches it.
	public static boolean isSupported(Blocks blocks, int x, int y, int z, int maxDistance, boolean diagonal) {
		int[][] offsets = diagonal ? ALL : FACES;
		SEEN.clear();
		QUEUE.clear();
		long start = BlockPos.asLong(x, y, z);
		SEEN.add(start);
		QUEUE.enqueue(start);

		for (int distance = 1; distance <= maxDistance && !QUEUE.isEmpty(); distance++) {
			for (int i = QUEUE.size(); i > 0; i--) {
				long pos = QUEUE.dequeueLong();
				int px = BlockPos.getX(pos), py = BlockPos.getY(pos), pz = BlockPos.getZ(pos);
				for (int[] offset : offsets) {
					int nx = px + offset[0], ny = py + offset[1], nz = pz + offset[2];
					long next = BlockPos.asLong(nx, ny, nz);
					if (!SEEN.add(next)) continue;
					switch (blocks.kind(nx, ny, nz)) {
						case LOG -> { return true; }
						case LEAF -> QUEUE.enqueue(next);
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
