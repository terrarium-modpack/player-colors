package dev.optimistic.playercolors.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.optimistic.playercolors.duck.NameColorDuck;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
  @WrapMethod(method = "respawnPlayer")
  private ServerPlayerEntity respawnPlayer(ServerPlayerEntity oldPlayer, boolean alive,
                                           Operation<ServerPlayerEntity> original) {
    ServerPlayerEntity newPlayer = original.call(oldPlayer, alive);
    ((NameColorDuck) newPlayer).player_colors$setColor(((NameColorDuck) oldPlayer).player_colors$getColor());
    return newPlayer;
  }
}
