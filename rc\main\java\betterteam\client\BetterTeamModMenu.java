package betterteam.client;

import betterteam.client.gui.BetterTeamConfigScreen;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

public class BetterTeamModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return BetterTeamConfigScreen::new;
	}
}
