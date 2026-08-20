package betterteam.client.gui;

import java.util.ArrayList;

import betterteam.client.BetterTeamClient;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class BetterTeamConfigScreen extends Screen {
	private final Screen parent;
	private final BetterTeamConfig config;
	private TeamConfig team;
	private Tab currentTab = Tab.TEAMS;
	private EditBox newTeamField;

	public BetterTeamConfigScreen(Screen parent) {
		super(Component.literal("BetterTeam"));
		this.parent = parent;
		this.config = BetterTeamClient.getConfig();
		this.team = config.getActiveTeam();
	}

	@Override
	protected void init() {
		if (team == null) {
			team = config.getActiveTeam();
		}
		int tabWidth = 60;
		int startX = (width - tabWidth) / 2;
		int y = 20;
		addRenderableWidget(Button.builder(Component.literal("队伍"), button -> switchTab(Tab.TEAMS))
			.bounds(startX, y, tabWidth, 20).build());
		addRenderableWidget(Button.builder(Component.literal("保存并返回"), button -> {
			config.save();
			if (minecraft != null) {
				minecraft.setScreen(parent);
			}
		}).bounds(width / 2 - 100, height - 30, 200, 20).build());
		initTabContent();
	}

	private void initTabContent() {
		int centerX = width / 2;
		int contentY = 60;
		if (currentTab == Tab.TEAMS) {
			newTeamField = new EditBox(font, centerX - 100, contentY, 150, 20, Component.literal(""));
			newTeamField.setMaxLength(32);
			newTeamField.setHint(Component.literal("新建队伍名"));
			addRenderableWidget(newTeamField);
			addRenderableWidget(Button.builder(Component.literal("添加"), button -> {
				String name = newTeamField.getValue().trim();
				if (!name.isEmpty()) {
					team = config.createTeam();
					team.name = name;
					config.activeTeamId = team.id;
					newTeamField.setValue("");
					reload();
				}
			}).bounds(centerX + 55, contentY, 50, 20).build());
			int listY = contentY + 30;
			for (TeamConfig teamConfig : new ArrayList<>(config.teams)) {
				if (listY > height - 60) {
					break;
				}
				int colorValue = teamConfig.getNameTextColorInt();
				Component teamText = Component.literal(teamConfig.name).withStyle(style -> style.withColor(TextColor.fromRgb(colorValue)));
				addRenderableWidget(Button.builder(teamText, button -> {
					team = teamConfig;
					config.activeTeamId = teamConfig.id;
					if (minecraft != null) {
						minecraft.setScreen(new BetterTeamEditTeamScreen(this, config, teamConfig));
					}
				}).bounds(centerX - 100, listY, 150, 20).build());
				addRenderableWidget(Button.builder(Component.literal("X").withStyle(ChatFormatting.RED), button -> {
					removeTeam(teamConfig);
					config.save();
					reload();
				}).bounds(centerX + 55, listY, 20, 20).build());
				listY += 25;
			}
		}
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.fill(0, 0, width, height, 0xAA000000);
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		int centerX = width / 2;
		guiGraphics.centeredText(font, title, centerX, 5, 0xFFFFFF);
		if (currentTab == Tab.TEAMS) {
			guiGraphics.text(font, Component.literal("创建队伍"), centerX - 100, 46, 0xFFFFFF, true);
			guiGraphics.text(font, Component.literal("队伍列表"), centerX - 100, 92, 0xFFFFFF, true);
		}
	}

	private void switchTab(Tab tab) {
		currentTab = tab;
		reload();
	}

	private void reload() {
		clearWidgets();
		init(width, height);
	}

	private void removeTeam(TeamConfig target) {
		config.teams.remove(target);
		if (config.teams.isEmpty()) {
			team = null;
			config.activeTeamId = null;
			return;
		}
		if (team == target) {
			team = config.teams.get(0);
			config.activeTeamId = team.id;
		}
	}

	private enum Tab {
		TEAMS
	}
}

