package betterteam.client;

import betterteam.client.gui.BetterTeamConfigScreen;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;

public class BetterTeamModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return BetterTeamConfigScreen::new;
	}
}
