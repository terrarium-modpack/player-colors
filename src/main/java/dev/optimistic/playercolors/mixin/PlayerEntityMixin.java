package dev.optimistic.playercolors.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.optimistic.playercolors.duck.NameColorDuck;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
  @WrapOperation(method = "getDisplayName", at = @At(value = "INVOKE", target = "Lnet/minecraft/scoreboard/Team;decorateName(Lnet/minecraft/scoreboard/AbstractTeam;Lnet/minecraft/text/Text;)Lnet/minecraft/text/MutableText;"))
  private MutableText getDisplayName$Team$decorateName(AbstractTeam team, Text name, Operation<MutableText> original) {
    if (!(((PlayerEntity) (Object) this) instanceof ServerPlayerEntity serverPlayerEntity)) return original.call(team, name);
    Formatting playerColor = ((NameColorDuck) serverPlayerEntity).player_colors$getColor();
    return playerColor == null ? original.call(team, name) : name.copy().styled(style -> style.withColor(playerColor));
  }
}
