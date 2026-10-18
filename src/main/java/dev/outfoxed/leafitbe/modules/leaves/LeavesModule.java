package dev.outfoxed.leafitbe.modules.leaves;

import com.jazzkuh.modulemanager.spigot.SpigotModule;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import dev.outfoxed.leafitbe.LeafitbePlugin;
import lombok.Getter;

@SuppressWarnings("unused")
public class LeavesModule extends SpigotModule<LeafitbePlugin> {
	@Getter
	private volatile LeafSettings settings;

	public LeavesModule(SpigotModuleManager<LeafitbePlugin> moduleManager) {
		super(moduleManager);
	}

	@Override
	public void onEnable() {
		reload();
		registerComponent(new LeavesListener(this));
	}

	public void reload() {
		settings = LeafSettings.from(getPlugin().getConfig());
	}
}
