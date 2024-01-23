package it.agilelab.provisioning.storage.provisioner.service.gateway

import cats.implicits._
import com.typesafe.scalalogging.Logger
import it.agilelab.provisioning.commons.principalsmapping.{
  CdpIamGroup,
  CdpIamPrincipals,
  CdpIamUser,
  PrincipalsMapper
}
import it.agilelab.provisioning.mesh.self.service.api.model.Component.OutputPort
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.mesh.self.service.core.gateway.{ ComponentGateway, ComponentGatewayError }
import it.agilelab.provisioning.mesh.self.service.core.model.ProvisionCommand
import it.agilelab.provisioning.storage.provisioner.core.models
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, S3Cdp, S3CdpAcl, S3CdpResources }
import it.agilelab.provisioning.storage.provisioner.service.gateway.S3CdpComponentGateway.getOutputPortComponent
import it.agilelab.provisioning.storage.provisioner.service.gateway.mapper.StorageSpaceMapper
import it.agilelab.provisioning.storage.provisioner.service.gateway.policy.StorageSpaceAclGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.storage.StorageSpaceGateway

/** S3CDPStorageSpaceProvisionerService
  * an implementation of ProvisionerService[S3RawOutputPort,S3RawOutputPortResources]
  */
class S3CdpComponentGateway(
  storageSpaceMapper: StorageSpaceMapper,
  storageSpaceGateway: StorageSpaceGateway,
  storageSpaceAclGateway: StorageSpaceAclGateway,
  principalsMapper: PrincipalsMapper[CdpIamPrincipals]
) extends ComponentGateway[DpCdp, S3Cdp, S3CdpResources, CdpIamPrincipals] {

  private val logger = Logger(getClass.getName)

  override def create(provisionCommand: ProvisionCommand[DpCdp, S3Cdp]): Either[ComponentGatewayError, S3CdpResources] =
    for {
      o                 <- getOutputPortComponent(provisionCommand.provisionRequest)
      dp                <- Right(provisionCommand.provisionRequest.dataProduct)
      storageSpace      <- storageSpaceMapper
                             .map(dp, o)
                             .leftMap { e =>
                               val errMsg = s"Unable to map provided request to S3 on CDP"
                               logger.error(s"$errMsg: ${e.show}")
                               ComponentGatewayError(errMsg)
                             }
      _                 <- storageSpaceGateway
                             .create(storageSpace)
                             .leftMap { e =>
                               val errMsg = s"Unable to create folder '${storageSpace.path}' in bucket '${storageSpace.bucket}' on S3"
                               logger.error(s"$errMsg: ${e.show}")
                               ComponentGatewayError(errMsg)
                             }
      owners            <- getOwnerPrincipals(dp)
      policyAttachments <- storageSpaceAclGateway.provisionAcl(dp, o, owners._1.workloadUsername, owners._2.groupName)
      policies          <- Right(S3CdpAcl(policyAttachments, Seq.empty))
    } yield models.S3CdpResources(storageSpace.id, storageSpace.bucket, storageSpace.path, policies)

  private def getOwnerPrincipals(
    dataProduct: DataProduct[DpCdp]
  ): Either[ComponentGatewayError, (CdpIamUser, CdpIamGroup)] = for {
    owners     <- Right(principalsMapper.map(Set(dataProduct.dataProductOwner, dataProduct.devGroup)))
    ownerUser  <- owners.get(dataProduct.dataProductOwner) match {
                    case Some(Right(principal: CdpIamUser)) => Right(principal)
                    case Some(Right(_))                     =>
                      Left(
                        ComponentGatewayError(s"Data Product Owner '${dataProduct.dataProductOwner}' is not a CDP user")
                      )
                    case Some(Left(e))                      =>
                      val errMsg = s"Failed to map Data Product Owner '${dataProduct.dataProductOwner}' to a CDP user"
                      logger.error(s"$errMsg: ${e.show}")
                      Left(ComponentGatewayError(errMsg))
                    case None                               =>
                      Left(
                        ComponentGatewayError("Something went wrong while retrieving mapped Data Product Owner")
                      )
                  }
    ownerGroup <- owners.get(dataProduct.devGroup) match {
                    case Some(Right(principal: CdpIamGroup)) => Right(principal)
                    case Some(Right(_))                      =>
                      Left(
                        ComponentGatewayError(s"Data Product Owner Group '${dataProduct.devGroup}' is not a CDP group")
                      )
                    case Some(Left(e))                       =>
                      val errMsg = s"Failed to map Data Product Owner Group '${dataProduct.devGroup}' to a CDP group"
                      logger.error(s"$errMsg: ${e.show}")
                      Left(ComponentGatewayError(errMsg))
                    case None                                =>
                      Left(
                        ComponentGatewayError("Something went wrong while retrieving mapped Data Product Owner Group")
                      )
                  }
  } yield (ownerUser, ownerGroup)

  override def destroy(
    provisionCommand: ProvisionCommand[DpCdp, S3Cdp]
  ): Either[ComponentGatewayError, S3CdpResources] = for {
    o                <- getOutputPortComponent(provisionCommand.provisionRequest)
    dp               <- Right(provisionCommand.provisionRequest.dataProduct)
    storageSpace     <- storageSpaceMapper
                          .map(dp, o)
                          .leftMap { e =>
                            val errMsg = s"Unable to map provided request to S3 on CDP"
                            logger.error(s"$errMsg: ${e.show}")
                            ComponentGatewayError(errMsg)
                          }
    // TODO handle removeData sent from coordinator
    detachedPolicies <- storageSpaceAclGateway.unprovisionAcl(o)
  } yield models.S3CdpResources(
    storageSpace.id,
    storageSpace.bucket,
    storageSpace.path,
    S3CdpAcl(Seq.empty, detachedPolicies)
  )

  override def updateAcl(
    provisionCommand: ProvisionCommand[DpCdp, S3Cdp],
    refs: Set[CdpIamPrincipals]
  ): Either[ComponentGatewayError, Set[CdpIamPrincipals]] = for {
    o <- getOutputPortComponent(provisionCommand.provisionRequest)
    _ <- storageSpaceAclGateway.updateAcl(o, refs)
  } yield refs

}

object S3CdpComponentGateway {
  def getOutputPortComponent(
    a: ProvisionRequest[DpCdp, S3Cdp]
  ): Either[ComponentGatewayError, OutputPort[S3Cdp]] = a.component
    .toRight(ComponentGatewayError("Received provisioning request does not contain a component"))
    .flatMap {
      case c: OutputPort[S3Cdp] => Right(c)
      case _                    =>
        Left(ComponentGatewayError("The provided component is not accepted by this provisioner"))
    }
}
