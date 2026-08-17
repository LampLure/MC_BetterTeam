package betterteam.client;

import org.lwjgl.glfw.GLFW;

import betterteam.client.gui.BetterTeamConfigScreen;
import betterteam.config.BetterTeamConfig;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

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
			KeyMapping.Category.MISC
		));
		ClientTickEvents.END_CLIENT_TICK.register(BetterTeamClient::onEndClientTick);
	}

	public static BetterTeamConfig getConfig() {
		return config;
	}

	private static void onEndClientTick(Minecraft client) {
		while (openConfigKey.consumeClick()) {
			client.setScreen(new BetterTeamConfigScreen(client.screen));
		}
		if (client.level == null) {
			return;
		}
	}
}
