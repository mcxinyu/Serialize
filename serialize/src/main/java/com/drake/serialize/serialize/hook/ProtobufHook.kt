package com.drake.serialize.serialize.hook

import com.drake.serialize.serialize.SerializeHook
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.KType

class ProtobufHook : SerializeHook {

    override fun <T> serialize(kType: KType, data: T): ByteArray {
        return ProtoBuf.encodeToByteArray(ProtoBuf.serializersModule.serializer(kType), data)
    }

    override fun <T> deserialize(kType: KType, byteArray: ByteArray): T {
        return ProtoBuf.decodeFromByteArray(ProtoBuf.serializersModule.serializer(kType), byteArray) as T
    }
}