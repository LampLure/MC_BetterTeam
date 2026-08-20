package betterteam.mixin;

import betterteam.client.CustomNameTagHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public abstract class AvatarRendererMixin {
    @Inject(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("TAIL")
    )
    private void betterteam$submitCustomNameTag(AvatarRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
        if (!(state instanceof CustomNameTagHolder holder)) {
            return;
        }
        if (holder.betterteam$getCustomTag() == null) {
            return;
        }

        Vec3 attachment = state.nameTagAttachment != null
                ? state.nameTagAttachment
                : new Vec3(0.0, state.boundingBoxHeight, 0.0);
        FormattedCharSequence text = holder.betterteam$getCustomTag().getVisualOrderText();
        Font font = Minecraft.getInstance().font;

        poseStack.pushPose();
        poseStack.translate(attachment.x, attachment.y + holder.betterteam$getHeightOffset(), attachment.z);
        poseStack.mulPose(cameraState.orientation);
        poseStack.scale(-0.025F, -0.025F, 0.025F);

        float xOffset = -font.width(text) / 2.0F;
        collector.submitText(
                poseStack,
                xOffset,
                0.0F,
                text,
                false,
                holder.betterteam$isSeeThrough() ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL,
                state.lightCoords,
                holder.betterteam$getColor(),
                holder.betterteam$getBackgroundColor(),
                0
        );
        poseStack.popPose();
    }
}
