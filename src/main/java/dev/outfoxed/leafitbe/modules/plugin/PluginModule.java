package dev.outfoxed.leafitbe.modules.plugin;

import com.jazzkuh.modulemanager.spigot.SpigotModule;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import dev.outfoxed.leafitbe.LeafitbePlugin;
import dev.outfoxed.leafitbe.modules.plugin.commands.LeafitbeCommand;

@SuppressWarnings("unused")
public class PluginModule extends SpigotModule<LeafitbePlugin> {

	public PluginModule(SpigotModuleManager<LeafitbePlugin> moduleManager) {
		super(moduleManager);
	}

	@Override
	public void onEnable() {
		registerComponent(new LeafitbeCommand());
	}
}
