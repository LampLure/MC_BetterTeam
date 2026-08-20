package betterteam.mixin;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
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

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void testForceAllPlayersGreen(T entity, S state, float tickDelta, CallbackInfo ci) {
        // 1. 只针对玩家实体
        if (entity instanceof Player player) {
            
            // 2. 强制使用高亮绿色 (0x55FF55)
            TextColor greenColor = TextColor.fromRgb(0x55FF55);
            
            // 3. 强制覆盖 nameTag 文本并加粗变绿
            state.nameTag = Component.literal(player.getName().getString())
                    .setStyle(Style.EMPTY.withColor(greenColor).withBold(true));

            // 4. 强制设置挂载点，即使玩家下蹲或距离过远也能挂载
            if (state.nameTagAttachment == null) {
                try {
                    state.nameTagAttachment = player.getAttachments().getNullable(
                            EntityAttachment.NAME_TAG, 0, player.getYRot()
                    );
                } catch (Throwable ignored) {
                }

                // 兜底偏移向量
                if (state.nameTagAttachment == null) {
                    state.nameTagAttachment = new Vec3(0.0, player.getBbHeight() + 0.5, 0.0);
                }
            }
        }
    }
}
