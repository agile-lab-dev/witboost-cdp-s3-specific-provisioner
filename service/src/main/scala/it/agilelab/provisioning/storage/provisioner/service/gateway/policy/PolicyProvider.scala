package it.agilelab.provisioning.storage.provisioner.service.gateway.policy

import it.agilelab.provisioning.storage.provisioner.core.models.StorageSpace

/** A PolicyProvider trait which is able to generate policies
  */
trait PolicyProvider {

  /** Generate a read only policy
    * @param storageSpace sequence of storage space for which to grant read only access
    * @return Left(Errors.Error) if something goes wrong during the policy generation; Right(AccessPolicy) otherwise
    */
  def getReadOnlyAccessPolicy(storageSpace: StorageSpace): AccessPolicy

  /** Generate a full-access policy
    * @param storageSpace sequence of storage space for which to grant full access
    * @return Left(Errors.Error) if something goes wrong during the policy generation; Right(AccessPolicy) otherwise
    */
  def getFullAccessPolicy(storageSpace: StorageSpace): AccessPolicy

}

object PolicyProvider {

  def iam(): PolicyProvider = new AwsIamPolicyProvider()
}
