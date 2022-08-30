package com.drake.serialize.serialize.hook

import com.drake.serialize.serialize.SerializeHook
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import kotlinx.serialization.serializer
import kotlin.reflect.KType

class JsonHook : SerializeHook {

    private val json = Json {
        ignoreUnknownKeys = true // JSON和数据模型字段可以不匹配
        coerceInputValues = true // 如果JSON字段是Null则使用默认值
    }

    override fun <T> serialize(kType: KType, data: T): ByteArray {
        return json.encodeToString(ProtoBuf.serializersModule.serializer(kType), data).toByteArray()
    }

    override fun <T> deserialize(kType: KType, byteArray: ByteArray): T {
        return json.decodeFromString(ProtoBuf.serializersModule.serializer(kType), byteArray.decodeToString()) as T
    }
}