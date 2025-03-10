通常一般键值数据我们使用SharePreference存储, 但是这样很麻烦且性能低.

为什么字段只能存在于内存中而不是直接映射到本地磁盘呢? 这个时候就可以使用本库的序列化功能创建一个`存在于磁盘的字段`. <br>
他的赋值和读值都会映射到磁盘中(这在程序编码中称为序列化)

> 请一定要阅读文章最后一章: [字段增删](#_10). 以保证数据安全性

## 使用

### 创建序列化字段

序列化字段即读写会自动映射到本地磁盘的字段(或者称为自动序列化字段)
> 框架内部使用腾讯的[MMKV](https://github.com/Tencent/MMKV)实现, 因为其比SharePreference/SQLite速度快多, 可以有效解决ANR.
> 关系型/列表数据/大体积数据还是推荐使用数据库完成

使用MMKV请初始化
```kotlin
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        // MMKV.initialize(cacheDir.absolutePath, MMKVLogLevel.LevelInfo) // 存储路径, [LevelNone] 即不输出日志
    }
}
```

```kotlin
private var name: String by serial()

// 第一个参数是默认值, 第二个是键名(默认使用的是字段名称作为存储时的键名)
private var simple: String by serial("默认值", "自定义键名")
```

之后这个字段读写都会自动读取和写入到本地磁盘

```kotlin
name = "吴彦祖" // 写入到本地磁盘

Log.d("日志", "name = ${name}") // 读取自本地磁盘
```

动态键名, 即键名包含变量

```kotlin
private var userId :String by serialLazy()
private var newMessage :Boolean by serial(name = "new_message_$userId")
```

> 不同类创建同名的序列化字段在本地磁盘中属于不同数据, 实际存储使用的名称为: `"${全类名}.${你指定的键名}"` <br>
> 比如`com.drake.serialize.sample.MainActivity.name`



## 支持类型
基本上支持任何类型, 故非关系型数据推荐直接使用Serialize而不是数据库存储

| 类型 | 描述 |
|-|-|
| 任何集合 | 集合的泛型自己注意匹配正确, 否则会get时抛出`ClassCastException`类型转换异常, 官方也是如此 |
| 任何基础类型 | 基础类型如果在不指定默认值情况下读取不到会返回Null(如果你字段不是可空类型(即`?`)则会引发崩溃), 为符合Kotlin而设计 |
| Serializable | 实现Serializable的类 |
| Parcelable |  实现Parcelable的类 |

## 可空字段

字段可以选择声明为可空和不可空类型, 即是否添加`?`符号. <br>
假设字段在本地不存在而你未添加`?`并且也没有设置默认值, 则会导致抛出空指针异常, 而添加`?`则仅仅会返回null

```kotlin
private var name: String? by serial()
```
这时如果本地不存在`name`的话则返回`null`

## LiveData
通过创建一个可观察的字段. 对其读写也会自动映射到本地磁盘(同时使用异步写入保证性能)

```kotlin
private val liveData by serialLiveData("默认值")
```

每次写入都会回调观察者`observe`
```kotlin
liveData.observe(this) {
    toast("观察到本地数据: $it")
}
```
## 懒加载
懒加载即只在第一次读取字段的时候才会从本地磁盘读取, 后续都是从内存读取. 完美解决ANR. 主线程上亿次读写都没问题.

这是为了避免反复从磁盘读取造成性能耗时. 使用场景譬如是否第一次启动应用/频繁读取的用户Id.

```kotlin
private var model: ModelSerializable by serialLazy() // 懒加载
```
> 重新赋值字段还是会同时更新内存和磁盘中的值



## 使用函数读写
直接通过函数手动存储键值. 无需创建字段.

```kotlin
serialize("name" to "吴彦祖") // 写

val name:String = deserialize("name") // 读
val nameB:String = deserialize("name", "默认值") // 假设读取失败返回默认值
```

## 数据类

每个应用可能都存在存储应用配置的本地数据, 这里非常推荐使用Serialize. 关系型数据/列表数据/大体积数据还是推荐使用数据库完成

由于每个类拥有的`序列化字段`并不是共用同一份数据. 那么当我们想要在任何地方访问本地同一数据则应当使用以下方式

1. 创建`object`单例类
2. 使用函数序列化(上面已介绍)

<br>
创建单例类

```kotlin
object AppConfig {
   var isFirstLaunch:String? by serial()
}
```

使用

```kotlin
AppConfig.isFirstLaunch
```

## 指定存储目录/日志等级

如果需要自定义所有序列化字段默认的存储目录或者日志输出等级, 使用MMKV进行初始化(可选操作).

=== "全局默认"
    ```kotlin
    class App : Application() {

        override fun onCreate() {
            super.onCreate()
            MMKV.initialize(cacheDir.absolutePath, MMKVLogLevel.LevelInfo) // 参数1是设置路径路径字符串, [LevelNone] 即不输出日志
        }
    }
    ```

=== "指定字段"
    ```kotlin
    private var name: String by serial(kv = MMKV.mmkvWithID("User"))
    ```

=== "手动序列化"
    ```kotlin
    MMKV.mmkvWithID("User").serialize("name" to "吴彦祖")
    MMKV.mmkvWithID("User").deserialize("name")
    ```

## 清除数据

清除数据有两种方法

1. 字段清除很简单, 赋值为null即可
    ```kotlin
    private var userId :String by serialLazy()
    userId = null

    // 为创建字段也可以赋值为null清除
    serialize("model" to null)
    ```


2. 使用你指定的MMKV实例删除, 假设你未指定过MMKV实例即为`MMKV.defaultMMKV()`
    ```kotlin
    MMKV.defaultMMKV()?.remove("指定删除的字段名")
    MMKV.defaultMMKV()?.clearAll()
    ```

## 对象新增字段
如果你存储对象到磁盘中, 那么就需要注意如果对象后面新增或者删除某个字段可能会导致无法读取原有对象

### Serializable

- 创建一个伴生对象字段`serialVersionUUID`可解决该问题<br>
- 但是新增的字段默认值将为零值而不是你声明的默认值(比如String为null/Int为0) <br>
- 可以在IDE的Plugins搜索 `Kotlin serialVersionUID generator` 安装插件快捷键自动生成唯一的UUID

```kotlin
data class SerializableModel(var name: String = "ModelSerializable", var age:Int = 11) : Serializable {
    companion object {
        private const  val serialVersionUID = -7L
    }
}
```

### Parcelable
如果Serializable新增字段如果有默认值. 实际上并不会生效. 这个时候建议实现Parcelable而不是Serializable. 其可以保证默认值效果
```kotlin
data class ParcelableModel(var name: String = "ModelParcelable") : Parcelable {

    constructor(parcel: Parcel) : this(parcel.readString() ?: "ModelParcelable") // 读取空则赋值默认值

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ParcelableModel> {
        override fun createFromParcel(parcel: Parcel): ParcelableModel {
            return ParcelableModel(parcel)
        }

        override fun newArray(size: Int): Array<ParcelableModel?> {
            return arrayOfNulls(size)
        }
    }
}
```
将光标放到`Parcelable`类名上使用Alt+Enter可以快速实现(Add Parcelable Implementation)


### 包名/类名/字段名变更
包名/类名/字段名变更都会导致本地序列化对象的字段key变更(因为默认key名称生成原则就是全路径类名+字段名). 导致无法读取上次打开应用存储的旧值, 除非手动指定字段key

```kotlin
private var name: String by serial(name = "unique_name")
```

## 覆盖值

示例
```kotlin
object UserConfig {
    var userData:UserData by serial()
}
```
有时候你需要修改对象UserData里面的字段, 然后再次保存到本地.br

你可能会这么写

```kotlin
UserConfig.userData.name = "new name"
UserConfig.userData = UserConfig.userData
```
实际上这是无效的, 因为`UserConfig.userData.name = "new name"`并没有将对象里面的字段映射到本地.

解决办法就是使用临时变量

```kotlin
val userData = UserConfig.userData
userData.name = "new name"
UserConfig.userData = userData
```