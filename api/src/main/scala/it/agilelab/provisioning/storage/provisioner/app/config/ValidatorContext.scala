package it.agilelab.provisioning.storage.provisioner.app.config

import cats.implicits.toBifunctorOps
import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGateway
import it.agilelab.provisioning.storage.provisioner.service.context.ContextError
import it.agilelab.provisioning.storage.provisioner.service.context.ContextError.ClientError

final case class ValidatorContext(
  cdpGateway: CdpGateway
)

object ValidatorContext {
  def init(
    conf: Conf
  ): Either[ContextError, ValidatorContext] =
    for {
      cdpGateway <- CdpGateway.default().leftMap(e => ClientError("CdpGateway", e))
    } yield new ValidatorContext(
      cdpGateway
    )
}
