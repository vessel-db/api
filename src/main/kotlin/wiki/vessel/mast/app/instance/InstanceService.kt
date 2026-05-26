package wiki.vessel.mast.app.instance

import io.ktor.server.routing.*
import net.atlantisservices.sdb.SdbRepository
import wiki.vessel.mast.app.Vessel
import wiki.vessel.mast.app.service.Service
import wiki.vessel.mast.app.service.ServicePriority

class InstanceService : Service() {
    override val name: String = "instance"
    override val priority: ServicePriority = ServicePriority.OPTIONAL

    override fun start() {
        Vessel.instances = SdbRepository(Instance::class, "instances")
        Vessel.subusers = SdbRepository(InstanceSubuser::class, "instance_subusers")

        val instanceCount = Vessel.instances.count()
        val subuserCount = Vessel.subusers.count()

        println("Loaded $instanceCount instances, $subuserCount subusers")
    }

    override fun routing(route: Route) {
        route.instanceRoutes()
    }
}