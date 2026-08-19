package betterteam.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import betterteam.client.BetterTeamClient;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
	/**
	 * 在 EntityRenderer.extractRenderState 返回后，强制为队员玩家设置 nameTag 和
	 * nameTagAttachment。
	 *
	 * MC 26.x 渲染管线中，玩家名称标签只有在 shouldShowName 返回 true 时才会被填充
	 * (state.nameTag != null)。而普通玩家默认 shouldShowName 返回 false，导致名称
	 * 标签根本不会渲染。这里通过在 extractRenderState 返回后强制注入 nameTag 来
	 * 绕过这个限制，让队伍成员的名称标签始终显示。
	 *
	 * 注意：必须注入到 EntityRenderer.extractRenderState 而不是 AvatarRenderer，因为
	 * AvatarRenderer.extractRenderState 会调用 super.extractRenderState()，这里才是
	 * nameTag 被设置或清除的地方。也不能注入到 submitNameDisplay，因为 AvatarRenderer
	 * 重写了它，玩家走的是 AvatarRenderer.submitNameDisplay 而非 EntityRenderer 的版本。
	 *
	 * 文字颜色通过 Component.style.withColor(TextColor.fromRgb(argb)) 设置。
	 * Font.drawInBatch(Component, ..., int defaultColor, ...) 中的 defaultColor 参数
	 * 只是后备颜色，当 Component style 指定了颜色时会使用 style 的颜色，所以这里的
	 * 自定义颜色会覆盖 NameTagFeatureRenderer 中固定的默认颜色 (0x80FFFFFF)。
	 */
	@Inject(
		method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V",
		at = @At("RETURN")
	)
	private void betterteam$forceTeamNameTag(T entity, S state, float partialTicks, CallbackInfo ci) {
		if (state.entityType != EntityType.PLAYER) {
			return;
		}
		if (!(entity instanceof Player player)) {
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
		String name = player.getName().getString();
		boolean isMember = team.isMember(name);
		boolean shouldColor = team.isWhitelist() ? isMember : !isMember;
		if (!shouldColor) {
			return;
		}

		// 强制设置 nameTag，让名称标签显示出来（即使 shouldShowName 为 false）。
		Component displayName = player.getDisplayName();
		if (displayName == null) {
			displayName = Component.literal(name);
		}

		// 计算带 alpha 通道的 ARGB 颜色：alpha 来自文字透明度，RGB 来自文字颜色。
		int textColorRgb = team.getNameTextColorInt() & 0x00FFFFFF;
		float textOpacity = team.getNameTextOpacity();
		int alpha = (int) (textOpacity * 255.0F) & 0xFF;
		int argbColor = (alpha << 24) | textColorRgb;

		// 用 copy() 避免修改原始 Component，再通过 withStyle 应用自定义颜色。
		state.nameTag = displayName.copy().withStyle(style -> style.withColor(TextColor.fromRgb(argbColor)));

		// 如果 nameTagAttachment 为 null，submitNameTag 内部会直接 return 不渲染，
		// 所以必须强制设置它。
		if (state.nameTagAttachment == null) {
			state.nameTagAttachment = entity.getAttachments().getNullable(
				EntityAttachment.NAME_TAG, 0, entity.getYRot(partialTicks)
			);
		}
	}
}
