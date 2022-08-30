package com.drake.serialize.serialize

import android.os.Parcelable
import com.tencent.mmkv.MMKV
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

@PublishedApi
internal object MMKVUtils {

    fun serialize(kv: MMKV, vararg params: Pair<String, Any?>) {
        params.forEach {
            val name = it.first
            when (val value = it.second) {
                null -> kv.remove(it.first)
                // Scalars
                is Boolean -> kv.encode(name, value)
                is Byte -> kv.encode(name, value.toInt())
                is Char -> kv.encode(name, value.toInt())
                is Double -> kv.encode(name, value)
                is Float -> kv.encode(name, value)
                is Int -> kv.encode(name, value)
                is Long -> kv.encode(name, value)
                is Short -> kv.encode(name, value.toInt())

                // References
                is CharSequence -> kv.encode(name, value.toString())
                is Parcelable -> kv.encode(it.first, value)

                // Scalar arrays
                is BooleanArray -> kv.encode(name, getBytes(value))
                is ByteArray -> kv.encode(name, value)
                is CharArray -> kv.encode(name, getBytes(value))
                is DoubleArray -> kv.encode(name, getBytes(value))
                is FloatArray -> kv.encode(name, getBytes(value))
                is IntArray -> kv.encode(name, getBytes(value))
                is LongArray -> kv.encode(name, getBytes(value))
                is ShortArray -> kv.encode(name, getBytes(value))
                else -> kv.encode(name, getBytes(value))
            }
            return@forEach
        }
    }

    fun <T> deserialize(kv: MMKV, clazz: Class<T>, name: String): T {
        val r = when (clazz) {
            // Scalars
            Boolean::class.java -> kv.decodeBool(name)
            Byte::class.java -> kv.decodeBool(name)
            Char::class.java -> kv.decodeBool(name)
            Double::class.java -> kv.decodeDouble(name)
            Float::class.java -> kv.decodeFloat(name)
            Int::class.java -> kv.decodeInt(name)
            Long::class.java -> kv.decodeLong(name)
            Short::class.java -> kv.decodeInt(name)

            // References
            CharSequence::class.java -> kv.decodeBytes(name)
            Parcelable::class.java -> kv.decodeParcelable(name, clazz)

            // Scalar arrays
            BooleanArray::class.java -> kv.decodeBytes(name)
            ByteArray::class.java -> kv.decodeBytes(name)
            CharArray::class.java -> kv.decodeBytes(name)
            DoubleArray::class.java -> kv.decodeBytes(name)
            FloatArray::class.java -> kv.decodeBytes(name)
            IntArray::class.java -> kv.decodeBytes(name)
            LongArray::class.java -> kv.decodeBytes(name)
            ShortArray::class.java -> kv.decodeBytes(name)
            else -> kv.decodeBytes(name)
        }
        return r as T
    }

    fun <T> deserialize(kv: MMKV, clazz: Class<T>, name: String, defValue: T): T {
        return try {
            deserialize(kv, clazz, name)
        } catch (e: Exception) {
            defValue
        }
    }


    fun getBytes(obj: Any): ByteArray {
        ByteArrayOutputStream().use { byteOutput ->
            ObjectOutputStream(byteOutput).use { objOutput ->
                objOutput.writeObject(obj)
                return byteOutput.toByteArray()
            }
        }
    }

    fun getObject(bytes: ByteArray): Any? {
        ByteArrayInputStream(bytes).use { byteInput ->
            ObjectInputStream(byteInput).use { objInput ->
                return objInput.readObject()
            }
        }
    }
}