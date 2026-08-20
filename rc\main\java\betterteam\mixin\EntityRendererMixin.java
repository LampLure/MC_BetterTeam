package betterteam.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import betterteam.client.BetterTeamClient;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.phys.Vec3;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
	@Unique
	private boolean betterteam$forceCurrentNameTagSeeThrough;

	@Inject(method = "finalizeRenderState", at = @At("TAIL"))
	private void betterteam$finalizeRenderState(T entity, S state, CallbackInfo ci) {
		betterteam$forceCurrentNameTagSeeThrough = false;
		if (state.entityType != EntityType.PLAYER || state.nameTag == null) {
			return;
		}
		BetterTeamConfig config = BetterTeamClient.getConfig();
		if (config == null) {
			return;
		}
		TeamConfig team = config.getActiveTeam();
		if (team == null) {
			return;
		}
		String name = state.nameTag.getString();
		boolean isMember = team.isMember(name);
		boolean shouldHighlight = team.isWhitelist() ? isMember : !isMember;
		if (!shouldHighlight) {
			return;
		}
		betterteam$forceCurrentNameTagSeeThrough = true;
		state.nameTag = Component.literal(name);
		Vec3 attachment = state.nameTagAttachment;
		if (attachment != null) {
			state.nameTagAttachment = attachment.add(0.0D, 0.25D, 0.0D);
		}
	}

	@ModifyArg(
		method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZIDLnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
			ordinal = 1
		),
		index = 4
	)
	private boolean betterteam$forceSeeThrough(boolean originalSeeThrough) {
		return originalSeeThrough || betterteam$forceCurrentNameTagSeeThrough;
	}

	private TeamConfig getActiveTeam() {
		BetterTeamConfig config = BetterTeamClient.getConfig();
		if (config == null) {
			return null;
		}
		return config.getActiveTeam();
	}

	private boolean shouldHighlight(TeamConfig team, String name) {
		if (name == null || name.isBlank()) {
			return false;
		}
		boolean isMember = team.isMember(name);
		return team.isWhitelist() ? isMember : !isMember;
	}
}
