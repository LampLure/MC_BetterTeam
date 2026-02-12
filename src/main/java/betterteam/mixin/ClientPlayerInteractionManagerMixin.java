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
	@Inject(method = "attackEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
	private void betterteam$preventFriendlyFire(PlayerEntity player, Entity target, CallbackInfo ci) {
		BetterTeamConfig config = BetterTeamClient.getConfig();
		if (config == null) {
			return;
		}
		TeamConfig team = config.getActiveTeam();
		if (team == null || !team.preventFriendlyFire) {
			return;
		}
		if (target instanceof PlayerEntity targetPlayer) {
			String name = targetPlayer.getName().getString();
			boolean isMember = team.isMember(name);
			boolean isFriend = team.isWhitelist() ? isMember : !isMember;
			if (isFriend) {
				ci.cancel();
			}
			return;
		}
		for (Entity passenger : target.getPassengerList()) {
			if (passenger instanceof PlayerEntity passengerPlayer) {
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
