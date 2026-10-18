package dev.outfoxed.leafitbe.modules.plugin.commands;

import com.jazzkuh.commandlib.common.annotations.*;
import com.jazzkuh.commandlib.paper.AnnotationCommand;
import dev.outfoxed.leafitbe.LeafitbePlugin;
import dev.outfoxed.leafitbe.modules.leaves.LeavesModule;
import dev.outfoxed.leafitbe.utils.ChatUtils;
import lombok.SneakyThrows;
import org.bukkit.command.CommandSender;

import java.net.URL;
import java.util.jar.Manifest;

@Command("Leafitbe")
public class LeafitbeCommand extends AnnotationCommand {

	@Main
	@Alias("leaf")
	@Description("The leafitbe command.")
	@Usage("<subcommand>")
	public void main(CommandSender sender) {
		version(sender);
	}

	@Subcommand("reload")
	@Description("Reloads the plugin configuration.")
	@Permission("leafitbe.command.reload")
	public void reload(CommandSender sender) {
		LeafitbePlugin.getInstance().reloadConfig();
		LeafitbePlugin.getModuleManager().get(LeavesModule.class).reload();
		sender.sendMessage(ChatUtils.prefix("<success>Configuration reloaded."));
	}

	@SneakyThrows
	@Subcommand("version")
	@Description("Shows the version of the plugin.")
	public void version(CommandSender sender) {
		URL url = LeafitbePlugin.getInstance().getClass().getClassLoader().getResource("META-INF/MANIFEST.MF");
		if (url == null) {
			sender.sendMessage(ChatUtils.format("<error>Unable to load version information."));
			return;
		}
		try (var in = url.openStream()) {
			var attrs = new Manifest(in).getMainAttributes();
			String version = attrs.getValue("Implementation-Version");
			String buildTime = attrs.getValue("Build-Time");
			String maintainers = attrs.getValue("Maintainers");

			sender.sendMessage("");
			sender.sendMessage(ChatUtils.format("<color>Leafitbe</color>", ChatUtils.PRIMARY));
			sender.sendMessage(ChatUtils.format("<color_alt>Version:</color_alt> %1", ChatUtils.PRIMARY, version != null ? version : "<error>Unknown"));
			sender.sendMessage(ChatUtils.format("<color_alt>Build Date:</color_alt> %1", ChatUtils.PRIMARY, buildTime != null ? buildTime : "<error>Unknown"));
			sender.sendMessage(ChatUtils.format("<color_alt>Maintained by:</color_alt> %1", ChatUtils.PRIMARY, maintainers != null ? maintainers : "<error>Unknown"));
			sender.sendMessage("");
		}
	}
}
