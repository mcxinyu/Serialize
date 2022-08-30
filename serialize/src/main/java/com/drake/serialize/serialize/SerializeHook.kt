package com.drake.serialize.serialize

import kotlin.reflect.KType

interface SerializeHook {

    fun <T> serialize(kType: KType, data: T): ByteArray

    fun <T> deserialize(kType: KType, byteArray: ByteArray): T
}