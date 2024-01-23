package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.provider

import com.cloudera.cdp.datalake.model.Datalake
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.policy.RangerPolicyGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.role.RangerRoleGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.zone.RangerSecurityZoneGateway

/** Groups classes needed to interact with Ranger
  * @param zoneGateway class to interact with Ranger zones
  * @param roleGateway class to interact with Ranger roles
  * @param policyGateway class to interact with Ranger policies
  * @param datalake the datalake related to Ranger
  */
class RangerGateway(
  val zoneGateway: RangerSecurityZoneGateway,
  val roleGateway: RangerRoleGateway,
  val policyGateway: RangerPolicyGateway,
  val datalake: Datalake
)
