package it.agilelab.provisioning.storage.provisioner.service.gateway.storage

import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import it.agilelab.provisioning.storage.provisioner.core.models.StorageSpace
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class S3CdpStorageSpaceGatewayTest extends AnyFunSuite with MockFactory {

  val s3Gateway: S3Gateway = mock[S3Gateway]

  test("create") {
    (s3Gateway.createFolder _)
      .expects("bucket", "path")
      .once()
      .returns(Right())

    val s3CdpStorageSpaceGateway = new S3CdpStorageSpaceGateway(s3Gateway)
    s3CdpStorageSpaceGateway.create(
      StorageSpace(
        "id",
        "bucket",
        "path"
      )
    )
  }
}
