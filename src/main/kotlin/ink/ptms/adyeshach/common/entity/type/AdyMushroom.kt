package ink.ptms.adyeshach.common.entity.type

import ink.ptms.adyeshach.common.bukkit.BukkitMushroom
import ink.ptms.adyeshach.common.entity.EntityTypes

/**
 * @author sky
 * @date 2020/8/4 23:15
 */
class AdyMushroom() : AdyCow(EntityTypes.MUSHROOM) {

    init {
        /*
        1.16,1.15
        16
        1.14
        15
         */
        registerMeta(at(11500 to 16, 11400 to 15), "type", BukkitMushroom.RED.name.toLowerCase())
    }

    fun setType(value: BukkitMushroom) {
        setMetadata("type", value.name.toLowerCase())
    }

    fun getType(): BukkitMushroom {
        return BukkitMushroom.valueOf(getMetadata<String>("type").toUpperCase())
    }
}