package dev.optimistic.playercolors.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.optimistic.playercolors.duck.NameColorDuck;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin implements NameColorDuck {
  @Unique
  private static final String NBT_KEY = "playercolors$color";
  @Shadow
  @Final
  public MinecraftServer server;
  @Unique
  private @Nullable Formatting color = null;
  @Unique
  private @Nullable Text playerListName;

  @Unique
  private void updateColor(@Nullable Formatting newColor) {
    color = newColor;
    if (color == null) {
      playerListName = null;
    } else {
      playerListName = ((PlayerEntity) (Object) this).getName().copy().styled(style -> style.withColor(color));
    }
  }

  @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
  private void readCustomDataFromNbt(NbtCompound nbt, CallbackInfo ci) {
    if (nbt.contains(NBT_KEY, NbtElement.INT_TYPE)) updateColor(Formatting.byColorIndex(nbt.getInt(NBT_KEY)));
  }

  @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
  private void writeCustomDataToNbt(NbtCompound nbt, CallbackInfo ci) {
    if (color != null) nbt.putInt(NBT_KEY, color.getColorIndex());
  }

  @Override
  public @Nullable Formatting player_colors$getColor() {
    return color;
  }

  @Override
  public void player_colors$setColor(@Nullable Formatting newColor) {
    if (newColor != null && (newColor.isModifier() || !newColor.isColor())) return;

    updateColor(newColor);
    server.getPlayerManager().sendToAll(new PlayerListS2CPacket(PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME, (ServerPlayerEntity) (Object) this));
  }

  @WrapMethod(method = "getPlayerListName")
  private Text getPlayerListName(Operation<Text> original) {
    return playerListName == null ? original.call() : playerListName;
  }
}
