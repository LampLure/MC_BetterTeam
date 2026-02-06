package betterteam.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.glfw.GLFW;

import betterteam.client.gui.BetterTeamConfigScreen;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;

public class BetterTeamClient implements ClientModInitializer {
	private static BetterTeamConfig config;
	private static KeyBinding openConfigKey;
	private static int tickCounter;

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
		tickCounter++;
		if (tickCounter % 10 == 0) {
			updateTeamVisuals(client);
		}
	}

	private static void updateTeamVisuals(MinecraftClient client) {
		TeamConfig team = config.getActiveTeam();
		if (team == null) {
			return;
		}
		Formatting color = Formatting.byName(team.color);
		if (color == null || !color.isColor()) {
			color = Formatting.GREEN;
		}
		Scoreboard scoreboard = client.world.getScoreboard();
		String teamName = "betterteam_" + team.id;
		Team scoreboardTeam = scoreboard.getTeam(teamName);
		if (scoreboardTeam == null) {
			scoreboardTeam = scoreboard.addTeam(teamName);
		}
		scoreboardTeam.setColor(color);
		Set<String> desiredMembers = new HashSet<>();
		for (PlayerEntity player : client.world.getPlayers()) {
			String name = player.getName().getString();
			boolean isMember = team.isMember(name);
			boolean shouldRender = team.isWhitelist() ? isMember : !isMember;
			if (shouldRender) {
				desiredMembers.add(name);
			}
		}
		List<String> existing = new ArrayList<>(scoreboardTeam.getPlayerList());
		for (String member : existing) {
			if (!desiredMembers.contains(member)) {
				scoreboard.removeScoreHolderFromTeam(member, scoreboardTeam);
			}
		}
		for (String member : desiredMembers) {
			if (!scoreboardTeam.getPlayerList().contains(member)) {
				scoreboard.addScoreHolderToTeam(member, scoreboardTeam);
			}
		}
	}
}
