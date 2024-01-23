package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.policy

import cats.Show
import cats.implicits.showInterpolator
import it.agilelab.provisioning.commons.client.ranger.RangerClientError

trait RangerPolicyGatewayError extends Exception with Product with Serializable

object RangerPolicyGatewayError {

  final case class UpsertPolicyErr(error: RangerClientError) extends RangerPolicyGatewayError
  final case class FindPolicyErr(error: RangerClientError)   extends RangerPolicyGatewayError
  final case class DeletePolicyErr(error: RangerClientError) extends RangerPolicyGatewayError

  implicit def showPolicyGatewayError: Show[RangerPolicyGatewayError] = Show.show {
    case UpsertPolicyErr(error) => show"UpsertPolicyErr($error)"
    case FindPolicyErr(error)   => show"FindPolicyErr($error)"
    case DeletePolicyErr(error) => show"DeletePolicyErr($error)"
  }
}
