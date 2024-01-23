package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.zone

import cats.implicits.{ catsSyntaxEq, toBifunctorOps }
import it.agilelab.provisioning.commons.client.ranger.model.{ RangerSecurityZone, RangerService }
import it.agilelab.provisioning.commons.client.ranger.{ RangerClient, RangerClientError }
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.zone.RangerSecurityZoneGatewayError.{
  FindServiceErr,
  UpsertSecurityZoneErr
}

class RangerSecurityZoneGateway(
  val rangerClient: RangerClient
) {

  /** Upserts a security zone based on the specified name. If a Security Zone already exists with that name,
    * it is updated by including the received parameters. Otherwise a new one it is created.
    *
    * @param zoneName Name of the security zone to upsert
    * @param serviceType The service type related to the security zone
    * @param dlName Data lake name
    * @param bucket The bucket name
    * @param path The path inside the bucket
    * @param adminUser The admin user of security zone
    * @return Either a [[RangerSecurityZoneGatewayError]] error while upserting the security zone, or the newly created or updated [[RangerSecurityZone]]
    */
  def upsertSecurityZone(
    zoneName: String,
    serviceType: String,
    dlName: String,
    bucket: String,
    path: String,
    adminUser: String
  ): Either[RangerSecurityZoneGatewayError, RangerSecurityZone] = for {
    zoneOpt     <- rangerClient
                     .findSecurityZoneByName(zoneName)
                     .leftMap(e => UpsertSecurityZoneErr(e))
    service     <- getService(serviceType, dlName)
    zoneUpdated <- zoneOpt.fold(createSecurityZone(zoneName, service.name, bucket, path, adminUser))(z =>
                     updateSC(z, service.name, bucket, path)
                   )
  } yield zoneUpdated

  private def getService(
    serviceType: String,
    dlName: String
  ): Either[RangerSecurityZoneGatewayError, RangerService] = for {
    services <- findServicesByTypeInCluster(serviceType, dlName)
                  .leftMap(e => FindServiceErr(e.toString))
    s        <- services.headOption.toRight[FindServiceErr](
                  FindServiceErr(
                    "Unable to find service with " +
                      "type %s in cluster %s.".format(serviceType, dlName)
                  )
                )
  } yield s

  private def findServicesByTypeInCluster(
    serviceType: String,
    clusterName: String
  ): Either[RangerClientError, Seq[RangerService]] =
    for {
      services        <- rangerClient.findAllServices
      servicesFiltered = services.filter(s =>
                           s.`type`.equalsIgnoreCase(serviceType) &&
                             s.configs.getOrElse("cluster.name", "") === clusterName
                         )
    } yield servicesFiltered

  private def createSecurityZone(
    zoneName: String,
    serviceName: String,
    bucket: String,
    path: String,
    adminUser: String
  ): Either[RangerSecurityZoneGatewayError, RangerSecurityZone] =
    rangerClient
      .createSecurityZone(
        RangerSecurityZoneGenerator.securityZone(
          zoneName = zoneName,
          serviceName = serviceName,
          bucketResources = Seq(bucket),
          pathResources = Seq(path),
          adminUsers = Seq(adminUser),
          adminUserGroups = Seq.empty,
          auditUsers = Seq(adminUser),
          auditUserGroups = Seq.empty
        )
      )
      .leftMap(e => UpsertSecurityZoneErr(e))

  private def updateSC(
    zone: RangerSecurityZone,
    serviceName: String,
    bucket: String,
    path: String
  ): Either[RangerSecurityZoneGatewayError, RangerSecurityZone] =
    rangerClient
      .updateSecurityZone(
        RangerSecurityZoneGenerator.securityZoneWithMergedServiceResources(
          zone,
          serviceName,
          Seq(bucket),
          Seq(path)
        )
      )
      .leftMap(e => UpsertSecurityZoneErr(e))

}

object RangerSecurityZoneGateway {
  def default(
    rangerClient: RangerClient
  ): RangerSecurityZoneGateway =
    new RangerSecurityZoneGateway(
      rangerClient
    )

}
