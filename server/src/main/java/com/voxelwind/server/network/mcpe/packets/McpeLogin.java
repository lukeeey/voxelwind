package com.voxelwind.server.network.mcpe.packets;

import com.voxelwind.nbt.util.Varints;
import com.voxelwind.server.network.mcpe.McpeUtil;
import com.voxelwind.server.network.NetworkPackage;
import com.voxelwind.server.network.util.CompressionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import lombok.Data;

import java.util.zip.DataFormatException;

@Data
public class McpeLogin implements NetworkPackage {
    private int protocolVersion; // = 91
    private byte gameEdition;
    private String chainData;
    private String skinData;

    @Override
    public void decode(ByteBuf buffer) {
        protocolVersion = buffer.readInt();
        if (protocolVersion != 91) {
            return;
        }
        gameEdition = buffer.readByte();
        int bodyLength = Varints.decodeUnsigned(buffer);
        ByteBuf body = buffer.readSlice(bodyLength);

        // Decompress the body
        ByteBuf result = null;
        try {
            result = CompressionUtil.inflate(body);
            chainData = McpeUtil.readLELengthString(result);
            skinData = McpeUtil.readLELengthString(result);
        } catch (DataFormatException e) {
            throw new RuntimeException("Unable to inflate login data body", e);
        } finally {
            if (result != null) {
                result.release();
            }
        }
    }

    @Override
    public void encode(ByteBuf buffer) {
        buffer.writeInt(protocolVersion);
        buffer.writeByte(gameEdition);

        ByteBuf body = PooledByteBufAllocator.DEFAULT.directBuffer();
        try {
            McpeUtil.writeLELengthString(body, chainData);
            McpeUtil.writeLELengthString(body, skinData);

            ByteBuf compressed = CompressionUtil.deflate(body);

            Varints.encodeUnsigned(buffer, compressed.readableBytes());
            buffer.writeBytes(compressed);
        } catch (DataFormatException e) {
            throw new RuntimeException("Unable to compress login data body", e);
        } finally {
            body.release();
        }
    }
}
