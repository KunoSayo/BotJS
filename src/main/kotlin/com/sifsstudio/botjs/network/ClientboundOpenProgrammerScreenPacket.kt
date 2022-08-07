package com.sifsstudio.botjs.network

import net.minecraft.network.FriendlyByteBuf
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.network.NetworkEvent
import java.util.function.Supplier

data class ClientboundOpenProgrammerScreenPacket(
    val entityId: Int
) {
    companion object {
        fun encode(msg: ClientboundOpenProgrammerScreenPacket, buf: FriendlyByteBuf) = buf.apply {
            writeInt(msg.entityId)
        }

        fun decode(buf: FriendlyByteBuf) = ClientboundOpenProgrammerScreenPacket(buf.readInt())

        fun handle(msg: ClientboundOpenProgrammerScreenPacket, ctx: Supplier<NetworkEvent.Context>) {
            ctx.get().apply {
                enqueueWork {
                    DistExecutor.unsafeRunWhenOn(Dist.CLIENT) {
                        Runnable {
                            ClientPacketHandler.handleOpenProgrammerScreen(msg)
                        }
                    }
                }
                packetHandled = true
            }
        }
    }
}
