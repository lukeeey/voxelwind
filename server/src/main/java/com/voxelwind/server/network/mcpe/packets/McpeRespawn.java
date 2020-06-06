package com.voxelwind.server.network.mcpe.packets;

import com.flowpowered.math.vector.Vector3f;
import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.mcpe.McpeUtil;
import io.netty.buffer.ByteBuf;
import lombok.Data;

@Data
public class McpeRespawn implements NetworkPackage {
    private Vector3f position;
    private State state;
    private long entityId;

    @Override
    public void decode(ByteBuf buffer) {
        position = McpeUtil.readVector3f(buffer);
        state = State.values()[buffer.readByte()];
        entityId = Varints.decodeUnsigned(buffer);
    }

    @Override
    public void encode(ByteBuf buffer) {
        McpeUtil.writeVector3f(buffer, position);
        buffer.writeByte(state.ordinal());
        Varints.encodeUnsigned(buffer, entityId);
    }

    public enum State {
        SEARCHING,
        SERVER_READY,
        CLIENT_READY
    }
}
