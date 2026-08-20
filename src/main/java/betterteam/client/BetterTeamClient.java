package betterteam.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import betterteam.BetterTeam;
import betterteam.client.gui.BetterTeamConfigScreen;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class BetterTeamClient implements ClientModInitializer {
	private static BetterTeamConfig config;
	private static KeyMapping openConfigKey;

	@Override
	public void onInitializeClient() {
		config = BetterTeamConfig.load();
		openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
			"key.betterteam.open_config",
			InputConstants.Type.KEYSYM,
			GLFW.GLFW_KEY_U,
			KeyMapping.Category.register(BetterTeam.id("betterteam"))
		));
		ClientTickEvents.END_CLIENT_TICK.register(BetterTeamClient::onEndClientTick);
		BetterTeam.LOGGER.info("BetterTeam client initialized");
	}

	public static BetterTeamConfig getConfig() {
		return config;
	}

	public static TeamConfig getActiveTeamForName(String name) {
		BetterTeamConfig currentConfig = getConfig();
		if (currentConfig == null) {
			return null;
		}
		TeamConfig team = currentConfig.getActiveTeam();
		if (team == null || name == null || name.isBlank()) {
			return null;
		}
		boolean isMember = team.isMember(name);
		boolean matches = team.isWhitelist() ? isMember : !isMember;
		return matches ? team : null;
	}

	public static boolean shouldGlow(Entity entity) {
		return getGlowTeam(entity) != null;
	}

	public static int getGlowColor(Entity entity) {
		TeamConfig team = getGlowTeam(entity);
		return team != null ? team.getHighlightColorValue() : 0xFFFFFF;
	}

	private static TeamConfig getGlowTeam(Entity entity) {
		if (!(entity instanceof Player player)) {
			return null;
		}
		TeamConfig team = getActiveTeamForName(player.getDisplayName().getString());
		if (team == null) {
			return null;
		}
		return switch (team.getHighlightMode()) {
			case OFF -> null;
			case SNEAKING -> player.isShiftKeyDown() ? team : null;
			case ALWAYS -> team;
		};
	}

	private static void onEndClientTick(Minecraft client) {
		while (openConfigKey.consumeClick()) {
			client.setScreen(new BetterTeamConfigScreen(client.screen));
		}
	}
}
