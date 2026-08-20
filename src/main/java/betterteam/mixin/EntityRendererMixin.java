package betterteam.mixin;

import betterteam.client.BetterTeamClient;
import betterteam.client.CustomNameTagHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    private static final float BETTERTEAM_NAME_TAG_HEIGHT_OFFSET = 0.5F;

    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
    private void betterteam$forceCustomNameTagVisible(T entity, double distanceToCameraSq, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof Player player && betterteam$getMatchingTeam(player) != null) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void betterteam$extractCustomNameTag(T entity, S state, float tickDelta, CallbackInfo ci) {
        if (state instanceof CustomNameTagHolder holder) {
            holder.betterteam$clearCustomTag();
        }
        if (!(entity instanceof Player player) || !(state instanceof CustomNameTagHolder holder)) {
            return;
        }

        TeamConfig team = betterteam$getMatchingTeam(player);
        if (team == null) {
            return;
        }

        String rawDisplayName = player.getDisplayName().getString();
        if (state.nameTagAttachment == null) {
            try {
                state.nameTagAttachment = player.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, player.getYRot());
            } catch (Throwable ignored) {
            }
            if (state.nameTagAttachment == null) {
                state.nameTagAttachment = new Vec3(0.0, player.getBbHeight(), 0.0);
            }
        }

        holder.betterteam$setCustomTag(
                Component.literal(rawDisplayName),
                betterteam$withOpacity(team.getNameTextColorInt(), team.getNameTextOpacity()),
                betterteam$withOpacity(team.getNameBackgroundColorInt(), team.getNameBackgroundOpacity()),
                true,
                BETTERTEAM_NAME_TAG_HEIGHT_OFFSET
        );
    }

    @Inject(
            method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void betterteam$submitCustomNameTag(S state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState cameraState, CallbackInfo ci) {
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
        ci.cancel();
    }

    private static int betterteam$withOpacity(int rgb, float opacity) {
        int alpha = Math.round(Math.max(0.0F, Math.min(1.0F, opacity)) * 255.0F) & 0xFF;
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    private static TeamConfig betterteam$getMatchingTeam(Player player) {
        BetterTeamConfig config = BetterTeamClient.getConfig();
        if (config == null) {
            return null;
        }

        TeamConfig team = config.getActiveTeam();
        if (team == null) {
            return null;
        }

        return team.isMember(player.getDisplayName().getString()) ? team : null;
    }
}
