package ink.ptms.adyeshach.impl.manager

import ink.ptms.adyeshach.core.Adyeshach
import ink.ptms.adyeshach.impl.DefaultAdyeshachAPI
import ink.ptms.adyeshach.impl.DefaultAdyeshachBooster
import ink.ptms.adyeshach.impl.entity.DefaultEntityInstance
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.LifeCycle
import taboolib.common.function.throttle
import taboolib.common.io.newFile
import taboolib.common.platform.Awake
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.function.warning
import taboolib.common.util.t
import taboolib.platform.util.onlinePlayers
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

/**
 * Adyeshach
 * ink.ptms.adyeshach.impl.manager.DefaultManagerHandler
 *
 * @author 坏黑
 * @since 2022/8/18 10:51
 */
@OptIn(ExperimentalTime::class)
internal object DefaultManagerHandler {

    // 当前游戏刻的玩家列表
    var playersInGameTick: Collection<Player> = listOf()

    // 是否首次触发（通常视为预热）
    var isFirstReport = true

    // 实体卡顿报告
    val entityReport = throttle<Player, Duration>(5000) { player, time ->
        warning(
            """
                处理玩家 ${player.name} 的私有实体管理器用时 $time!
                Processing the private entity manager for player ${player.name} took $time!
            """.t()
        )
        warning(
            """
                详细信息已输出到 dump/${player.name}.log 文件中, 请注意查看!
                Detailed information has been output to the dump/${player.name}.log file, please check it out!
            """.t()
        )
        val manager = DefaultAdyeshachAPI.playerEntityTemporaryManagerMap[player.name]!!
        warning("-- Manager Details --")
        warning("   Active entities: ${manager.activeEntity.size}")
        val passengers = manager.activeEntity.sumOf { (it as DefaultEntityInstance).passengers.size }
        warning("   Passengers: $passengers")
        warning("-- End of Manager Details --")
        // 输出到文件
        submitAsync {
            newFile(getDataFolder().resolve("dump/${player.name}.log")).writeText(
                buildString {
                    append("-- Manager Details --\n")
                    append("   Active entities: ${manager.activeEntity.size}\n")
                    append("   Passengers: $passengers\n")
                    append("   Entities: \n")
                    manager.activeEntity.forEach {
                        it as DefaultEntityInstance
                        append("     ${it.id} (${it.entityType}): ${it.getLocation()}\n")
                        append("     Passengers:")
                        val pt = measureTime {
                            it.passengers.forEach { p ->
                                val find = manager.getEntityByUniqueId(p)
                                if (find == null) {
                                    append("     - $p")
                                } else {
                                    append("     - $p (${find.id}, ${find.entityType}: ${find.getLocation()})")
                                }
                            }
                        }
                        append("     Passenger calculation time: $pt\n")
                    }
                    append("-- End of Manager Details --\n")
                }
            )
        }
    }

    @Awake(LifeCycle.ACTIVE)
    fun onActive() {
        // 公共管理器
        DefaultAdyeshachBooster.api.localPublicEntityManager.onEnable()
        // 私有管理器
        onlinePlayers.forEach { Adyeshach.api().setupEntityManager(it) }
        // 公共管理器
        submit(period = 1) {
            playersInGameTick = Bukkit.getOnlinePlayers()
            DefaultAdyeshachBooster.api.localPublicEntityManager.onTick()
            DefaultAdyeshachBooster.api.localPublicEntityManagerTemporary.onTick()
        }
        // 私有管理器
        submit(period = 1) {
            onlinePlayers.forEach { player ->
                val manager = DefaultAdyeshachAPI.playerEntityTemporaryManagerMap[player.name]
                if (manager != null) {
                    val time = measureTime { manager.onTick() }
                    // 如果处理这个玩家的时间超过 50ms 则在后台进行报告，报告周期为 5 秒 1 次。
                    if (time > 50.milliseconds) {
                        if (isFirstReport) {
                            isFirstReport = false
                        } else {
                            entityReport(player, time)
                        }
                    }
                }
            }
        }
        // 自动保存
        submitAsync(period = 1200, delay = 1200) {
            // 公共管理器
            DefaultAdyeshachBooster.api.localPublicEntityManager.onSave()
        }
    }

    @Awake(LifeCycle.DISABLE)
    fun onDisable() {
        // 公共管理器
        DefaultAdyeshachBooster.api.localPublicEntityManagerTemporary.onDisable()
        DefaultAdyeshachBooster.api.localPublicEntityManager.onDisable()
        DefaultAdyeshachBooster.api.localPublicEntityManager.onSave()
        // 私有管理器
        onlinePlayers.forEach { Adyeshach.api().releaseEntityManager(it, false) }
    }
}