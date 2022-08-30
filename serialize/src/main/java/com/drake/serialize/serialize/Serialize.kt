/*
 * Copyright (C) 2018 Drake, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.drake.serialize.serialize

import com.tencent.mmkv.MMKV

object Serialize {
    /** 序列化/反序列化 */
    var hook: SerializeHook? = null
}

//<editor-fold desc="写入">

/**
 * 将键值数据序列化存储到磁盘
 */
fun serialize(vararg params: Pair<String, Any?>) {
    MMKV.defaultMMKV().serialize(*params)
}

/**
 * 将键值数据序列化存储到磁盘
 */
fun MMKV.serialize(vararg params: Pair<String, Any?>) {
    MMKVUtils.serialize(this, *params)
}
//</editor-fold>

//<editor-fold desc="读取">
/**
 * 根据[name]读取磁盘数据, 即使读取的是基础类型磁盘不存在的话也会返回null
 */
inline fun <reified T> deserialize(name: String): T {
    return MMKVUtils.deserialize(MMKV.defaultMMKV(), T::class.java, name)
}

/**
 * 根据[name]读取磁盘数据, 假设磁盘没有则返回[defValue]指定的默认值
 */
inline fun <reified T> deserialize(name: String, defValue: T): T {
    return MMKVUtils.deserialize(MMKV.defaultMMKV(), T::class.java, name, defValue)
}

/** 根据[name]读取磁盘数据, 即使读取的是基础类型磁盘不存在的话也会返回null */
inline fun <reified T> MMKV.deserialize(name: String): T {
    return MMKVUtils.deserialize(this, T::class.java, name)
}

/** 根据[name]读取磁盘数据, 假设磁盘没有则返回[defValue]指定的默认值 */
inline fun <reified T> MMKV.deserialize(name: String, defValue: T): T {
    return MMKVUtils.deserialize(this, T::class.java, name, defValue)
}
//</editor-fold>