package betterteam.client.gui;

import java.util.ArrayList;

import betterteam.client.BetterTeamClient;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

public class BetterTeamConfigScreen extends Screen {
	private final Screen parent;
	private final BetterTeamConfig config;
	private TeamConfig team;
	private Tab currentTab = Tab.TEAMS;
	private TextFieldWidget newTeamField;

	public BetterTeamConfigScreen(Screen parent) {
		super(Text.literal("BetterTeam"));
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
		addDrawableChild(ButtonWidget.builder(Text.literal("队伍"), button -> switchTab(Tab.TEAMS))
			.dimensions(startX, y, tabWidth, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("保存并返回"), button -> {
			config.save();
			if (client != null) {
				client.setScreen(parent);
			}
		}).dimensions(width / 2 - 100, height - 30, 200, 20).build());
		initTabContent();
	}

	private void initTabContent() {
		int centerX = width / 2;
		int contentY = 60;
		if (currentTab == Tab.TEAMS) {
			newTeamField = new TextFieldWidget(textRenderer, centerX - 100, contentY, 150, 20, Text.literal(""));
			newTeamField.setMaxLength(32);
			newTeamField.setPlaceholder(Text.literal("新建队伍名"));
			addDrawableChild(newTeamField);
			addDrawableChild(ButtonWidget.builder(Text.literal("添加"), button -> {
				String name = newTeamField.getText().trim();
				if (!name.isEmpty()) {
					team = config.createTeam();
					team.name = name;
					config.activeTeamId = team.id;
					newTeamField.setText("");
					reload();
				}
			}).dimensions(centerX + 55, contentY, 50, 20).build());
			int listY = contentY + 30;
			for (TeamConfig teamConfig : new ArrayList<>(config.teams)) {
				if (listY > height - 60) {
					break;
				}
				int colorValue = teamConfig.getNameTextColorInt();
				Text teamText = Text.literal(teamConfig.name).styled(style -> style.withColor(TextColor.fromRgb(colorValue)));
				addDrawableChild(ButtonWidget.builder(teamText, button -> {
					team = teamConfig;
					config.activeTeamId = teamConfig.id;
					if (client != null) {
						client.setScreen(new BetterTeamEditTeamScreen(this, config, teamConfig));
					}
				}).dimensions(centerX - 100, listY, 150, 20).build());
				addDrawableChild(ButtonWidget.builder(Text.literal("X").formatted(Formatting.RED), button -> {
					removeTeam(teamConfig);
					config.save();
					reload();
				}).dimensions(centerX + 55, listY, 20, 20).build());
				listY += 25;
			}
		}
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, 0xAA000000);
		super.render(context, mouseX, mouseY, delta);
		int centerX = width / 2;
		context.drawCenteredTextWithShadow(textRenderer, title, centerX, 5, 0xFFFFFF);
		if (currentTab == Tab.TEAMS) {
			context.drawTextWithShadow(textRenderer, Text.literal("创建队伍"), centerX - 100, 46, 0xFFFFFF);
			context.drawTextWithShadow(textRenderer, Text.literal("队伍列表"), centerX - 100, 92, 0xFFFFFF);
		}
	}

	private void switchTab(Tab tab) {
		currentTab = tab;
		reload();
	}

	private void reload() {
		clearChildren();
		if (client != null) {
			init(client, width, height);
		}
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
	private TextFieldWidget teamNameField;
	private TextFieldWidget textColorField;
	private TextFieldWidget backgroundColorField;
	private TextFieldWidget memberField;
	private ButtonWidget friendlyButton;
	private ButtonWidget modeButton;
	private ButtonWidget textOpacityButton;
	private ButtonWidget backgroundOpacityButton;

	public BetterTeamEditTeamScreen(Screen parent, BetterTeamConfig config, TeamConfig team) {
		super(Text.literal("编辑队伍"));
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
		teamNameField = new TextFieldWidget(textRenderer, centerX - 100, y, 200, 20, Text.literal(""));
		teamNameField.setMaxLength(32);
		teamNameField.setText(team.name);
		addDrawableChild(teamNameField);
		y += 30;
		modeButton = addDrawableChild(ButtonWidget.builder(getModeText(), button -> {
			team.whitelist = !team.isWhitelist();
			modeButton.setMessage(getModeText());
			config.save();
		}).dimensions(centerX - 100, y, 200, 20).build());
		y += 30;
		textColorField = new TextFieldWidget(textRenderer, centerX - 100, y, fieldWidth, 20, Text.literal(""));
		textColorField.setMaxLength(7);
		textColorField.setText(team.getNameTextColor());
		textColorField.setPlaceholder(Text.literal("#RRGGBB"));
		addDrawableChild(textColorField);
		textOpacityButton = addDrawableChild(ButtonWidget.builder(getTextOpacityText(), button -> {
			team.nameTextOpacity = nextOpacity(team.getNameTextOpacity());
			textOpacityButton.setMessage(getTextOpacityText());
			config.save();
		}).dimensions(centerX - 100 + fieldWidth + gap, y, buttonWidth, 20).build());
		y += 30;
		backgroundColorField = new TextFieldWidget(textRenderer, centerX - 100, y, fieldWidth, 20, Text.literal(""));
		backgroundColorField.setMaxLength(7);
		backgroundColorField.setText(team.getNameBackgroundColor());
		backgroundColorField.setPlaceholder(Text.literal("#RRGGBB"));
		addDrawableChild(backgroundColorField);
		backgroundOpacityButton = addDrawableChild(ButtonWidget.builder(getBackgroundOpacityText(), button -> {
			team.nameBackgroundOpacity = nextOpacity(team.getNameBackgroundOpacity());
			backgroundOpacityButton.setMessage(getBackgroundOpacityText());
			config.save();
		}).dimensions(centerX - 100 + fieldWidth + gap, y, buttonWidth, 20).build());
		y += 30;
		friendlyButton = addDrawableChild(ButtonWidget.builder(getFriendlyText(), button -> {
			team.preventFriendlyFire = !team.preventFriendlyFire;
			friendlyButton.setMessage(getFriendlyText());
			config.save();
		}).dimensions(centerX - 100, y, 200, 20).build());
		y += 30;
		memberField = new TextFieldWidget(textRenderer, centerX - 100, y, 150, 20, Text.literal(""));
		memberField.setMaxLength(32);
		memberField.setPlaceholder(Text.literal("玩家名称"));
		addDrawableChild(memberField);
		addDrawableChild(ButtonWidget.builder(Text.literal("添加"), button -> {
			String name = memberField.getText().trim();
			if (!name.isEmpty()) {
				team.addMember(name);
				config.save();
				memberField.setText("");
				reload();
			}
		}).dimensions(centerX + 55, y, 45, 20).build());
		int listY = y + 30;
		for (String member : new ArrayList<>(team.members)) {
			if (listY > height - 50) {
				break;
			}
			String memberName = member;
			addDrawableChild(ButtonWidget.builder(Text.literal("删除").formatted(Formatting.RED), button -> {
				team.removeMember(memberName);
				config.save();
				reload();
			}).dimensions(centerX + 55, listY, 50, 20).build());
			listY += 25;
		}
		addDrawableChild(ButtonWidget.builder(Text.literal("返回"), button -> close())
			.dimensions(centerX - 100, height - 30, 200, 20).build());
		addDrawableChild(ButtonWidget.builder(Text.literal("删除队伍").formatted(Formatting.RED), button -> {
			config.teams.remove(team);
			if (config.teams.isEmpty()) {
				config.activeTeamId = null;
			} else if (config.activeTeamId != null && config.getTeamById(config.activeTeamId) == null) {
				config.activeTeamId = config.teams.get(0).id;
			}
			config.save();
			if (client != null) {
				client.setScreen(parent);
			}
		}).dimensions(centerX - 100, height - 55, 200, 20).build());
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, 0xAA000000);
		super.render(context, mouseX, mouseY, delta);
		int centerX = width / 2;
		context.drawCenteredTextWithShadow(textRenderer, title, centerX, 10, 0xFFFFFF);
		context.drawTextWithShadow(textRenderer, Text.literal("队伍名"), centerX - 100, 26, 0xFFFFFF);
		context.drawTextWithShadow(textRenderer, Text.literal("模式"), centerX - 100, 56, 0xFFFFFF);
		context.drawTextWithShadow(textRenderer, Text.literal("名称文字"), centerX - 100, 86, 0xFFFFFF);
		context.drawTextWithShadow(textRenderer, Text.literal("名称背景"), centerX - 100, 116, 0xFFFFFF);
		context.drawTextWithShadow(textRenderer, Text.literal("成员列表"), centerX - 100, 240, 0xFFFFFF);
		int y = 250;
		for (String member : team.members) {
			if (y > height - 50) {
				break;
			}
			context.fill(centerX - 100, y, centerX + 50, y + 20, 0x80000000);
			context.drawText(textRenderer, Text.literal(member), centerX - 95, y + 6, 0xFFFFFFFF, true);
			y += 25;
		}
	}

	@Override
	public void close() {
		applyTeamName();
		applyColorSettings();
		config.save();
		if (client != null) {
			client.setScreen(parent);
		}
	}

	private void applyTeamName() {
		String name = teamNameField.getText().trim();
		if (!name.isEmpty()) {
			team.name = name;
		}
	}

	private void applyColorSettings() {
		String textColor = normalizeHex(textColorField.getText(), team.nameTextColor);
		String backgroundColor = normalizeHex(backgroundColorField.getText(), team.nameBackgroundColor);
		if (textColor != null) {
			team.nameTextColor = textColor;
		}
		if (backgroundColor != null) {
			team.nameBackgroundColor = backgroundColor;
		}
	}

	private void reload() {
		clearChildren();
		if (client != null) {
			init(client, width, height);
		}
	}

	private Text getModeText() {
		return Text.literal("模式: " + (team.isWhitelist() ? "白名单" : "黑名单"));
	}

	private Text getTextOpacityText() {
		return Text.literal("文字透明度: " + formatPercent(team.getNameTextOpacity()));
	}

	private Text getBackgroundOpacityText() {
		return Text.literal("背景透明度: " + formatPercent(team.getNameBackgroundOpacity()));
	}

	private Text getFriendlyText() {
		return Text.literal("防误伤: " + (team.preventFriendlyFire ? "开" : "关"));
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
