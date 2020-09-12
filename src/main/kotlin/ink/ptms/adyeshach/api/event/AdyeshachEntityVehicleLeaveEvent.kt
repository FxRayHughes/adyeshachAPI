package ink.ptms.adyeshach.api.event

import ink.ptms.adyeshach.common.entity.EntityInstance
import io.izzel.taboolib.module.event.EventCancellable
import org.bukkit.Bukkit

/**
 * @Author sky
 * @Since 2020-08-14 19:21
 */
class AdyeshachEntityVehicleLeaveEvent(val entity: EntityInstance, val vehicle: EntityInstance) : EventCancellable<AdyeshachEntityVehicleLeaveEvent>() {

    init {
        async(!Bukkit.isPrimaryThread())
    }
}