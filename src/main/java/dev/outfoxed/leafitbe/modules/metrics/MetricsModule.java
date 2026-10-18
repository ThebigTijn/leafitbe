package dev.outfoxed.leafitbe.modules.metrics;

import com.jazzkuh.modulemanager.spigot.SpigotModule;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import dev.outfoxed.leafitbe.LeafitbePlugin;
import org.bstats.bukkit.Metrics;

@SuppressWarnings("unused")
public class MetricsModule extends SpigotModule<LeafitbePlugin> {

	private static final int PLUGIN_ID = 34802;

	public MetricsModule(SpigotModuleManager<LeafitbePlugin> moduleManager) {
		super(moduleManager);
	}

	@Override
	public void onEnable() {
		new Metrics(getPlugin(), PLUGIN_ID);
	}
}
