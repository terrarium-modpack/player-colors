package dev.optimistic.playercolors;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import dev.optimistic.playercolors.duck.NameColorDuck;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.ColorArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ServerEntrypoint implements DedicatedServerModInitializer, CommandRegistrationCallback {
    private static final Text REMOVED_COLOR_OVERRIDE = Text.literal("Unset color override.");
    private static final Map<Formatting, Text> UPDATE_MESSAGES;

    static {
        Formatting[] nonModifierFormats = Arrays.stream(Formatting.values()).filter(formatting -> !formatting.isModifier()).toArray(Formatting[]::new);
        Map<Formatting, Text> map = new IdentityHashMap<>(nonModifierFormats.length);

        Text message = Text.literal("Set color override to ");
        Text period = Text.literal(".");

        for (Formatting code : nonModifierFormats) {
            map.put(code, Text.empty()
                    .append(message)
                    .append(Text.literal(code.getName()).styled(style -> style.withColor(code)))
                    .append(period)
            );
        }

        UPDATE_MESSAGES = Collections.unmodifiableMap(map);
    }

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(this);
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess,
                         CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(
                CommandManager.literal("playercolors")
                        .requires(ServerCommandSource::isExecutedByPlayer)
                        .then(CommandManager.literal("unset")
                                .executes(context -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    assert player != null;
                                    ((NameColorDuck) player).player_colors$setColor(null);
                                    context.getSource().sendMessage(REMOVED_COLOR_OVERRIDE);
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(
                                CommandManager.literal("set")
                                        .then(CommandManager.argument("color", ColorArgumentType.color())
                                                .executes(context -> {
                                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                                    assert player != null;
                                                    Formatting color = ColorArgumentType.getColor(context, "color");
                                                    ((NameColorDuck) player).player_colors$setColor(color);
                                                    context.getSource().sendMessage(UPDATE_MESSAGES.get(color));
                                                    return Command.SINGLE_SUCCESS;
                                                })
                                        )
                        )
        );
    }
}
