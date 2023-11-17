package it.agilelab.provisioning.storage.provisioner.service.gateway.policy

import cats.implicits._
import it.agilelab.provisioning.aws.iam.gateway.{ IamGateway, IamGatewayError }
import it.agilelab.provisioning.mesh.repository.Repository
import it.agilelab.provisioning.mesh.self.service.lambda.core.model.Role
import StorageSpaceAclGatewayError.{ FetchRolesErr, UpdateStorageSpaceAclErr }
import it.agilelab.provisioning.storage.provisioner.core.models.{ PolicyAttachment, S3CdpAcl, StorageSpace }

/** PolicyService implementation which is able to attach "output port centric" policies.
  * More specifically, for each output port is generated one policy that grants the access for only that output port.
  * This policy is named with a combination of data product and output port name.
  * The policy is attached to multiple roles
  *
  * @param policyProvider a policy provider
  * @param iamGateway a IAM gateway
  */
class S3CdpStorageSpaceAclGateway(
  policyProvider: PolicyProvider,
  iamGateway: IamGateway
) extends StorageSpaceAclGateway {

  override def updateAcl(
    storageSpace: StorageSpace
  ): Either[StorageSpaceAclGatewayError, S3CdpAcl] =
    for {
      // TODO here the data should come from the updateActl request, instead of relying in the role repo
      // roles               <- roleRepository.findAll(None).leftMap(e => FetchRolesErr(e))
      roles               <- Right(Seq.empty[Role])
      ownersRoleToAttach   = roles.filter(r => storageSpace.owners.contains(r.name))
      ownersRoleToDetach   = roles.filter(r => !storageSpace.owners.contains(r.name))
      usersRoleToAttach    = roles.filter(r => storageSpace.users.contains(r.name))
      usersRoleToDetach    = roles.filter(r => !storageSpace.users.contains(r.name))
      ownersPolicy         = policyProvider.getFullAccessPolicy(storageSpace)
      usersPolicy          = policyProvider.getReadOnlyAccessPolicy(storageSpace)
      _                   <- updatePolicies(ownersRoleToAttach, ownersRoleToDetach, ownersPolicy)
                               .leftMap(e => UpdateStorageSpaceAclErr(e))
      _                   <- updatePolicies(usersRoleToAttach, usersRoleToDetach, usersPolicy)
                               .leftMap(e => UpdateStorageSpaceAclErr(e))
      ownersAttachedPolicy = ownersRoleToAttach.map(r => PolicyAttachment(r.iamRole, ownersPolicy.id))
      usersAttachedPolicy  = usersRoleToAttach.map(r => PolicyAttachment(r.iamRole, usersPolicy.id))
      ownersDetachedPolicy = ownersRoleToDetach.map(r => PolicyAttachment(r.iamRole, ownersPolicy.id))
      usersDetachedPolicy  = usersRoleToDetach.map(r => PolicyAttachment(r.iamRole, usersPolicy.id))
    } yield S3CdpAcl(
      ownersAttachedPolicy ++ usersAttachedPolicy,
      ownersDetachedPolicy ++ usersDetachedPolicy
    )

  private def updatePolicies(
    toAttach: Seq[Role],
    toDetach: Seq[Role],
    policy: AccessPolicy
  ): Either[IamGatewayError, Unit] =
    for {
      _ <- toAttach.map(r => iamGateway.putRolePolicy(r.iamRole, policy.id, policy.document)).sequence
      _ <- toDetach.map(r => iamGateway.safeDeleteRolePolicy(r.iamRole, policy.id)).sequence
    } yield ()

}
