package it.agilelab.provisioning.storage.provisioner.app.config

import io.circe.generic.auto._
import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.commons.principalsmapping.CdpIamPrincipals
import it.agilelab.provisioning.mesh.self.service.api.controller.ProvisionerController
import it.agilelab.provisioning.mesh.self.service.core.provisioner.Provisioner
import it.agilelab.provisioning.storage.provisioner.app.api.validator.S3CdpValidator
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, S3Cdp, S3CdpResources }
import it.agilelab.provisioning.storage.provisioner.service.context.{
  ContextError,
  MemoryStateRepository,
  ProvisionerContext
}
import it.agilelab.provisioning.storage.provisioner.service.gateway.S3CdpComponentGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.mapper.S3CdpStorageSpaceMapper
import it.agilelab.provisioning.storage.provisioner.service.gateway.policy.S3CdpStorageSpaceAclGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.provider.RangerGatewayProvider
import it.agilelab.provisioning.storage.provisioner.service.gateway.storage.S3CdpStorageSpaceGateway

object S3CdpProvisionerController {
  def apply(
    conf: Conf
  ): Either[ContextError, ProvisionerController[DpCdp, S3Cdp, CdpIamPrincipals]] = for {
    s3Validator <- ValidatorContext
                     .init(conf)
                     .map { ctx =>
                       S3CdpValidator.validator(
                         ctx.cdpGateway
                       )
                     }
    controller  <- ProvisionerContext
                     .init(conf)
                     .map { ctx =>
                       ProvisionerController.defaultAclWithAudit[DpCdp, S3Cdp, CdpIamPrincipals](
                         s3Validator,
                         Provisioner.defaultSync[DpCdp, S3Cdp, S3CdpResources, CdpIamPrincipals](
                           new S3CdpComponentGateway(
                             new S3CdpStorageSpaceMapper,
                             new S3CdpStorageSpaceGateway(ctx.s3Gateway),
                             new S3CdpStorageSpaceAclGateway(
                               ctx.cdpGateway,
                               new RangerGatewayProvider(ctx.deployRoleUser, ctx.deployRolePwd),
                               ctx.deployRoleUser
                             ),
                             ctx.principalsMapper
                           )
                         ),
                         // TODO we should create our custom ProvisionerController to avoid to inject a state repo
                         new MemoryStateRepository,
                         ctx.principalsMapper
                       )
                     }
  } yield controller

}
