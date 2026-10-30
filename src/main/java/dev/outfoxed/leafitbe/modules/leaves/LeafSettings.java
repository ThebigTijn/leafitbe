package dev.outfoxed.leafitbe.modules.leaves;

import org.bukkit.configuration.ConfigurationSection;

public record LeafSettings(boolean enabled, int maxDistance, boolean diagonal, boolean drops, boolean effects, boolean fast, int minDelay, int maxDelay, long supportedCacheMs) {
	// Capped so one search stays small: 16 is at most 33^3 blocks.
	private static final int MAX_DISTANCE_CAP = 16;

	public static LeafSettings from(ConfigurationSection config) {
		int minDelay = Math.max(1, config.getInt("decay.fast.min-delay", 4));
		return new LeafSettings(
				config.getBoolean("decay.enabled", true),
				Math.clamp(config.getInt("max-distance", 6), 1, MAX_DISTANCE_CAP),
				config.getBoolean("diagonal", true),
				config.getBoolean("decay.drops", true),
				config.getBoolean("decay.effects", true),
				config.getBoolean("decay.fast.enabled", true),
				minDelay,
				Math.max(minDelay, config.getInt("decay.fast.max-delay", 12)),
				Math.max(0, config.getLong("supported-cache-minutes", 10)) * 60_000);
	}
}
