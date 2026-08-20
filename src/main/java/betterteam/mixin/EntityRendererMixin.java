package betterteam.mixin;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    private void testForceAllPlayersGreen(T entity, S state, float tickDelta, CallbackInfo ci) {
        // 1. 只针对玩家实体
        if (entity instanceof PlayerEntity player) {
            
            // 2. 强制使用高亮绿色 (Minecraft 标准亮绿 0x55FF55)
            TextColor greenColor = TextColor.fromRgb(0x55FF55);
            
            // 3. 强制覆盖 nameTag 内容和样式
            state.nameTag = Text.literal(player.getName().getString())
                    .setStyle(Style.EMPTY.withColor(greenColor).withBold(true));

            // 4. 解决下蹲（Sneaking）玩家和默认隐藏问题：强制注入挂载坐标
            // 如果原版因玩家潜行将 attachment 置为 null，渲染器会直接忽略，这里强制补充
            if (state.nameTagAttachment == null) {
                // 如果映射字段中存在 NAME_TAG 挂载点
                try {
                    state.nameTagAttachment = player.getAttachments().getNullable(
                            EntityAttachmentType.NAME_TAG, 0, player.getYRotation()
                    );
                } catch (Throwable ignored) {
                }

                // 保底 fallback：如果获取不到挂载点，直接给头部上方一个固定偏移向量
                if (state.nameTagAttachment == null) {
                    state.nameTagAttachment = new Vec3d(0.0, player.getHeight() + 0.5, 0.0);
                }
            }
        }
    }
}
