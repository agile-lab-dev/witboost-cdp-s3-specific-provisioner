package it.agilelab.provisioning.storage.provisioner.service.gateway.mapper

import it.agilelab.provisioning.mesh.self.service.api.model.Component.{ DataContract, OutputPort }
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import io.circe.Json
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGateway
import it.agilelab.provisioning.storage.provisioner.core.models.{ Acl, DpCdp, S3Cdp, StorageSpace }
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class StorageSpaceMapperTest extends AnyFunSuite with MockFactory {

  val cdpGateway: CdpGateway = stub[CdpGateway]
  val storageSpaceMapper     = new S3CdpStorageSpaceMapper

  test("map return Right") {
    val actual = storageSpaceMapper.map(
      DataProduct(
        id = "urn:dmb:dp:dm-name:dp-name:1",
        name = "dp-name",
        domain = "dm-name",
        environment = "environment",
        version = "version",
        dataProductOwner = "data product owner",
        specific = DpCdp(),
        components = Seq.empty[Json]
      ),
      OutputPort(
        id = "urn:dmb:cmp:dm-name:dp-name:1:sources",
        name = "sources",
        description = "description",
        version = "version",
        dataContract = DataContract(
          schema = Seq.empty
        ),
        specific = S3Cdp(
          cdpEnvironment = "cdpEnv",
          bucket = "my-bucket",
          folder = "a-path/x/",
          acl = Acl(
            owners = Seq("own1", "own2"),
            users = Seq("usr1", "usr2")
          )
        )
      )
    )

    val expected = Right(
      StorageSpace(
        id = "urn:dmb:cmp:dm-name:dp-name:1:sources:environment",
        bucket = "my-bucket",
        path = "a-path/x/",
        owners = Seq("own1", "own2"),
        users = Seq("usr1", "usr2")
      )
    )

    assert(actual == expected)
  }
}
