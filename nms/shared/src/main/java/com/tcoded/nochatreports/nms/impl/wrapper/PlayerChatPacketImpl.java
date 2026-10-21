package com.tcoded.nochatreports.nms.impl.wrapper;

import com.tcoded.nochatreports.nms.impl.VersionCompat;
import com.tcoded.nochatreports.nms.wrapper.PlayerChatPacket;
import com.tcoded.nochatreports.nms.wrapper.SystemChatPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class PlayerChatPacketImpl implements PlayerChatPacket {

    private final ClientboundPlayerChatPacket packet;

    public PlayerChatPacketImpl(ClientboundPlayerChatPacket packet) {
        this.packet = packet;
    }

    public PlayerChatPacketImpl(ByteBuf byteBuf) {
        this(VersionCompat.decodePlayerChat(byteBuf));
    }

    public SystemChatPacket toSystem() {
        try {
            // Get the content of the message
            Component content = VersionCompat.unsignedContent(packet);
            if (content == null) content = Component.literal(packet.body().content());

            // Apply formatting to the content
            Component formattedContent = VersionCompat.decorate(packet, content);

            // Create a new system chat packet
            ClientboundSystemChatPacket systemChatPacket = new ClientboundSystemChatPacket(formattedContent, false);
            return new SystemChatPacketImpl(systemChatPacket);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
