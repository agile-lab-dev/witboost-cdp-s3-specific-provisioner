package it.agilelab.provisioning.storage.provisioner.service.gateway.storage

import it.agilelab.provisioning.aws.s3.gateway.S3GatewayError

sealed trait StorageSpaceGatewayError
object StorageSpaceGatewayError {
  final case class CreateStorageSpaceErr(error: S3GatewayError) extends StorageSpaceGatewayError
}
