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

package com.drake.serialize.sample.base

import android.app.Application
import com.tencent.mmkv.MMKV

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        // 可选的初始化配置
        MMKV.initialize(this)
        // MMKV.initialize(cacheDir.absolutePath, MMKVLogLevel.LevelInfo) // 参数1: 存储路径, 参数2: 日志等级
    }
}