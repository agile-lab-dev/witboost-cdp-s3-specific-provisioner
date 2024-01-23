package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.provider

import cats.implicits.toBifunctorOps
import com.cloudera.cdp.datalake.model.Datalake
import it.agilelab.provisioning.commons.client.ranger.RangerClient
import it.agilelab.provisioning.commons.http.Auth.BasicCredential
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.policy.RangerPolicyGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.role.RangerRoleGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.zone.RangerSecurityZoneGateway

import scala.util.Try

/** Class to create the gateways that communicate with the Cloudera Ranger to manage security zones, roles and policies
  *
  * @param username Deploy user username with the appropriate permissions
  * @param password Deploy user password
  */
class RangerGatewayProvider(
  username: String,
  password: String
) {

  def getRangerClient(
    rangerHost: String
  ): Either[RangerGatewayProviderError, RangerClient] =
    Try(RangerClient.defaultWithAudit(prepRangerHost(rangerHost), BasicCredential(username, password))).toEither
      .leftMap(e => RangerGatewayProviderError(e))

  protected def prepRangerHost(host: String): String =
    if (host.endsWith("/"))
      host.substring(0, host.length - 1)
    else host

  def getRangerGateway(
    rangerClient: RangerClient,
    datalake: Datalake
  ): RangerGateway =
    new RangerGateway(
      RangerSecurityZoneGateway.default(rangerClient),
      RangerRoleGateway.default(rangerClient),
      RangerPolicyGateway.default(rangerClient),
      datalake
    )

}
