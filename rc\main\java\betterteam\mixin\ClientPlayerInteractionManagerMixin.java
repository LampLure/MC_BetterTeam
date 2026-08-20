package betterteam.mixin;

import betterteam.client.BetterTeamClient;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void betterteam$preventFriendlyFire(Player player, Entity target, CallbackInfo ci) {
		BetterTeamConfig config = BetterTeamClient.getConfig();
		if (config == null) {
			return;
		}
		TeamConfig team = config.getActiveTeam();
		if (team == null || !team.preventFriendlyFire) {
			return;
		}
		if (target instanceof Player targetPlayer) {
			String name = targetPlayer.getName().getString();
			boolean isMember = team.isMember(name);
			boolean isFriend = team.isWhitelist() ? isMember : !isMember;
			if (isFriend) {
				ci.cancel();
			}
			return;
		}
		for (Entity passenger : target.getPassengers()) {
			if (passenger instanceof Player passengerPlayer) {
				String name = passengerPlayer.getName().getString();
				boolean isMember = team.isMember(name);
				boolean isFriend = team.isWhitelist() ? isMember : !isMember;
				if (isFriend) {
					ci.cancel();
					return;
				}
			}
		}
	}
}
