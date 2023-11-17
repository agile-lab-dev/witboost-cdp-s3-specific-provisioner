package it.agilelab.provisioning.storage.provisioner.service.gateway.policy

import it.agilelab.provisioning.storage.provisioner.core.models.{ S3CdpAcl, StorageSpace }

/** An AclGateway trait
  * Which is able to generate and attach policies for S3 bucket/folder
  */
trait StorageSpaceAclGateway {
  def updateAcl(storageSpace: StorageSpace): Either[StorageSpaceAclGatewayError, S3CdpAcl]
}
