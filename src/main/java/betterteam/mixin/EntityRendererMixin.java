package betterteam.mixin;

import betterteam.client.BetterTeamClient;
import betterteam.client.CustomNameTagHolder;
import betterteam.config.BetterTeamConfig;
import betterteam.config.TeamConfig;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {
    private static final float BETTERTEAM_NAME_TAG_HEIGHT_OFFSET = 0.5F;

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void betterteam$extractCustomNameTag(T entity, S state, float tickDelta, CallbackInfo ci) {
        if (state instanceof CustomNameTagHolder holder) {
            holder.betterteam$clearCustomTag();
        }
        if (!(entity instanceof Player player) || !(state instanceof CustomNameTagHolder holder)) {
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

        String rawDisplayName = player.getDisplayName().getString();
        if (!team.isMember(rawDisplayName)) {
            return;
        }

        state.nameTag = null;
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

    private static int betterteam$withOpacity(int rgb, float opacity) {
        int alpha = Math.round(Math.max(0.0F, Math.min(1.0F, opacity)) * 255.0F) & 0xFF;
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }
}
