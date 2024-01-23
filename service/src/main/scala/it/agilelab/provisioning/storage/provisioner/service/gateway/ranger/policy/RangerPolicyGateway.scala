package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.policy

import cats.implicits.{ toBifunctorOps, toTraverseOps }
import it.agilelab.provisioning.commons.client.ranger.RangerClient
import it.agilelab.provisioning.commons.client.ranger.model.RangerPolicy
import it.agilelab.provisioning.storage.provisioner.core.models.PolicyAttachment
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.policy.RangerPolicyGatewayError.{
  DeletePolicyErr,
  FindPolicyErr,
  UpsertPolicyErr
}

class RangerPolicyGateway(
  rangerClient: RangerClient
) {

  /** Upserts a policy to allow access to a specific bucket and folder to the owner and user role.
    * If the policy already exists, it is updated with the received parameters.
    * Otherwise, a new one is created.
    * @param prefix The prefix to use for the policy name and description
    * @param bucket The bucket name
    * @param path The folder path inside the bucket
    * @param ownerRole The owner role
    * @param userRole The user role
    * @param zoneName Ranger Security Zone name to which the policy will be associated
    * @return Either a [[RangerPolicyGatewayError]] if there was an error upserting the policy,
    *         or the upserted [[PolicyAttachment]].
    */
  def upsertComponentPolicy(
    prefix: String,
    bucket: String,
    path: String,
    ownerRole: String,
    userRole: String,
    zoneName: String
  ): Either[RangerPolicyGatewayError, PolicyAttachment] = {
    val componentAccessPolicy =
      RangerPolicyGenerator.componentAccessPolicy(prefix, bucket, path, ownerRole, userRole, zoneName)
    upsertPolicy(componentAccessPolicy, Some(zoneName))
  }

  private def upsertPolicy(
    policy: RangerPolicy,
    zoneName: Option[String]
  ): Either[RangerPolicyGatewayError, PolicyAttachment] =
    for {
      optP <- rangerClient
                .findPolicyByName(policy.service, policy.name, getSafeZoneName(zoneName))
                .leftMap(e => FindPolicyErr(e))
      pl   <- optP
                .fold(rangerClient.createPolicy(policy))(p => rangerClient.updatePolicy(policy.copy(id = p.id)))
                .leftMap(e => UpsertPolicyErr(e))
    } yield PolicyAttachment(s"${pl.id.toString}", pl.name)

  private def getSafeZoneName(z: Option[String]): Option[String] = z match {
    case Some("") => None
    case s        => s
  }

  /** Deletes the policies that allowed access to a specific bucket and folder to a set of owners and users.
    *
    * @param prefix The prefix to use for the policy name and description
    * @param zoneName Ranger Security Zone name to which the policy to be deleted is associated
    * @return Either a [[RangerPolicyGatewayError]] if there was an error deleting the policy,
    *         or the list of deleted [[PolicyAttachment]].
    *         If a policy doesn't exist, the method still returns a Right but the policy won't be included in the Right result.
    */
  def deleteComponentPolicy(
    prefix: String,
    zoneName: String
  ): Either[RangerPolicyGatewayError, Seq[PolicyAttachment]] = {
    val componentAccessPolicy = RangerPolicyGenerator.componentAccessPolicy(prefix, "", "", "", "", zoneName)
    for {
      ownAP <- removePolicy(componentAccessPolicy, Some(zoneName))
    } yield ownAP.map(Seq(_)).getOrElse(Seq.empty[PolicyAttachment])
  }

  private def removePolicy(
    policy: RangerPolicy,
    zoneName: Option[String]
  ): Either[RangerPolicyGatewayError, Option[PolicyAttachment]] =
    for {
      optP <- rangerClient
                .findPolicyByName(policy.service, policy.name, getSafeZoneName(zoneName))
                .leftMap(e => FindPolicyErr(e))
      pl   <- optP
                .map(p =>
                  rangerClient
                    .deletePolicy(policy.copy(id = p.id))
                    .map(_ => PolicyAttachment(s"${p.id.toString}", policy.name))
                    .leftMap(e => DeletePolicyErr(e))
                )
                .sequence
    } yield pl

}

object RangerPolicyGateway {
  def default(
    rangerClient: RangerClient
  ): RangerPolicyGateway = new RangerPolicyGateway(rangerClient)

}
