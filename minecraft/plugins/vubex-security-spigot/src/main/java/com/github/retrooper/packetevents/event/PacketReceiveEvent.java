/*
 * This file is part of packetevents - https://github.com/retrooper/packetevents
 * Copyright (C) 2022 retrooper and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.retrooper.packetevents.event;

import com.github.retrooper.packetevents.event.simple.*;
import com.github.retrooper.packetevents.exception.PacketProcessException;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.PacketSide;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.User;

public class PacketReceiveEvent extends ProtocolPacketEvent<Object> {
    protected PacketReceiveEvent(Object channel, User user, Object player, Object rawByteBuf,
                                 boolean autoProtocolTranslation) throws PacketProcessException {
        super(PacketSide.CLIENT, channel, user, player, rawByteBuf, autoProtocolTranslation);
    }

    protected PacketReceiveEvent(int packetID, PacketTypeCommon packetType,
                                 ServerVersion serverVersion,
                                 Object channel, User user, Object player,
                                 Object byteBuf) throws PacketProcessException {
        super(packetID, packetType, serverVersion,
                channel, user, player, byteBuf);
    }

    @Override
    public void call(PacketListenerCommon listener) {
        listener.onPacketReceive(this);
    }

    @Override
    public PacketReceiveEvent clone() {
        if (this instanceof PacketHandshakeReceiveEvent) {
            return ((PacketHandshakeReceiveEvent)this).clone();
        }
        else if (this instanceof PacketStatusReceiveEvent) {
            return ((PacketStatusReceiveEvent)this).clone();
        }
        else if (this instanceof PacketLoginReceiveEvent) {
            return ((PacketLoginReceiveEvent)this).clone();
        }
        else if (this instanceof PacketPlayReceiveEvent) {
            return ((PacketPlayReceiveEvent)this).clone();
        }
        else {
            return null;
        }
    }
}
