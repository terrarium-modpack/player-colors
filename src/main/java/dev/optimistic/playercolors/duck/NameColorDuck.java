package dev.optimistic.playercolors.duck;

import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public interface NameColorDuck {
    @Nullable
    Formatting player_colors$getColor();

    void player_colors$setColor(@Nullable Formatting newColor);
}