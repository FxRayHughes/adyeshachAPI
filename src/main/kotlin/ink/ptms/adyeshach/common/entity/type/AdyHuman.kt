package ink.ptms.adyeshach.common.entity.type

import com.google.gson.annotations.Expose
import ink.ptms.adyeshach.api.AdyeshachAPI
import ink.ptms.adyeshach.api.event.AdyeshachGameProfileGenerateEvent
import ink.ptms.adyeshach.api.nms.NMS
import ink.ptms.adyeshach.common.bukkit.BukkitAnimation
import ink.ptms.adyeshach.common.bukkit.BukkitPose
import ink.ptms.adyeshach.common.bukkit.data.GameProfile
import ink.ptms.adyeshach.common.entity.ClientEntity
import ink.ptms.adyeshach.common.entity.EntityTypes
import ink.ptms.adyeshach.common.util.mojang.MojangAPI
import org.bukkit.entity.Player
import taboolib.common.platform.function.submit
import taboolib.module.chat.colored
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * @author sky
 * @since 2020-08-04 15:44
 */
class AdyHuman : AdyEntityLiving(EntityTypes.PLAYER) {

    private val playerUUID = UUID.randomUUID()

    @Expose
    private val gameProfile = GameProfile()

    @Expose
    private var isSleepingLegacy = false

    @Expose
    var isHideFromTabList = true
        set(value) {
            if (value) {
                forViewers { removePlayerInfo(it) }
            } else {
                forViewers { addPlayerInfo(it) }
            }
            field = value
        }

    init {
        /**
         * 1.15 -> 16
         * 1.14 -> 15
         * 1.10 -> 13
         * 1.9 -> 12
         */
//        mask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinCape", 0x01, true)
//        mask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinJacket", 0x02, true)
//        mask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinLeftSleeve", 0x04, true)
//        mask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinRightSleeve", 0x08, true)
//        mask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinLeftPants", 0x10, true)
//        mask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinRightPants", 0x20, true)
//        mask(at(11700 to 17, 11500 to 16, 11400 to 15, 11000 to 13, 10900 to 12), "skinHat", 0x40, true)
//        naturalEditor("isSleepingLegacy")
//                .reset { _, _ ->
//                    setSleeping(false)
//                }
//                .modify { player, entity, _ ->
//                    setSleeping(!isSleeping())
//                    entity.openEditor(player)
//                }
//                .display { _, _, _ ->
//                    isSleeping().toDisplay()
//                }
//        naturalEditor("isHideFromTabList")
//                .reset { _, _ ->
//                    isHideFromTabList = true
//                }
//                .modify { player, entity, _ ->
//                    isHideFromTabList = !isHideFromTabList
//                    entity.openEditor(player)
//                }
//                .display { _, _, _ ->
//                    isHideFromTabList.toDisplay()
//                }
//        naturalEditor("playerName")
//                .reset { _, _ ->
//                    setName("Adyeshach NPC")
//                }
//                .modify { player, entity, _ ->
//                    player.inputSign(arrayOf(getName(), "", "请在第一行输入内容")) {
//                        if (it[0].isNotEmpty()) {
//                            val name = "${it[0]}${it[1]}"
//                            setName(if (name.length > 16) name.substring(0, 16) else name)
//                        }
//                        entity.openEditor(player)
//                    }
//                }
//                .display { _, _, _ ->
//                    if (getName().isEmpty()) "§7_" else Editor.toSimple(getName())
//                }
//        naturalEditor("playerPing")
//                .reset { _, _ ->
//                    setPing(60)
//                }
//                .modify { player, entity, _ ->
//                    player.inputSign(arrayOf("${getPing()}", "", "请在第一行输入内容")) {
//                        if (it[0].isNotEmpty()) {
//                            setPing(NumberConversions.toInt(it[0]))
//                        }
//                        entity.openEditor(player)
//                    }
//                }
//                .display { _, _, _ ->
//                    getPing().toString()
//                }
//        naturalEditor("playerTexture")
//                .reset { _, _ ->
//                    resetTexture()
//                }
//                .modify { player, entity, _ ->
//                    player.inputSign(arrayOf(getTextureName(), "", "请在第一行输入内容")) {
//                        if (it[0].isNotEmpty()) {
//                            setTexture(it[0])
//                        }
//                        entity.openEditor(player)
//                    }
//                }
//                .display { _, _, _ ->
//                    if (gameProfile.textureName.isEmpty()) "§7_" else Editor.toSimple(gameProfile.textureName)
//                }
        // refresh skin
        submit(async = true, delay = 200, period = 200) {
            if (manager != null) {
                forViewers {
                    refreshPlayerInfo(it)
                }
            } else {
                cancel()
            }
        }
    }

