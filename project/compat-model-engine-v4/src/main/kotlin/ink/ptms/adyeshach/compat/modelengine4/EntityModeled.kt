package ink.ptms.adyeshach.compat.modelengine4

import com.ticxo.modelengine.api.entity.Dummy
import com.ticxo.modelengine.api.nms.entity.EntityHandler
import ink.ptms.adyeshach.core.entity.EntityInstance
import ink.ptms.adyeshach.core.entity.StandardTags
import org.bukkit.Location
import org.bukkit.entity.HumanEntity
import org.bukkit.inventory.EquipmentSlot

/**
 * Adyeshach
 * ink.ptms.adyeshach.compat.modelengine3.EntityModeled
 *
 * @author 坏黑
 * @since 2024/1/14 22:30
 */
internal class EntityModeled(val entity: EntityInstance) : Dummy<EntityInstance>(entity.index, entity.normalizeUniqueId, entity) {

    override fun interact(p0: HumanEntity, p1: EquipmentSlot): EntityHandler.InteractionResult {
        return EntityHandler.InteractionResult.SUCCESS
    }

    override fun getLocation(): Location {
        return entity.getLocation()
    }

    override fun isVisible(): Boolean {
        return !entity.isInvisible()
    }

    override fun setVisible(p0: Boolean) {
        entity.setInvisible(!p0)
    }

    override fun isRemoved(): Boolean {
        return entity.isRemoved
    }

    override fun isAlive(): Boolean {
        return !entity.isRemoved
    }

    override fun getRenderRadius(): Int {
        return entity.visibleDistance.toInt()
    }

    override fun setRenderRadius(p0: Int) {
        entity.visibleDistance = p0.toDouble()
    }

    override fun isWalking(): Boolean {
        return entity.hasTag(StandardTags.IS_MOVING)
    }
}