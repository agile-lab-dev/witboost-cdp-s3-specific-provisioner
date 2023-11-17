package it.agilelab.provisioning.storage.provisioner.service.gateway

import it.agilelab.provisioning.mesh.repository.Repository
import it.agilelab.provisioning.mesh.self.service.api.model.Component.{ DataContract, OutputPort }
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.mesh.self.service.core.model.ProvisionCommand
import it.agilelab.provisioning.mesh.self.service.lambda.core.model.{ DataProductEntityKey, Domain }
import it.agilelab.provisioning.storage.provisioner.core.models._
import io.circe.Json
import it.agilelab.provisioning.storage.provisioner.core.models
import it.agilelab.provisioning.storage.provisioner.core.models.{
  Acl,
  DpCdp,
  PolicyAttachment,
  S3Cdp,
  S3CdpAcl,
  S3CdpResources,
  StorageSpace
}
import it.agilelab.provisioning.storage.provisioner.service.gateway.mapper.StorageSpaceMapper
import it.agilelab.provisioning.storage.provisioner.service.gateway.policy.StorageSpaceAclGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.storage.StorageSpaceGateway
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class S3CdpComponentGatewayTest extends AnyFunSuite with MockFactory {

  val storageSpaceMapper: StorageSpaceMapper         = mock[StorageSpaceMapper]
  val storageSpaceGateway: StorageSpaceGateway       = mock[StorageSpaceGateway]
  val storageSpaceAclGateway: StorageSpaceAclGateway = mock[StorageSpaceAclGateway]

  val s3CdpResourceGateway =
    new S3CdpComponentGateway(
      storageSpaceMapper,
      storageSpaceGateway,
      storageSpaceAclGateway
    )

  test("create return Right") {
    val provisionRequest = ProvisionRequest[DpCdp, S3Cdp](
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
      Some(
        OutputPort(
          id = "urn:dmb:cmp:dm-name:dp-name:1:sources",
          name = "sources",
          description = "description",
          version = "version",
          dataContract = DataContract(
            schema = Seq.empty
          ),
          specific = S3Cdp(
            "x",
            "a-path/x/",
            "cdpEnv",
            // TODO need review, acl are not required to be passed in specific section
            Acl(
              owners = Seq("own1", "own2"),
              users = Seq("usr1", "usr2")
            )
          )
        )
      )
    )

    inSequence(
      (storageSpaceMapper.map _)
        .expects(*, *)
        .once()
        .returns(Right(StorageSpace("my-id", "my-bucket", "my-path", Seq("owner1"), Seq("owners2")))),
      (storageSpaceGateway.create _)
        .expects(StorageSpace("my-id", "my-bucket", "my-path", Seq("owner1"), Seq("owners2")))
        .once()
        .returns(Right())
//      (storageSpaceAclGateway.updateAcl _)
//        .expects(StorageSpace("my-id", "my-bucket", "my-path", Seq("owner1"), Seq("owners2")))
//        .once()
//        .returns(Right(S3CdpAcl(Seq(PolicyAttachment("x", "y")), Seq.empty[PolicyAttachment])))
    )
    val actual   = s3CdpResourceGateway.create(ProvisionCommand("x", provisionRequest))
    val expected = Right(
      S3CdpResources(
        "my-id",
        "my-bucket",
        "my-path",
        models.S3CdpAcl(Seq.empty[PolicyAttachment], Seq.empty[PolicyAttachment])
      )
    )
    assert(actual == expected)
  }

  test("destroy return Right") {
    val provisionRequest = ProvisionRequest[DpCdp, S3Cdp](
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
      Some(
        OutputPort(
          id = "urn:dmb:cmp:dm-name:dp-name:1:sources",
          name = "sources",
          description = "description",
          version = "version",
          dataContract = DataContract(
            schema = Seq.empty
          ),
          specific = S3Cdp(
            "x",
            "a-path/x/",
            "cdpEnv",
            // TODO need review, acl are not required to be passed in specific section
            Acl(
              owners = Seq("own1", "own2"),
              users = Seq("usr1", "usr2")
            )
          )
        )
      )
    )

    inSequence(
      (storageSpaceMapper.map _)
        .expects(*, *)
        .once()
        .returns(Right(StorageSpace("my-id", "my-bucket", "my-path", Seq("owner1"), Seq("owners2")))),
      (storageSpaceAclGateway.updateAcl _)
        .expects(StorageSpace("my-id", "my-bucket", "my-path", Seq.empty[String], Seq.empty[String]))
        .once()
        .returns(Right(models.S3CdpAcl(Seq.empty[PolicyAttachment], Seq(PolicyAttachment("x", "y")))))
    )
    val actual   = s3CdpResourceGateway.destroy(ProvisionCommand("x", provisionRequest))
    val expected = Right(
      S3CdpResources(
        "my-id",
        "my-bucket",
        "my-path",
        models.S3CdpAcl(Seq.empty[PolicyAttachment], Seq(PolicyAttachment("x", "y")))
      )
    )
    assert(actual == expected)
  }
}
