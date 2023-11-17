package it.agilelab.provisioning.storage.provisioner.service.gateway.policy

import cats.implicits.toShow
import it.agilelab.provisioning.aws.iam.policy.Action._
import it.agilelab.provisioning.aws.iam.policy.Effect.ALLOW
import it.agilelab.provisioning.aws.iam.policy.Resource.S3Resource
import it.agilelab.provisioning.aws.iam.policy.{ Action, Policy, Statement }
import it.agilelab.provisioning.storage.provisioner.core.models.StorageSpace

class AwsIamPolicyProvider extends PolicyProvider {

  /** Generate a read only policy
    *
    * @param storageSpace sequence of storage space for which to grant read only access
    * @return Left(Errors.Error) if something goes wrong during the policy generation; Right(AccessPolicy) otherwise
    */
  override def getReadOnlyAccessPolicy(
    storageSpace: StorageSpace
  ): AccessPolicy =
    AccessPolicy(
      policyName("read-only", storageSpace),
      policyDocument(S3_GET_OBJECT, storageSpace).show
    )

  /** Generate a full-access policy
    *
    * @param storageSpace sequence of storage space for which to grant full access
    * @return Left(Errors.Error) if something goes wrong during the policy generation; Right(AccessPolicy) otherwise
    */
  override def getFullAccessPolicy(
    storageSpace: StorageSpace
  ): AccessPolicy =
    AccessPolicy(
      policyName("full-access", storageSpace),
      policyDocument(S3_ALL, storageSpace).show
    )

  private def policyName(prefix: String, storageSpace: StorageSpace) =
    "%s.%s".format(prefix, storageSpace.id.replace(":", "."))

  private def policyDocument(action: Action, storageSpace: StorageSpace): Policy =
    Policy(Seq(Statement(ALLOW, Seq(action), Seq(S3Resource(storageSpace.bucket, sanitizePath(storageSpace.path))))))

  private def sanitizePath(path: String): String =
    safelyAppend(safelyAppend(path, "/"), "*")

  private def safelyAppend(path: String, suffix: String): String =
    if (path.endsWith(suffix)) path
    else path + suffix

}
