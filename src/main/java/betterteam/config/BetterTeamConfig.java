package betterteam.config;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;

public class BetterTeamConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "betterteam.json";
	private static final String MEMBERS_DIR_NAME = "betterteam";

	public String activeTeamId;
	public List<TeamConfig> teams = new ArrayList<>();

	public static BetterTeamConfig load() {
		Path path = getConfigPath();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path)) {
				BetterTeamConfig config = GSON.fromJson(reader, BetterTeamConfig.class);
				if (config != null) {
					config.ensureDefaults();
					config.loadMemberFiles();
					return config;
				}
			} catch (IOException | JsonSyntaxException ignored) {
			}
		}
		BetterTeamConfig config = createDefault();
		config.save();
		return config;
	}

	public void save() {
		ensureDefaults();
		Path path = getConfigPath();
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path)) {
				GSON.toJson(this, writer);
			}
		} catch (IOException ignored) {
		}
		saveMemberFiles();
	}

	public TeamConfig getActiveTeam() {
		TeamConfig team = getTeamById(activeTeamId);
		if (team != null) {
			return team;
		}
		if (teams.isEmpty()) {
			activeTeamId = null;
			return null;
		}
		activeTeamId = teams.get(0).id;
		return teams.get(0);
	}

	public TeamConfig getTeamById(String id) {
		if (id == null) {
			return null;
		}
		for (TeamConfig team : teams) {
			if (id.equals(team.id)) {
				return team;
			}
		}
		return null;
	}

	public TeamConfig createTeam() {
		TeamConfig team = new TeamConfig();
		team.id = UUID.randomUUID().toString();
		team.name = "队伍" + (teams.size() + 1);
		team.color = ChatFormatting.GREEN.getName();
		team.whitelist = true;
		team.outline = true;
		team.preventFriendlyFire = true;
		team.nameTextColor = "#FFFFFF";
		team.nameBackgroundColor = "#000000";
		team.nameTextOpacity = 1.0F;
		team.nameBackgroundOpacity = 0.25F;
		teams.add(team);
		return team;
	}

	private void ensureDefaults() {
		if (teams == null) {
			teams = new ArrayList<>();
		}
		if (!teams.isEmpty() && (activeTeamId == null || getTeamById(activeTeamId) == null)) {
			activeTeamId = teams.get(0).id;
		}
		if (teams.isEmpty()) {
			activeTeamId = null;
		}
		for (TeamConfig team : teams) {
			if (team.id == null) {
				team.id = UUID.randomUUID().toString();
			}
			if (team.name == null || team.name.isBlank()) {
				team.name = "队伍" + (teams.indexOf(team) + 1);
			}
			if (team.color == null || ChatFormatting.getByName(team.color) == null) {
				team.color = ChatFormatting.GREEN.getName();
			}
			if (team.whitelist == null) {
				team.whitelist = true;
			}
			if (team.nameTextColor == null) {
				team.nameTextColor = "#FFFFFF";
			}
			if (team.nameBackgroundColor == null) {
				team.nameBackgroundColor = "#000000";
			}
			if (team.nameTextOpacity == null) {
				team.nameTextOpacity = 1.0F;
			} else {
				team.nameTextOpacity = clampOpacity(team.nameTextOpacity, 1.0F);
			}
			if (team.nameBackgroundOpacity == null) {
				team.nameBackgroundOpacity = 0.25F;
			} else {
				team.nameBackgroundOpacity = clampOpacity(team.nameBackgroundOpacity, 0.25F);
			}
			if (team.members == null) {
				team.members = new ArrayList<>();
			}
		}
	}

	private float clampOpacity(float value, float defaultValue) {
		if (Float.isNaN(value)) {
			return defaultValue;
		}
		if (value < 0.0F) {
			return 0.0F;
		}
		if (value > 1.0F) {
			return 1.0F;
		}
		return value;
	}

	private static Path getConfigPath() {
		return FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
	}

	private static Path getMembersDir() {
		return FabricLoader.getInstance().getGameDir().resolve(MEMBERS_DIR_NAME);
	}

	private static Path getMemberFilePath(String teamName) {
		return getMembersDir().resolve(sanitizeFileName(teamName) + ".txt");
	}

	private static Path getLegacyMemberFilePath(String teamName) {
		return getMembersDir().resolve(sanitizeFileName(teamName));
	}

	private static String sanitizeFileName(String name) {
		if (name == null) {
			return "team";
		}
		String trimmed = name.trim();
		if (trimmed.isEmpty()) {
			return "team";
		}
		String cleaned = trimmed.replaceAll("[\\\\/:*?\"<>|]", "_");
		if (cleaned.isBlank()) {
			return "team";
		}
		return cleaned;
	}

	private void loadMemberFiles() {
		Path dir = getMembersDir();
		if (!Files.exists(dir)) {
			return;
		}
		for (TeamConfig team : teams) {
			if (team.id == null) {
				continue;
			}
			Path file = getMemberFilePath(team.name);
			if (!Files.exists(file)) {
				file = getLegacyMemberFilePath(team.name);
			}
			if (!Files.exists(file)) {
				continue;
			}
			try {
				List<String> lines = Files.readAllLines(file);
				List<String> members = new ArrayList<>();
				for (String line : lines) {
					if (line == null) {
						continue;
					}
					String trimmed = line.trim();
					if (!trimmed.isEmpty()) {
						members.add(trimmed);
					}
				}
				team.members = members;
			} catch (IOException ignored) {
			}
		}
	}

	private void saveMemberFiles() {
		Path dir = getMembersDir();
		try {
			Files.createDirectories(dir);
		} catch (IOException ignored) {
			return;
		}
		Set<String> validIds = new HashSet<>();
		for (TeamConfig team : teams) {
			if (team.id == null) {
				continue;
			}
			String fileName = sanitizeFileName(team.name);
			validIds.add(fileName + ".txt");
			Path file = getMemberFilePath(team.name);
			Path legacyFile = getLegacyMemberFilePath(team.name);
			List<String> lines = new ArrayList<>();
			for (String member : team.members) {
				if (member == null) {
					continue;
				}
				String trimmed = member.trim();
				if (!trimmed.isEmpty()) {
					lines.add(trimmed);
				}
			}
			try {
				Files.write(file, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
			} catch (IOException ignored) {
			}
			try {
				Files.deleteIfExists(legacyFile);
			} catch (IOException ignored) {
			}
		}
		try (Stream<Path> stream = Files.list(dir)) {
			for (Path path : stream.toList()) {
				String name = path.getFileName().toString();
				if (!validIds.contains(name)) {
					Files.deleteIfExists(path);
				}
			}
		} catch (IOException ignored) {
		}
	}

	private static BetterTeamConfig createDefault() {
		BetterTeamConfig config = new BetterTeamConfig();
		TeamConfig team = config.createTeam();
		config.activeTeamId = team.id;
		return config;
	}
}
