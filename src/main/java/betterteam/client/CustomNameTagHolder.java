package betterteam.client;

import net.minecraft.network.chat.Component;

public interface CustomNameTagHolder {
    void betterteam$setCustomTag(Component tag, int color, int backgroundColor, boolean seeThrough, float heightOffset);

    void betterteam$clearCustomTag();

    Component betterteam$getCustomTag();

    int betterteam$getColor();

    int betterteam$getBackgroundColor();

    boolean betterteam$isSeeThrough();

    float betterteam$getHeightOffset();
}
