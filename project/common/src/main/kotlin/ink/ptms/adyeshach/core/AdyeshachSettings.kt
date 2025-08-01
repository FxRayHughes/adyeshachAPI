package ink.ptms.adyeshach.core

import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.util.resettableLazy
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigNode
import taboolib.module.configuration.Configuration
import java.io.File

/**
 * Adyeshach
 * ink.ptms.adyeshach.core.AdyeshachSettings
 *
 * @author 坏黑
 * @since 2022/6/16 16:29
 */
@ConfigNode(bind = "core/config.yml")
object AdyeshachSettings {

    @Awake(LifeCycle.ENABLE)
    fun migrate() {
        val file = File(getDataFolder(), "config.yml")
        if (file.exists()) {
            file.copyTo(File("core/config.yml"), true)
            file.delete()
        }
    }

    @Config("core/config.yml")
    lateinit var conf: Configuration
        private set

    /**
     * 调试模式
     */
    @ConfigNode("Settings.debug")
    var debug = false

    /**
     * 单位可视距离
     */
    @ConfigNode("Settings.visible-distance")
    var visibleDistance = 64.0

    /**
     * 主线程寻路
     */
    @ConfigNode("Settings.pathfinder-sync")
    var pathfinderSync = true

    /**
     * 在未知世界下删除单位
     */
    @ConfigNode("Settings.delete-file-in-unknown-world")
    var deleteFileInUnknownWorld = emptyList<String>()

    /**
     * 可视条件检查间隔
     */
    @ConfigNode("Settings.view-condition-interval")
    var viewConditionInterval = 40
        get() = if (field == 0) 40 else field

    /**
     * 可见范围的刷新周期
     */
    @ConfigNode("Settings.visible-refresh-interval")
    var visibleRefreshInterval = 10
        get() = if (field == 0) 10 else field

    /**
     * 单位生成时机
     * JOIN 表示玩家进入游戏时
     * KEEP_ALIVE 表示当玩家向服务端发送第一个心跳包时
     */
    val spawnTrigger by resettableLazy {
        try {
            SpawnTrigger.valueOf(conf.getString("Settings.spawn-event")!!.uppercase())
        } catch (ignored: Exception) {
            SpawnTrigger.KEEP_ALIVE
        }
    }

    /**
     * JOIN 延迟时间
     */
    @ConfigNode("Settings.spawn-delay")
    var spawnDelay = 0
        get() = if (field == 0) 20 else field

    /**
     * 是否为自动删除世界
     */
    fun isAutoDeleteWorld(world: String): Boolean {
        return deleteFileInUnknownWorld.any { if (it.endsWith("?")) world.contains(it.substring(0, it.length - 1)) else world == it }
    }
}

enum class SpawnTrigger {

    KEEP_ALIVE, JOIN
}

const val ADYESHACH_PREFIX = " §5§l‹ ›§f§7 "