package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.zone

import it.agilelab.provisioning.commons.client.ranger.model.{ RangerSecurityZone, RangerSecurityZoneResources }
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.RangerResources

object RangerSecurityZoneGenerator {
  def securityZone(
    zoneName: String,
    serviceName: String,
    bucketResources: Seq[String],
    pathResources: Seq[String],
    adminUsers: Seq[String],
    adminUserGroups: Seq[String],
    auditUsers: Seq[String],
    auditUserGroups: Seq[String]
  ): RangerSecurityZone =
    RangerSecurityZoneGenerator
      .empty(zoneName)
      .copy(
        services = Map(
          serviceName -> RangerResources.s3SecurityZoneResources(
            bucketResources,
            pathResources
          )
        ),
        adminUsers = adminUsers,
        adminUserGroups = adminUserGroups,
        auditUsers = auditUsers,
        auditUserGroups = auditUserGroups
      )

  def empty(name: String): RangerSecurityZone =
    RangerSecurityZone(
      id = -1,
      name = name,
      services = Map.empty,
      isEnabled = true,
      adminUsers = Seq.empty,
      adminUserGroups = Seq.empty,
      auditUsers = Seq.empty,
      auditUserGroups = Seq.empty
    )

  def securityZoneWithMergedServiceResources(
    zone: RangerSecurityZone,
    serviceName: String,
    bucketResources: Seq[String],
    pathResources: Seq[String]
  ): RangerSecurityZone = zone.copy(
    services = zone.services.updatedWith(serviceName)(resources =>
      Some(
        resources.fold(
          RangerResources.s3SecurityZoneResources(
            bucketResources,
            pathResources
          )
        )(old =>
          RangerResources.s3SecurityZoneResources(
            bucketNames = mergeResources(old, bucketResources, "bucket"),
            paths = mergeResources(old, pathResources, "path")
          )
        )
      )
    )
  )

  private def mergeResources(
    oldRes: RangerSecurityZoneResources,
    newRes: Seq[String],
    resKey: String
  ): Seq[String] =
    oldRes.resources
      .filter(res => res.contains(resKey))
      .flatMap(m => m(resKey))
      .concat(newRes)
      .distinct

}
