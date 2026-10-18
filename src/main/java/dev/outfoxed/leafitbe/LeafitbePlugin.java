package dev.outfoxed.leafitbe;

import com.jazzkuh.commandlib.paper.PaperCommandLoader;
import com.jazzkuh.modulemanager.spigot.SpigotModuleManager;
import dev.outfoxed.leafitbe.utils.ChatUtils;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class LeafitbePlugin extends JavaPlugin {
	@Getter
	private static LeafitbePlugin instance;

	@Getter
	private static SpigotModuleManager<@NotNull LeafitbePlugin> moduleManager;

	public LeafitbePlugin() {
		instance = this;
		moduleManager = new SpigotModuleManager<>(this, getComponentLogger());
	}

	@Override
	public void onLoad() {
		saveDefaultConfig();
		moduleManager.scanModules(this.getClass());
		moduleManager.load();
	}

	@Override
	public void onEnable() {
		PaperCommandLoader.loadResolvers();
		PaperCommandLoader.setFormattingProvider((commandException, message) -> ChatUtils.prefix(message));
		moduleManager.enable();
	}

	@Override
	public void onDisable() {
		moduleManager.disable();
	}
}
