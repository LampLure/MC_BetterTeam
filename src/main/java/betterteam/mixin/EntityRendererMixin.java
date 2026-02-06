package betterteam.mixin;

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import betterteam.client.BetterTeamClient;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
	@Inject(method = "renderLabelIfPresent", at = @At("HEAD"), cancellable = true)
	private void betterteam$renderLabelIfPresent(S state, Text text, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
		if (state.entityType != EntityType.PLAYER) {
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
		String name = text.getString();
		boolean isMember = team.isMember(name);
		if (team.isWhitelist()) {
			if (!isMember) {
				return;
			}
		} else if (isMember) {
			return;
		}
		Vec3d pos = state.nameLabelPos;
		if (pos == null) {
			return;
		}
		boolean seeThrough = !state.sneaking;
		int yOffset = ("deadmau5".equals(name) ? -10 : 0) - 2;
		matrices.push();
		matrices.translate(pos.x, pos.y, pos.z);
		matrices.multiply(MinecraftClient.getInstance().getEntityRenderDispatcher().getRotation());
		matrices.scale(0.025F, -0.025F, 0.025F);
		Matrix4f matrix = matrices.peek().getPositionMatrix();
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		Text label = Text.literal(name);
		int textWidth = textRenderer.getWidth(label);
		float x = -textWidth / 2.0F;
		int textAlpha = (int)(team.getNameTextOpacity() * 255.0F) << 24;
		int backgroundAlpha = (int)(team.getNameBackgroundOpacity() * 255.0F) << 24;
		int textColor = textAlpha | (team.getNameTextColorInt() & 0x00FFFFFF);
		int background = backgroundAlpha | (team.getNameBackgroundColorInt() & 0x00FFFFFF);
		textRenderer.draw(label, x, yOffset, textColor, false, matrix, vertexConsumers, seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, background, light);
		if (seeThrough) {
			textRenderer.draw(label, x, yOffset, textColor, false, matrix, vertexConsumers, TextRenderer.TextLayerType.NORMAL, 0, LightmapTextureManager.applyEmission(light, 2));
		}
		matrices.pop();
		ci.cancel();
	}
}
