package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.provider

import cats.Show
import cats.implicits.showInterpolator
import it.agilelab.provisioning.commons.showable.ShowableOps.showThrowableError

final case class RangerGatewayProviderError(error: Throwable) extends Exception

object RangerGatewayProviderError {
  implicit def showRangerGatewayProviderError: Show[RangerGatewayProviderError] =
    Show.show(e => show"RangerGatewayProviderError(${e.error})")
}
