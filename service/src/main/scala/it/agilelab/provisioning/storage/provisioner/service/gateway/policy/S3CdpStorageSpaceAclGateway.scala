package it.agilelab.provisioning.storage.provisioner.service.gateway.policy

import cats.implicits._
import com.typesafe.scalalogging.Logger
import it.agilelab.provisioning.commons.client.ranger.model.RangerRole
import it.agilelab.provisioning.commons.principalsmapping.{ CdpIamGroup, CdpIamPrincipals, CdpIamUser }
import it.agilelab.provisioning.mesh.self.service.api.model.Component.OutputPort
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGateway
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, PolicyAttachment, S3Cdp }
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.provider.RangerGatewayProvider
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.role.{ OwnerRoleType, UserRoleType }

class S3CdpStorageSpaceAclGateway(
  cdpGateway: CdpGateway,
  rangerGatewayProvider: RangerGatewayProvider,
  adminUser: String
) extends StorageSpaceAclGateway {

  private val logger = Logger(getClass.getName)

  override def provisionAcl(
    dp: DataProduct[DpCdp],
    o: OutputPort[S3Cdp],
    ownerUser: String,
    ownerGroup: String
  ): Either[ComponentGatewayError, Seq[PolicyAttachment]] = for {
    gtwys           <- getRangerGateway(o.specific.cdpEnvironment)
    identifiers     <- extractIdentifiers(o)
    szName           = buildSecurityZoneName(identifiers._1, identifiers._2, identifiers._3)
    sz              <- gtwys.zoneGateway
                         .upsertSecurityZone(
                           zoneName = szName,
                           serviceType = "S3",
                           dlName = gtwys.datalake.getDatalakeName,
                           bucket = o.specific.bucket,
                           path = buildPath(dp.environment, identifiers._1, identifiers._2, identifiers._3),
                           adminUser = adminUser
                         )
                         .leftMap { e =>
                           val errMsg = s"Failed to upsert the Security Zone '$szName' on Ranger"
                           logger.error(s"$errMsg: ${e.show}")
                           ComponentGatewayError(errMsg)
                         }
    ownerRole       <- gtwys.roleGateway
                         .upsertRole(
                           rolePrefix = buildOwnerRolePrefix(identifiers._1, identifiers._2, identifiers._3),
                           roleType = OwnerRoleType,
                           deployUser = adminUser,
                           ownerGroups = Seq.empty,
                           ownerUsers = Seq.empty,
                           users = Seq(ownerUser),
                           groups = Seq(ownerGroup)
                         )
                         .leftMap { e =>
                           val errMsg = s"Failed to upsert the Owner Role on Ranger"
                           logger.error(s"$errMsg: ${e.show}")
                           ComponentGatewayError(errMsg)
                         }
    userRole        <- gtwys.roleGateway
                         .upsertRole(
                           rolePrefix = buildUserRolePrefix(identifiers._1, identifiers._2, identifiers._3, identifiers._4),
                           roleType = UserRoleType,
                           deployUser = adminUser,
                           ownerGroups = Seq.empty,
                           ownerUsers = Seq.empty,
                           users = Seq.empty, // this will be populated by updateAcl
                           groups = Seq.empty // this will be populated by updateAcl
                         )
                         .leftMap { e =>
                           val errMsg = s"Failed to upsert the User Role on Ranger"
                           logger.error(s"$errMsg: ${e.show}")
                           ComponentGatewayError(errMsg)
                         }
    componentPolicy <- gtwys.policyGateway
                         .upsertComponentPolicy(
                           prefix = buildPolicyPrefix(identifiers._1, identifiers._2, identifiers._3, identifiers._4),
                           bucket = o.specific.bucket,
                           path = o.specific.folder,
                           ownerRole = ownerRole.name,
                           userRole = userRole.name,
                           zoneName = sz.name
                         )
                         .leftMap { e =>
                           val errMsg = s"Failed to upsert the Component Policy on Ranger"
                           logger.error(s"$errMsg: ${e.show}")
                           ComponentGatewayError(errMsg)
                         }
  } yield Seq(componentPolicy)

  private def buildOwnerRolePrefix(domain: String, name: String, majorVersion: String) =
    clean(s"${domain}_${name}_$majorVersion")

  private def buildUserRolePrefix(domain: String, name: String, majorVersion: String, componentName: String) =
    clean(s"${domain}_${name}_${majorVersion}_$componentName")

  private def extractIdentifiers(op: OutputPort[S3Cdp]) =
    op.id match {
      case s"urn:dmb:cmp:$domain:$name:$majorVersion:$componentName" =>
        Right((domain, name, majorVersion, componentName))
      case _                                                         =>
        Left(
          ComponentGatewayError(
            s"Component id '${op.id}' is not in the expected shape, cannot extract attributes"
          )
        )
    }

  private def buildPolicyPrefix(domain: String, name: String, majorVersion: String, componentName: String) =
    clean(s"${domain}_${name}_${majorVersion}_$componentName")

  private def buildSecurityZoneName(domain: String, name: String, majorVersion: String) =
    clean(s"${domain}_${name}_$majorVersion")

  private def clean(string: String) = string.replaceAll("[^A-Za-z0-9_]", "_")

  private def buildPath(environment: String, domain: String, name: String, majorVersion: String) =
    s"mesh/domains/${domain}/data-products/${name}/${environment}/${majorVersion}/*"

  private def getRangerGateway(cdpEnvironment: String) = for {
    env          <- cdpGateway
                      .getEnvironment(cdpEnvironment)
                      .leftMap { e =>
                        val errMsg = s"Failed to retrieve CDP Environment '$cdpEnvironment'"
                        logger.error(s"$errMsg: ${e.show}")
                        ComponentGatewayError(errMsg)
                      }
    dl           <- cdpGateway.getDataLake(env).leftMap { e =>
                      val errMsg = s"Failed to retrieve CDP Datalake for env '$cdpEnvironment'"
                      logger.error(s"$errMsg: ${e.show}")
                      ComponentGatewayError(errMsg)
                    }
    rangerHost   <- cdpGateway.getRangerHost(dl).leftMap { e =>
                      val errMsg = s"Failed to retrieve Ranger Host for Datalake '${dl.getDatalakeName}'"
                      logger.error(s"$errMsg: ${e.show}")
                      ComponentGatewayError(errMsg)
                    }
    rangerClient <- rangerGatewayProvider.getRangerClient(rangerHost).leftMap { e =>
                      val errMsg = s"Failed to retrieve Ranger Client for host '$rangerHost'"
                      logger.error(s"$errMsg: ${e.show}")
                      ComponentGatewayError(errMsg)
                    }
  } yield rangerGatewayProvider.getRangerGateway(rangerClient, dl)

  override def unprovisionAcl(o: OutputPort[S3Cdp]): Either[ComponentGatewayError, Seq[PolicyAttachment]] = for {
    gtwys       <- getRangerGateway(o.specific.cdpEnvironment)
    identifiers <- extractIdentifiers(o)
    szName       = buildSecurityZoneName(identifiers._1, identifiers._2, identifiers._3)
    policies    <- gtwys.policyGateway
                     .deleteComponentPolicy(
                       prefix = buildPolicyPrefix(identifiers._1, identifiers._2, identifiers._3, identifiers._4),
                       zoneName = szName
                     )
                     .leftMap { e =>
                       val errMsg = s"Failed to delete Component Policy on Ranger"
                       logger.error(s"$errMsg: ${e.show}")
                       ComponentGatewayError(errMsg)
                     }
    _           <- gtwys.roleGateway
                     .deleteUserRole(
                       buildUserRolePrefix(identifiers._1, identifiers._2, identifiers._3, identifiers._4)
                     )
                     .leftMap { e =>
                       val errMsg = s"Failed to delete User Role on Ranger"
                       logger.error(s"$errMsg: ${e.show}")
                       ComponentGatewayError(errMsg)
                     }
  } yield policies

  override def updateAcl(
    o: OutputPort[S3Cdp],
    refs: Set[CdpIamPrincipals]
  ): Either[ComponentGatewayError, RangerRole] = for {
    gtwys       <- getRangerGateway(o.specific.cdpEnvironment)
    identifiers <- extractIdentifiers(o)
    usersGroups <- Right(refs.partitionMap {
                     case CdpIamUser(_, workloadUsername, _) => Left(workloadUsername)
                     case CdpIamGroup(groupName, _)          => Right(groupName)
                   })
    userRole    <- gtwys.roleGateway
                     .upsertRole(
                       rolePrefix = buildUserRolePrefix(identifiers._1, identifiers._2, identifiers._3, identifiers._4),
                       roleType = UserRoleType,
                       deployUser = adminUser,
                       ownerUsers = Seq.empty,
                       ownerGroups = Seq.empty,
                       users = usersGroups._1.toSeq,
                       groups = usersGroups._2.toSeq
                     )
                     .leftMap { e =>
                       val errMsg = s"Failed to upsert the User Role on Ranger"
                       logger.error(s"$errMsg: ${e.show}")
                       ComponentGatewayError(errMsg)
                     }
  } yield userRole

}
