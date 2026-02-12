package betterteam.client;

import org.lwjgl.glfw.GLFW;

import betterteam.client.gui.BetterTeamConfigScreen;
import betterteam.config.BetterTeamConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class BetterTeamClient implements ClientModInitializer {
	private static BetterTeamConfig config;
	private static KeyBinding openConfigKey;

	@Override
	public void onInitializeClient() {
		config = BetterTeamConfig.load();
		openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.betterteam.open_config",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_U,
			"category.betterteam"
		));
		ClientTickEvents.END_CLIENT_TICK.register(BetterTeamClient::onEndClientTick);
	}

	public static BetterTeamConfig getConfig() {
		return config;
	}

	private static void onEndClientTick(MinecraftClient client) {
		while (openConfigKey.wasPressed()) {
			client.setScreen(new BetterTeamConfigScreen(client.currentScreen));
		}
		if (client.world == null) {
			return;
		}
	}
}
