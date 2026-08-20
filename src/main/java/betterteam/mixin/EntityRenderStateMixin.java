package betterteam.mixin;

import betterteam.client.CustomNameTagHolder;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public class EntityRenderStateMixin implements CustomNameTagHolder {
    @Unique
    private Component betterteam$customTag;
    @Unique
    private int betterteam$customColor;
    @Unique
    private int betterteam$backgroundColor;
    @Unique
    private boolean betterteam$seeThrough;
    @Unique
    private float betterteam$heightOffset;

    @Override
    public void betterteam$setCustomTag(Component tag, int color, int backgroundColor, boolean seeThrough, float heightOffset) {
        this.betterteam$customTag = tag;
        this.betterteam$customColor = color;
        this.betterteam$backgroundColor = backgroundColor;
        this.betterteam$seeThrough = seeThrough;
        this.betterteam$heightOffset = heightOffset;
    }

    @Override
    public void betterteam$clearCustomTag() {
        this.betterteam$customTag = null;
        this.betterteam$customColor = 0;
        this.betterteam$backgroundColor = 0;
        this.betterteam$seeThrough = false;
        this.betterteam$heightOffset = 0.0F;
    }

    @Override
    public Component betterteam$getCustomTag() {
        return this.betterteam$customTag;
    }

    @Override
    public int betterteam$getColor() {
        return this.betterteam$customColor;
    }

    @Override
    public int betterteam$getBackgroundColor() {
        return this.betterteam$backgroundColor;
    }

    @Override
    public boolean betterteam$isSeeThrough() {
        return this.betterteam$seeThrough;
    }

    @Override
    public float betterteam$getHeightOffset() {
        return this.betterteam$heightOffset;
    }
}
