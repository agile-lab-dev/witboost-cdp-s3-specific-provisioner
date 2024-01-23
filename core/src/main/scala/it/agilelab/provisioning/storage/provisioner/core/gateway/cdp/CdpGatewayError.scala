package it.agilelab.provisioning.storage.provisioner.core.gateway.cdp

import cats.Show
import cats.implicits.showInterpolator
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClientError
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClientError
import it.agilelab.provisioning.commons.showable.ShowableOps

sealed trait CdpGatewayError extends Exception with Product with Serializable

object CdpGatewayError {
  final case class CdpGatewayInitErr(error: Throwable)                       extends CdpGatewayError
  final case class DescribeCdpEnvErr(error: CdpEnvClientError)               extends CdpGatewayError
  final case class CdpEnvWrongStatusErr(environment: String, status: String) extends CdpGatewayError
  final case class DescribeCdpDlErr(error: CdpDlClientError)                 extends CdpGatewayError
  final case class CdpDlWrongStatusErr(environment: String, status: String)  extends CdpGatewayError
  final case class DatalakeNotFound(environment: String)                     extends CdpGatewayError
  final case class GetRangerHostErr(error: String)                           extends CdpGatewayError

  implicit def showCdpGatewayError: Show[CdpGatewayError] =
    Show.show {
      case CdpGatewayInitErr(error)                  => show"CdpGatewayInitErr(${ShowableOps.showThrowableError.show(error)})"
      case DescribeCdpEnvErr(error)                  => show"DescribeCdpEnvErr($error)"
      case CdpEnvWrongStatusErr(environment, status) => show"CdpEnvWrongStatusErr($environment,$status)"
      case DescribeCdpDlErr(error)                   => show"DescribeCdpDlErr($error)"
      case CdpDlWrongStatusErr(environment, status)  => show"CdpDlWrongStatusErr($environment,$status)"
      case DatalakeNotFound(environment)             => show"DatalakeNotFound($environment)"
      case GetRangerHostErr(error)                   => show"GetRangerHostErr($error)"
    }
}
