package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.zone

import cats.Show
import cats.implicits.showInterpolator
import it.agilelab.provisioning.commons.client.ranger.RangerClientError
import it.agilelab.provisioning.mesh.repository.RepositoryError

trait RangerSecurityZoneGatewayError extends Exception with Product with Serializable

object RangerSecurityZoneGatewayError {
  final case class RangerSecurityZoneGatewayInitErr(error: RepositoryError) extends RangerSecurityZoneGatewayError
  final case class UpsertSecurityZoneErr(error: RangerClientError)          extends RangerSecurityZoneGatewayError
  final case class FindSecurityZoneOwnerErr(error: RepositoryError)         extends RangerSecurityZoneGatewayError
  final case class FindServiceErr(error: String)                            extends RangerSecurityZoneGatewayError

  implicit def showRangerSecurityZoneGatewayError: Show[RangerSecurityZoneGatewayError] =
    Show.show {
      case RangerSecurityZoneGatewayInitErr(error) => show"RangerSecurityZoneGatewayInitErr($error)"
      case UpsertSecurityZoneErr(error)            => show"UpsertSecurityZoneErr($error)"
      case FindSecurityZoneOwnerErr(error)         => show"FindSecurityZoneOwnerErr($error)"
      case FindServiceErr(error)                   => show"FindServiceErr($error)"
    }
}
