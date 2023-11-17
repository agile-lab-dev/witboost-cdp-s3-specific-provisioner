package it.agilelab.provisioning.storage.provisioner.service.gateway.storage

import cats.implicits.toBifunctorOps
import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import StorageSpaceGatewayError.CreateStorageSpaceErr
import it.agilelab.provisioning.storage.provisioner.core.models.StorageSpace

class S3CdpStorageSpaceGateway(s3Gateway: S3Gateway) extends StorageSpaceGateway {
  override def create(storageSpace: StorageSpace): Either[StorageSpaceGatewayError, Unit] =
    s3Gateway
      .createFolder(storageSpace.bucket, storageSpace.path)
      .leftMap { e =>
        CreateStorageSpaceErr(e)
      }
}
