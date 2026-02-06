package betterteam.mixin;

import betterteam.client.BetterTeamClient;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
	@Inject(method = "attackEntity", at = @At("HEAD"), cancellable = true)
	private void betterteam$preventFriendlyFire(PlayerEntity player, Entity target, CallbackInfo ci) {
		if (!(target instanceof PlayerEntity targetPlayer)) {
			return;
		}
		BetterTeamConfig config = BetterTeamClient.getConfig();
		if (config == null) {
			return;
		}
		TeamConfig team = config.getActiveTeam();
		if (team == null || !team.preventFriendlyFire) {
			return;
		}
		String name = targetPlayer.getName().getString();
		if (team.isMember(name)) {
			ci.cancel();
		}
	}
}
