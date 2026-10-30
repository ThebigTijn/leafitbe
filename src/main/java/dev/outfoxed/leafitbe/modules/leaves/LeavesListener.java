package dev.outfoxed.leafitbe.modules.leaves;

import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
public class LeavesListener implements Listener {
	private final LeavesModule module;
	private final Set<Pending> pending = new HashSet<>();
	// Leaves vanilla wants gone but our rules keep (diagonal, longer reach) come back on every random tick, so the search is skipped for a while.
	// Fast decay still checks them straight away when something nearby breaks.
	// Per world, packed position to the time the leaf may be searched again.
	private final Map<ServerLevel, Long2LongOpenHashMap> supported = new WeakHashMap<>();
	// ponytail: cleared whole when full; a few searches get redone then.
	private static final int SUPPORTED_MAX = 500_000;
	// Reused by kind() on the main thread so a search doesn't allocate a BlockPos per block.
	private static final BlockPos.MutableBlockPos CURSOR = new BlockPos.MutableBlockPos();
	// Set while we call our own LeavesDecayEvent, so we don't handle it a second time.
	private boolean firing;

	private record Pending(ServerLevel level, BlockPos pos) {}

	// Vanilla fires this from a random tick once a leaf is 7 face-steps from a log. We always cancel it and decide with our own rules.
	@EventHandler(priority = EventPriority.LOWEST)
	public void onLeavesDecay(LeavesDecayEvent event) {
		if (firing) return;
		event.setCancelled(true);
		CraftBlock block = (CraftBlock) event.getBlock();
		ServerLevel level = block.getCraftWorld().getHandle();
		BlockPos pos = block.getPosition();
		Long2LongOpenHashMap cache = supported.computeIfAbsent(level, l -> new Long2LongOpenHashMap());
		long now = System.currentTimeMillis();
		if (cache.get(pos.asLong()) > now) return;
		if (!check(level, pos)) return;
		if (cache.size() >= SUPPORTED_MAX) cache.clear();
		cache.put(pos.asLong(), now + module.getSettings().supportedCacheMs());
	}

	// Fast decay: removing a log or leaf queues its neighbours, and every leaf that decays queues its own, so a whole canopy goes in a few seconds.
	// ponytail: pistons, /setblock and other plugins don't trigger this; vanilla random ticks still catch those leaves.
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBreak(BlockBreakEvent event) {
		removed(List.of(event.getBlock()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBurn(BlockBurnEvent event) {
		removed(List.of(event.getBlock()));
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBlockExplode(BlockExplodeEvent event) {
		removed(event.blockList());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityExplode(EntityExplodeEvent event) {
		removed(event.blockList());
	}

	private void removed(List<org.bukkit.block.Block> blocks) {
		LeafSettings settings = module.getSettings();
		if (!settings.enabled() || !settings.fast()) return;
		for (org.bukkit.block.Block block : blocks) {
			CraftBlock craftBlock = (CraftBlock) block;
			queueNeighbours(craftBlock.getCraftWorld().getHandle(), craftBlock.getPosition(), settings);
		}
	}

	private void queueNeighbours(ServerLevel level, BlockPos center, LeafSettings settings) {
		for (BlockPos pos : BlockPos.betweenClosed(center.offset(-1, -1, -1), center.offset(1, 1, 1))) {
			BlockState state = level.getBlockStateIfLoaded(pos);
			if (state == null || !(state.getBlock() instanceof LeavesBlock)) continue;

			Pending entry = new Pending(level, pos.immutable());
			if (!pending.add(entry)) continue;
			int delay = ThreadLocalRandom.current().nextInt(settings.minDelay(), settings.maxDelay() + 1);
			Bukkit.getScheduler().runTaskLater(module.getPlugin(), () -> {
				pending.remove(entry);
				check(entry.level(), entry.pos());
			}, delay);
		}
	}

	// True when the leaf stays because a log holds it up.
	private boolean check(ServerLevel level, BlockPos pos) {
		LeafSettings settings = module.getSettings();
		BlockState state = level.getBlockStateIfLoaded(pos);
		if (!settings.enabled() || state == null || !(state.getBlock() instanceof LeavesBlock) || state.getValue(LeavesBlock.PERSISTENT)) return false;
		if (LeafSupport.isSupported((x, y, z) -> kind(level, x, y, z), pos.getX(), pos.getY(), pos.getZ(), settings.maxDistance(), settings.diagonal())) return true;

		// Other plugins (claims, protection) still get a say through the normal event.
		firing = true;
		boolean allowed;
		try {
			allowed = new LeavesDecayEvent(CraftBlock.at(level, pos)).callEvent();
		} finally {
			firing = false;
		}
		if (!allowed || level.getBlockState(pos) != state) return false;

		if (settings.effects()) {
			level.destroyBlock(pos, settings.drops());
		} else {
			if (settings.drops()) Block.dropResources(state, level, pos);
			level.removeBlock(pos, false);
		}
		if (settings.fast()) queueNeighbours(level, pos, settings);
		return false;
	}

	// Same blocks vanilla counts: the prevents_nearby_leaf_decay tag is a log, anything with leaf distance is a leaf. Unloaded chunks count as air.
	private static LeafSupport.Kind kind(ServerLevel level, int x, int y, int z) {
		BlockState state = level.getBlockStateIfLoaded(CURSOR.set(x, y, z));
		if (state == null) return LeafSupport.Kind.OTHER;
		if (state.is(BlockTags.PREVENTS_NEARBY_LEAF_DECAY)) return LeafSupport.Kind.LOG;
		if (state.getBlock() instanceof LeavesBlock) return LeafSupport.Kind.LEAF;
		return LeafSupport.Kind.OTHER;
	}
}