    override fun visible(viewer: Player, visible: Boolean) {
        if (visible) {
            addPlayerInfo(viewer)
            spawn(viewer) {
                // 创建客户端对应表
                AdyeshachAPI.clientEntityMap.computeIfAbsent(viewer.name) { ConcurrentHashMap() }[index] = ClientEntity(this, playerUUID)
                // 生成实体
                NMS.INSTANCE.spawnNamedEntity(viewer, index, playerUUID, position.toLocation())
            }
            // 更新装备
            submit(delay = 1) {
                updateEquipment()
            }
            // 更新死亡信息以及玩家类型专属属性
            submit(delay = 5) {
                if (isDie) {
                    die(viewer)
                }
                if (isSleepingLegacy) {
                    setSleeping(true)
                }
                if (isHideFromTabList) {
                    removePlayerInfo(viewer)
                }
            }
        } else {
            removePlayerInfo(viewer)
            destroy(viewer) {
                // 销毁实体
                NMS.INSTANCE.destroyEntity(viewer, index)
                // 移除客户端对应表
                AdyeshachAPI.clientEntityMap[viewer.name]?.remove(index)
            }
        }
    }

    fun setName(name: String) {
        gameProfile.name = name.colored()
        respawn()
    }

    fun getName(): String {
        return gameProfile.name
    }

    fun setPing(ping: Int) {
        gameProfile.ping = ping
        respawn()
    }

    fun getPing(): Int {
        return gameProfile.ping
    }

    fun setTexture(name: String) {
        gameProfile.textureName = name
        submit(async = true) {
            try {
                MojangAPI.get(name)?.run {
                    setTexture(value, signature)
                }
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }
    }

    fun setTexture(texture: String, signature: String) {
        gameProfile.texture = arrayOf(texture, signature)
        respawn()
    }

    fun getTexture(): Array<String> {
        return gameProfile.texture
    }

    fun getTextureName(): String {
        return gameProfile.textureName
    }

    fun resetTexture() {
        gameProfile.texture = arrayOf("")
        respawn()
    }

    fun setSkinCapeEnabled(value: Boolean) {
        setMetadata("skinCape", value)
    }

    fun isSkinCapeEnabled() {
        return getMetadata("skinCape")
    }

    fun setSkinJacketEnabled(value: Boolean) {
        setMetadata("skinJacket", value)
    }

    fun isSkinJacketEnabled() {
        return getMetadata("skinJacket")
    }

    fun setSkinLeftSleeveEnabled(value: Boolean) {
        setMetadata("skinLeftSleeve", value)
    }

    fun isSkinLeftSleeveEnabled() {
        return getMetadata("skinLeftSleeve")
    }

    fun setSkinRightSleeveEnabled(value: Boolean) {
        setMetadata("skinRightSleeve", value)
    }

    fun isSkinRightSleeveEnabled() {
        return getMetadata("skinRightSleeve")
    }

    fun setSkinLeftPantsEnabled(value: Boolean) {
        setMetadata("skinLeftPants", value)
    }

    fun isSkinLeftPantsEnabled() {
        return getMetadata("skinLeftPants")
    }

    fun setSkinRightPantsEnabled(value: Boolean) {
        setMetadata("skinRightPants", value)
    }

    fun isSkinRightPantsEnabled() {
        return getMetadata("skinRightPants")
    }

    fun setSkinHatEnabled(value: Boolean) {
        setMetadata("skinHat", value)
    }

    fun isSkinHatEnabled() {
        return getMetadata("skinHat")
    }

    fun setSleeping(value: Boolean) {
        if (value) {
            if (minecraftVersion >= 11400) {
                setPose(BukkitPose.SLEEPING)
            } else {
                forViewers {
                    NMS.INSTANCE.sendPlayerSleeping(it, index, position.toLocation())
                }
            }
        } else {
            if (minecraftVersion >= 11400) {
                setPose(BukkitPose.STANDING)
            } else {
                displayAnimation(BukkitAnimation.LEAVE_BED)
            }
            teleport(position)
        }
        isSleepingLegacy = value
    }

    fun isSleeping(): Boolean {
        return if (minecraftVersion >= 11400) {
            getPose() == BukkitPose.SLEEPING
        } else {
            isSleepingLegacy
        }
    }

    fun refreshPlayerInfo(viewer: Player) {
        removePlayerInfo(viewer)
        addPlayerInfo(viewer)
        submit(delay = 5) {
            if (isHideFromTabList) {
                removePlayerInfo(viewer)
            }
        }
    }

    private fun addPlayerInfo(viewer: Player) {
        val event = AdyeshachGameProfileGenerateEvent(this, viewer, gameProfile.clone())
        event.call()
        NMS.INSTANCE.addPlayerInfo(viewer, playerUUID, event.gameProfile.name, event.gameProfile.ping, event.gameProfile.texture)
    }

    private fun removePlayerInfo(viewer: Player) {
        NMS.INSTANCE.removePlayerInfo(viewer, playerUUID)
    }
}