class BetterTeamEditTeamScreen extends Screen {
	private final Screen parent;
	private final BetterTeamConfig config;
	private final TeamConfig team;
	private EditBox teamNameField;
	private EditBox textColorField;
	private EditBox backgroundColorField;
	private EditBox memberField;
	private Button friendlyButton;
	private Button modeButton;
	private Button textOpacityButton;
	private Button backgroundOpacityButton;

	public BetterTeamEditTeamScreen(Screen parent, BetterTeamConfig config, TeamConfig team) {
		super(Component.literal("编辑队伍"));
		this.parent = parent;
		this.config = config;
		this.team = team;
	}

	@Override
	protected void init() {
		int centerX = width / 2;
		int y = 40;
		int fieldWidth = 120;
		int buttonWidth = 78;
		int gap = 2;
		teamNameField = new EditBox(font, centerX - 100, y, 200, 20, Component.literal(""));
		teamNameField.setMaxLength(32);
		teamNameField.setValue(team.name);
		addRenderableWidget(teamNameField);
		y += 30;
		modeButton = addRenderableWidget(Button.builder(getModeText(), button -> {
			team.whitelist = !team.isWhitelist();
			modeButton.setMessage(getModeText());
			config.save();
		}).bounds(centerX - 100, y, 200, 20).build());
		y += 30;
		textColorField = new EditBox(font, centerX - 100, y, fieldWidth, 20, Component.literal(""));
		textColorField.setMaxLength(7);
		textColorField.setValue(team.getNameTextColor());
		textColorField.setHint(Component.literal("#RRGGBB"));
		addRenderableWidget(textColorField);
		textOpacityButton = addRenderableWidget(Button.builder(getTextOpacityText(), button -> {
			team.nameTextOpacity = nextOpacity(team.getNameTextOpacity());
			textOpacityButton.setMessage(getTextOpacityText());
			config.save();
		}).bounds(centerX - 100 + fieldWidth + gap, y, buttonWidth, 20).build());
		y += 30;
		backgroundColorField = new EditBox(font, centerX - 100, y, fieldWidth, 20, Component.literal(""));
		backgroundColorField.setMaxLength(7);
		backgroundColorField.setValue(team.getNameBackgroundColor());
		backgroundColorField.setHint(Component.literal("#RRGGBB"));
		addRenderableWidget(backgroundColorField);
		backgroundOpacityButton = addRenderableWidget(Button.builder(getBackgroundOpacityText(), button -> {
			team.nameBackgroundOpacity = nextOpacity(team.getNameBackgroundOpacity());
			backgroundOpacityButton.setMessage(getBackgroundOpacityText());
			config.save();
		}).bounds(centerX - 100 + fieldWidth + gap, y, buttonWidth, 20).build());
		y += 30;
		friendlyButton = addRenderableWidget(Button.builder(getFriendlyText(), button -> {
			team.preventFriendlyFire = !team.preventFriendlyFire;
			friendlyButton.setMessage(getFriendlyText());
			config.save();
		}).bounds(centerX - 100, y, 200, 20).build());
		y += 30;
		memberField = new EditBox(font, centerX - 100, y, 150, 20, Component.literal(""));
		memberField.setMaxLength(256);
		memberField.setHint(Component.literal("玩家名称或正则"));
		addRenderableWidget(memberField);
		addRenderableWidget(Button.builder(Component.literal("添加"), button -> {
			String name = memberField.getValue().trim();
			if (!name.isEmpty()) {
				team.addMember(name);
				config.save();
				memberField.setValue("");
				reload();
			}
		}).bounds(centerX + 55, y, 45, 20).build());
		int listY = y + 30;
		for (String member : new ArrayList<>(team.members)) {
			if (listY > height - 50) {
				break;
			}
			String memberName = member;
			addRenderableWidget(Button.builder(Component.literal("删除").withStyle(ChatFormatting.RED), button -> {
				team.removeMember(memberName);
				config.save();
				reload();
			}).bounds(centerX + 55, listY, 50, 20).build());
			listY += 25;
		}
		addRenderableWidget(Button.builder(Component.literal("返回"), button -> onClose())
			.bounds(centerX - 100, height - 30, 200, 20).build());
		addRenderableWidget(Button.builder(Component.literal("删除队伍").withStyle(ChatFormatting.RED), button -> {
			config.teams.remove(team);
			if (config.teams.isEmpty()) {
				config.activeTeamId = null;
			} else if (config.activeTeamId != null && config.getTeamById(config.activeTeamId) == null) {
				config.activeTeamId = config.teams.get(0).id;
			}
			config.save();
			if (minecraft != null) {
				minecraft.setScreen(parent);
			}
		}).bounds(centerX - 100, height - 55, 200, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		guiGraphics.fill(0, 0, width, height, 0xAA000000);
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
		int centerX = width / 2;
		guiGraphics.centeredText(font, title, centerX, 10, 0xFFFFFF);
		guiGraphics.text(font, Component.literal("队伍名"), centerX - 100, 26, 0xFFFFFF, true);
		guiGraphics.text(font, Component.literal("模式"), centerX - 100, 56, 0xFFFFFF, true);
		guiGraphics.text(font, Component.literal("名称文字"), centerX - 100, 86, 0xFFFFFF, true);
		guiGraphics.text(font, Component.literal("名称背景"), centerX - 100, 116, 0xFFFFFF, true);
		guiGraphics.text(font, Component.literal("成员列表"), centerX - 100, 240, 0xFFFFFF, true);
		int y = 250;
		for (String member : team.members) {
			if (y > height - 50) {
				break;
			}
			guiGraphics.fill(centerX - 100, y, centerX + 50, y + 20, 0x80000000);
			guiGraphics.text(font, Component.literal(member), centerX - 95, y + 6, 0xFFFFFFFF, true);
			y += 25;
		}
	}

	@Override
	public void onClose() {
		applyTeamName();
		applyColorSettings();
		config.save();
		if (minecraft != null) {
			minecraft.setScreen(parent);
		}
	}

	private void applyTeamName() {
		String name = teamNameField.getValue().trim();
		if (!name.isEmpty()) {
			team.name = name;
		}
	}

	private void applyColorSettings() {
		String textColor = normalizeHex(textColorField.getValue(), team.nameTextColor);
		String backgroundColor = normalizeHex(backgroundColorField.getValue(), team.nameBackgroundColor);
		if (textColor != null) {
			team.nameTextColor = textColor;
		}
		if (backgroundColor != null) {
			team.nameBackgroundColor = backgroundColor;
		}
	}

	private void reload() {
		clearWidgets();
		init(width, height);
	}

	private Component getModeText() {
		return Component.literal("模式: " + (team.isWhitelist() ? "白名单" : "黑名单"));
	}

	private Component getTextOpacityText() {
		return Component.literal("文字透明度: " + formatPercent(team.getNameTextOpacity()));
	}

	private Component getBackgroundOpacityText() {
		return Component.literal("背景透明度: " + formatPercent(team.getNameBackgroundOpacity()));
	}

	private Component getFriendlyText() {
		return Component.literal("防误伤: " + (team.preventFriendlyFire ? "开" : "关"));
	}

	private String formatPercent(float value) {
		int percent = Math.round(value * 100.0F);
		return percent + "%";
	}

	private float nextOpacity(float value) {
		float next = Math.round((value + 0.1F) * 10.0F) / 10.0F;
		if (next > 1.0F) {
			next = 0.0F;
		}
		return next;
	}

	private String normalizeHex(String value, String fallback) {
		if (value == null) {
			return fallback;
		}
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			return fallback;
		}
		String hex = trimmed.startsWith("#") ? trimmed.substring(1) : trimmed;
		if (hex.length() != 6) {
			return fallback;
		}
		for (int i = 0; i < hex.length(); i++) {
			char c = hex.charAt(i);
			boolean ok = (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
			if (!ok) {
				return fallback;
			}
		}
		return "#" + hex.toUpperCase();
	}
}
