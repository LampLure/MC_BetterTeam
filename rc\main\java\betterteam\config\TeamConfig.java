package betterteam.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.minecraft.ChatFormatting;

public class TeamConfig {
	public String id;
	public String name;
	public String color;
	public Boolean whitelist;
	public boolean outline;
	public boolean preventFriendlyFire;
	public String nameTextColor;
	public String nameBackgroundColor;
	public Float nameTextOpacity;
	public Float nameBackgroundOpacity;
	public List<String> members = new ArrayList<>();

	public boolean isWhitelist() {
		return whitelist == null || whitelist;
	}

	public float getNameTextOpacity() {
		return normalizeOpacity(nameTextOpacity, 1.0F);
	}

	public float getNameBackgroundOpacity() {
		return normalizeOpacity(nameBackgroundOpacity, 0.25F);
	}

	public String getNameTextColor() {
		return normalizeHex(nameTextColor, "#FFFFFF");
	}

	public String getNameBackgroundColor() {
		return normalizeHex(nameBackgroundColor, "#000000");
	}

	public int getNameTextColorInt() {
		return parseHexColor(getNameTextColor(), 0xFFFFFF);
	}

	public int getNameBackgroundColorInt() {
		return parseHexColor(getNameBackgroundColor(), 0x000000);
	}

	public boolean isMember(String name) {
		if (name == null) {
			return false;
		}
		String cleaned = stripColorCodes(name.trim());
		if (cleaned.isEmpty()) {
			return false;
		}
		for (String member : members) {
			if (matchesMemberRule(member, cleaned)) {
				return true;
			}
		}
		return false;
	}

	public void addMember(String name) {
		if (name == null) {
			return;
		}
		String trimmed = name.trim();
		if (trimmed.isEmpty()) {
			return;
		}
		if (!isMember(trimmed)) {
			members.add(trimmed);
		}
	}

	public void removeMember(String name) {
		if (name == null) {
			return;
		}
		String trimmed = name.trim();
		if (trimmed.isEmpty()) {
			return;
		}
		members.removeIf(member -> member != null && member.equalsIgnoreCase(trimmed));
	}

	private boolean matchesMemberRule(String member, String target) {
		if (member == null) {
			return false;
		}
		String rule = member.trim();
		if (rule.isEmpty()) {
			return false;
		}
		if (!looksLikeRegex(rule)) {
			return rule.equalsIgnoreCase(target);
		}
		try {
			return Pattern.compile(rule, Pattern.CASE_INSENSITIVE).matcher(target).matches();
		} catch (PatternSyntaxException e) {
			return rule.equalsIgnoreCase(target);
		}
	}

	private boolean looksLikeRegex(String value) {
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
				case '.':
				case '*':
				case '+':
				case '?':
				case '|':
				case '(':
				case ')':
				case '[':
				case ']':
				case '{':
				case '}':
				case '^':
				case '$':
				case '\\':
					return true;
				default:
					break;
			}
		}
		return false;
	}

	private String stripColorCodes(String value) {
		String stripped = ChatFormatting.stripFormatting(value);
		return stripped == null ? "" : stripped.trim();
	}

	private float normalizeOpacity(Float value, float fallback) {
		if (value == null || Float.isNaN(value)) {
			return fallback;
		}
		if (value < 0.0F) {
			return 0.0F;
		}
		if (value > 1.0F) {
			return 1.0F;
		}
		return value;
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

	private int parseHexColor(String value, int fallback) {
		if (value == null) {
			return fallback;
		}
		String hex = value.startsWith("#") ? value.substring(1) : value;
		if (hex.length() != 6) {
			return fallback;
		}
		try {
			return Integer.parseInt(hex, 16);
		} catch (NumberFormatException e) {
			return fallback;
		}
	}
}
