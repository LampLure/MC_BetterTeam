package betterteam.client.gui;

import java.util.ArrayList;
import java.util.Arrays;

import betterteam.client.BetterTeamClient;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;

public class BetterTeamConfigScreen extends Screen {
	static final ChatFormatting[] HIGHLIGHT_COLOR_OPTIONS = Arrays.stream(ChatFormatting.values())
		.filter(ChatFormatting::isColor)
		.toArray(ChatFormatting[]::new);

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
			newTeamField = new EditBox(font, centerX - 100, contentY, 150, 20, Component.empty());
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
				Component teamText = Component.literal(teamConfig.name)
					.withStyle(style -> style.withColor(TextColor.fromRgb(colorValue)));
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
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, width, height, 0xAA000000);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		int centerX = width / 2;
		graphics.centeredText(font, title.getString(), centerX, 5, 0xFFFFFF);
		if (currentTab == Tab.TEAMS) {
			graphics.text(font, "创建队伍", centerX - 100, 46, 0xFFFFFF);
			graphics.text(font, "队伍列表", centerX - 100, 92, 0xFFFFFF);
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
	private static final int FIELD_LEFT = -100;
	private static final int FIELD_WIDTH = 200;
	private static final int ROW_HEIGHT = 20;
	private static final int ROW_GAP = 30;
	private static final int ACTION_BUTTON_GAP = 4;
	private static final int BOTTOM_MARGIN = 30;

	private final Screen parent;
	private final BetterTeamConfig config;
	private final TeamConfig team;
	private EditBox teamNameField;
	private EditBox textColorField;
	private EditBox backgroundColorField;
	private EditBox memberField;
	private Button friendlyButton;
	private Button modeButton;
	private Button highlightModeButton;
	private Button highlightColorButton;
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
		int bottomButtonsY = getBottomButtonsY();
		int halfButtonWidth = (FIELD_WIDTH - ACTION_BUTTON_GAP) / 2;
		int memberListBottom = bottomButtonsY - 12;
		teamNameField = new EditBox(font, centerX + FIELD_LEFT, y, FIELD_WIDTH, ROW_HEIGHT, Component.empty());
		teamNameField.setMaxLength(32);
		teamNameField.setValue(team.name);
		addRenderableWidget(teamNameField);
		y += ROW_GAP;
		modeButton = addRenderableWidget(Button.builder(getModeText(), button -> {
			team.whitelist = !team.isWhitelist();
			modeButton.setMessage(getModeText());
			config.save();
		}).bounds(centerX + FIELD_LEFT, y, FIELD_WIDTH, ROW_HEIGHT).build());
		y += ROW_GAP;
		textColorField = new EditBox(font, centerX + FIELD_LEFT, y, fieldWidth, ROW_HEIGHT, Component.empty());
		textColorField.setMaxLength(7);
		textColorField.setValue(team.getNameTextColor());
		textColorField.setHint(Component.literal("#RRGGBB"));
		addRenderableWidget(textColorField);
		textOpacityButton = addRenderableWidget(Button.builder(getTextOpacityText(), button -> {
			team.nameTextOpacity = nextOpacity(team.getNameTextOpacity());
			textOpacityButton.setMessage(getTextOpacityText());
			config.save();
		}).bounds(centerX + FIELD_LEFT + fieldWidth + gap, y, buttonWidth, ROW_HEIGHT).build());
		y += ROW_GAP;
		backgroundColorField = new EditBox(font, centerX + FIELD_LEFT, y, fieldWidth, ROW_HEIGHT, Component.empty());
		backgroundColorField.setMaxLength(7);
		backgroundColorField.setValue(team.getNameBackgroundColor());
		backgroundColorField.setHint(Component.literal("#RRGGBB"));
		addRenderableWidget(backgroundColorField);
		backgroundOpacityButton = addRenderableWidget(Button.builder(getBackgroundOpacityText(), button -> {
			team.nameBackgroundOpacity = nextOpacity(team.getNameBackgroundOpacity());
			backgroundOpacityButton.setMessage(getBackgroundOpacityText());
			config.save();
		}).bounds(centerX + FIELD_LEFT + fieldWidth + gap, y, buttonWidth, ROW_HEIGHT).build());
		y += ROW_GAP;
		friendlyButton = addRenderableWidget(Button.builder(getFriendlyText(), button -> {
			team.preventFriendlyFire = !team.preventFriendlyFire;
			friendlyButton.setMessage(getFriendlyText());
			config.save();
		}).bounds(centerX + FIELD_LEFT, y, FIELD_WIDTH, ROW_HEIGHT).build());
		y += ROW_GAP;
		highlightModeButton = addRenderableWidget(Button.builder(getHighlightModeText(), button -> {
			team.highlightMode = nextHighlightMode(team.getHighlightMode());
			team.outline = team.getHighlightMode() != TeamConfig.HighlightMode.OFF;
			highlightModeButton.setMessage(getHighlightModeText());
			config.save();
		}).bounds(centerX + FIELD_LEFT, y, FIELD_WIDTH, ROW_HEIGHT).build());
		y += ROW_GAP;
		highlightColorButton = addRenderableWidget(Button.builder(getHighlightColorText(), button -> {
			team.highlightColor = nextHighlightColor().getName();
			highlightColorButton.setMessage(getHighlightColorText());
			config.save();
		}).bounds(centerX + FIELD_LEFT, y, FIELD_WIDTH, ROW_HEIGHT).build());
		y += ROW_GAP;
		memberField = new EditBox(font, centerX + FIELD_LEFT, y, 150, ROW_HEIGHT, Component.empty());
		memberField.setMaxLength(64);
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
		}).bounds(centerX + 55, y, 45, ROW_HEIGHT).build());
		int listY = getMemberListTop();
		for (String member : new ArrayList<>(team.members)) {
			if (listY + ROW_HEIGHT > memberListBottom) {
				break;
			}
			String memberName = member;
			addRenderableWidget(Button.builder(Component.literal("删除").withStyle(ChatFormatting.RED), button -> {
				team.removeMember(memberName);
				config.save();
				reload();
			}).bounds(centerX + 45, listY, 55, ROW_HEIGHT).build());
			listY += 25;
		}
		addRenderableWidget(Button.builder(Component.literal("返回"), button -> onClose())
			.bounds(centerX + FIELD_LEFT, bottomButtonsY, halfButtonWidth, ROW_HEIGHT).build());
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
		}).bounds(centerX + FIELD_LEFT + halfButtonWidth + ACTION_BUTTON_GAP, bottomButtonsY, halfButtonWidth, ROW_HEIGHT).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		graphics.fill(0, 0, width, height, 0xAA000000);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		int centerX = width / 2;
		graphics.centeredText(font, title.getString(), centerX, 10, 0xFFFFFF);
		graphics.text(font, "队伍名", centerX - 100, 26, 0xFFFFFF);
		graphics.text(font, "模式", centerX - 100, 56, 0xFFFFFF);
		graphics.text(font, "名称文字", centerX - 100, 86, 0xFFFFFF);
		graphics.text(font, "名称背景", centerX - 100, 116, 0xFFFFFF);
		graphics.text(font, "发光模式", centerX - 100, 176, 0xFFFFFF);
		graphics.text(font, "发光颜色", centerX - 100, 206, 0xFFFFFF);
		int listLabelY = getMemberListTop() - 12;
		graphics.text(font, "成员列表", centerX - 100, listLabelY, 0xFFFFFF);
		int y = getMemberListTop();
		int memberListBottom = getBottomButtonsY() - 12;
		for (String member : team.members) {
			if (y + ROW_HEIGHT > memberListBottom) {
				break;
			}
			graphics.fill(centerX - 100, y, centerX + 40, y + ROW_HEIGHT, 0x80000000);
			graphics.text(font, member, centerX - 95, y + 6, 0xFFFFFFFF, true);
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

	private int getMemberListTop() {
		return 310;
	}

	private int getBottomButtonsY() {
		return height - BOTTOM_MARGIN;
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

	private Component getHighlightModeText() {
		return Component.literal("发光模式: " + team.getHighlightMode().getDisplayName());
	}

	private Component getHighlightColorText() {
		ChatFormatting formatting = team.getHighlightFormatting();
		return Component.literal("发光颜色: ")
			.append(Component.literal(formatting.getName()).withStyle(formatting));
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

	private TeamConfig.HighlightMode nextHighlightMode(TeamConfig.HighlightMode current) {
		TeamConfig.HighlightMode[] values = TeamConfig.HighlightMode.values();
		int nextIndex = (current.ordinal() + 1) % values.length;
		return values[nextIndex];
	}

	private ChatFormatting nextHighlightColor() {
		ChatFormatting current = team.getHighlightFormatting();
		int currentIndex = Arrays.asList(BetterTeamConfigScreen.HIGHLIGHT_COLOR_OPTIONS).indexOf(current);
		int nextIndex = (currentIndex + 1) % BetterTeamConfigScreen.HIGHLIGHT_COLOR_OPTIONS.length;
		return BetterTeamConfigScreen.HIGHLIGHT_COLOR_OPTIONS[nextIndex];
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
