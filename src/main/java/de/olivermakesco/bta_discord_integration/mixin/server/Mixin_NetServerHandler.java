package de.olivermakesco.bta_discord_integration.mixin.server;

import de.olivermakesco.bta_discord_integration.server.DiscordChatRelay;
import net.minecraft.server.entity.player.EntityPlayerMP;
import net.minecraft.core.net.ChatEmotes;
import net.minecraft.server.net.handler.NetServerHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = NetServerHandler.class, remap = false)
public class Mixin_NetServerHandler {
    @Shadow private EntityPlayerMP playerEntity;

    @Redirect(
            method = "handleChat",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/net/ChatEmotes;process(Ljava/lang/String;)Ljava/lang/String;"

            )
    )
    String redirectChatHandle(String s) {
        String message = ChatEmotes.process(s);

        if (!de.olivermakesco.bta_discord_integration.config.BTADiscordIntegrationConfig.discord_enable) {
            return message;
        }

        String username = playerEntity.username;

        DiscordChatRelay.sendToDiscord(username, message);

        return message;
    }

    @Inject(
            method = "handleErrorMessage",
            at = @At("HEAD")
    )
    void sendLeaveMessage(String s, Object[] aobj, CallbackInfo ci) {
        String username = playerEntity.username;
        DiscordChatRelay.sendJoinLeaveMessage(username, false);
    }
}
