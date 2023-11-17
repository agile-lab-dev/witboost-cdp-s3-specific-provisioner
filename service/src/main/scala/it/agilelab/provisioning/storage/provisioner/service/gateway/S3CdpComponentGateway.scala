package it.agilelab.provisioning.storage.provisioner.service.gateway

import cats.implicits._
import it.agilelab.provisioning.mesh.self.service.api.model.Component.OutputPort
import it.agilelab.provisioning.mesh.self.service.api.model.{ Component, ProvisionRequest }
import it.agilelab.provisioning.mesh.self.service.core.gateway.{ ComponentGateway, ComponentGatewayError }
import it.agilelab.provisioning.mesh.self.service.core.model.ProvisionCommand
import it.agilelab.provisioning.storage.provisioner.core.models
import it.agilelab.provisioning.storage.provisioner.core.models.{
  DpCdp,
  PolicyAttachment,
  S3Cdp,
  S3CdpAcl,
  S3CdpResources
}
import it.agilelab.provisioning.storage.provisioner.service.gateway.mapper.StorageSpaceMapper
import it.agilelab.provisioning.storage.provisioner.service.gateway.policy.StorageSpaceAclGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.storage.StorageSpaceGateway

/** S3CDPStorageSpaceProvisionerService
  * an implementation of ProvisionerService[S3RawOutputPort,S3RawOutputPortResources]
  */
class S3CdpComponentGateway(
  storageSpaceMapper: StorageSpaceMapper,
  storageSpaceGateway: StorageSpaceGateway,
  storageSpaceAclGateway: StorageSpaceAclGateway
) extends ComponentGateway[DpCdp, S3Cdp, S3CdpResources] {

  override def create(provisionCommand: ProvisionCommand[DpCdp, S3Cdp]): Either[ComponentGatewayError, S3CdpResources] =
    provisionCommand match {
      case ProvisionCommand(_, ProvisionRequest(dp, c: Option[OutputPort[S3Cdp]])) if isRightComponent(c) =>
        c.fold[Either[ComponentGatewayError, S3CdpResources]](
          Left(ComponentGatewayError(s"Unable to create resource from empty component"))
        )(o =>
          for {
            storageSpace <- storageSpaceMapper
                              .map(dp, o)
                              .leftMap(_ => ComponentGatewayError("Unable to map provided request to s3 on cdp"))
            _            <- storageSpaceGateway
                              .create(storageSpace)
                              .leftMap(_ => ComponentGatewayError("Unable to create s3 on cdp"))
            // TODO this section need to be executed in the updateAcl request
//            policies     <- storageSpaceAclGateway
//                              .updateAcl(storageSpace)
//                              .leftMap(_ => ComponentGatewayError("Unable to update s3 folder acl"))
            policies     <- Right(S3CdpAcl(Seq.empty[PolicyAttachment], Seq.empty[PolicyAttachment]))
          } yield models.S3CdpResources(storageSpace.id, storageSpace.bucket, storageSpace.path, policies)
        )
      case _                                                                                              =>
        Left(ComponentGatewayError(s"Unable to create resource, bad incoming request ${provisionCommand.toString}"))
    }

  override def destroy(
    provisionCommand: ProvisionCommand[DpCdp, S3Cdp]
  ): Either[ComponentGatewayError, S3CdpResources] =
    provisionCommand match {
      case ProvisionCommand(_, ProvisionRequest(dp, c: Option[OutputPort[S3Cdp]])) if isRightComponent(c) =>
        c.fold[Either[ComponentGatewayError, S3CdpResources]](
          Left(ComponentGatewayError(s"Unable to destroy resource from empty component"))
        )(o =>
          for {
            storageSpace <- storageSpaceMapper
                              .map(dp, o)
                              .map(sp => sp.copy(owners = Seq.empty[String], users = Seq.empty[String]))
                              .leftMap(_ => ComponentGatewayError("Unable to map provided request to s3 on cdp"))

            // TODO this method doesn't delete bucket and folder, is it right?

            // TODO probably it's ok to remove acl when undeploying
            policies     <- storageSpaceAclGateway
                              .updateAcl(storageSpace)
                              .leftMap(_ => ComponentGatewayError("Unable to update s3 folder acl"))
          } yield models.S3CdpResources(storageSpace.id, storageSpace.bucket, storageSpace.path, policies)
        )
      case _                                                                                              =>
        Left(ComponentGatewayError(s"Unable to destroy resource, bad incoming request ${provisionCommand.toString}"))
    }

  private def isRightComponent(c: Option[Component[S3Cdp]]): Boolean =
    c.fold(false)(o =>
      o.isInstanceOf[OutputPort[S3Cdp]] &&
        o.asInstanceOf[OutputPort[S3Cdp]].specific.isInstanceOf[S3Cdp]
    )

}
