package it.agilelab.provisioning.storage.provisioner.service.gateway.policy

import it.agilelab.provisioning.aws.iam.gateway.IamGatewayError
import it.agilelab.provisioning.mesh.repository.RepositoryError

trait StorageSpaceAclGatewayError extends Exception with Product with Serializable

object StorageSpaceAclGatewayError {
  final case class FetchRolesErr(error: RepositoryError)            extends StorageSpaceAclGatewayError
  final case class UpdateStorageSpaceAclErr(error: IamGatewayError) extends StorageSpaceAclGatewayError
}
