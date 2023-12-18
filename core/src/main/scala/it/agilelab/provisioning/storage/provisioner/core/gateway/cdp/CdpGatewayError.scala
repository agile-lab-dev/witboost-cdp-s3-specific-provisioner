package it.agilelab.provisioning.storage.provisioner.core.gateway.cdp

import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClientError
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClientError

sealed trait CdpGatewayError extends Exception with Product with Serializable

object CdpGatewayError {
  final case class CdpGatewayInitErr(error: Throwable)                       extends CdpGatewayError
  final case class DescribeCdpEnvErr(error: CdpEnvClientError)               extends CdpGatewayError
  final case class CdpEnvWrongStatusErr(environment: String, status: String) extends CdpGatewayError
  final case class DescribeCdpDlErr(error: CdpDlClientError)                 extends CdpGatewayError
  final case class CdpDlWrongStatusErr(environment: String, status: String)  extends CdpGatewayError
  final case class DatalakeNotFound(environment: String)                     extends CdpGatewayError
}
