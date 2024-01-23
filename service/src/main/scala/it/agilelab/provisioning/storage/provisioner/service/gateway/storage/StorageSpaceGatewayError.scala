package it.agilelab.provisioning.storage.provisioner.service.gateway.storage

import cats.Show
import cats.implicits.showInterpolator
import it.agilelab.provisioning.aws.s3.gateway.S3GatewayError

sealed trait StorageSpaceGatewayError
object StorageSpaceGatewayError {
  final case class CreateStorageSpaceErr(error: S3GatewayError) extends StorageSpaceGatewayError

  implicit def showStorageSpaceGatewayError: Show[StorageSpaceGatewayError] =
    Show.show { case CreateStorageSpaceErr(error) =>
      show"CreateStorageSpaceErr($error)"
    }
}
