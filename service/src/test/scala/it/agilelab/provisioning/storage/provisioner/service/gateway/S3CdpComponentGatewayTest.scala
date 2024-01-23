package it.agilelab.provisioning.storage.provisioner.service.gateway

import it.agilelab.provisioning.commons.principalsmapping.{
  CdpIamGroup,
  CdpIamPrincipals,
  CdpIamUser,
  PrincipalsMapper
}
import it.agilelab.provisioning.mesh.self.service.core.model.ProvisionCommand
import it.agilelab.provisioning.storage.provisioner.core.models
import it.agilelab.provisioning.storage.provisioner.core.models._
import it.agilelab.provisioning.storage.provisioner.service.gateway.mapper.StorageSpaceMapper
import it.agilelab.provisioning.storage.provisioner.service.gateway.policy.StorageSpaceAclGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.role.RangerRoleGenerator
import it.agilelab.provisioning.storage.provisioner.service.gateway.storage.StorageSpaceGateway
import it.agilelab.provisioning.storage.provisioner.service.helpers.{ OutputPortFaker, ProvisionRequestFaker }
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class S3CdpComponentGatewayTest extends AnyFunSuite with MockFactory {

  val storageSpaceMapper: StorageSpaceMapper               = mock[StorageSpaceMapper]
  val storageSpaceGateway: StorageSpaceGateway             = mock[StorageSpaceGateway]
  val storageSpaceAclGateway: StorageSpaceAclGateway       = mock[StorageSpaceAclGateway]
  val principalsMapper: PrincipalsMapper[CdpIamPrincipals] = mock[PrincipalsMapper[CdpIamPrincipals]]

  val s3CdpResourceGateway =
    new S3CdpComponentGateway(
      storageSpaceMapper,
      storageSpaceGateway,
      storageSpaceAclGateway,
      principalsMapper
    )

  val op                 = OutputPortFaker(S3Cdp(cdpEnvironment = "cdpEnvironment1", bucket = "bucket1", folder = "folder1")).build()
  val request            = ProvisionRequestFaker[DpCdp, S3Cdp](DpCdp())
    .withComponent(
      op
    )
    .build()
  val policiesAttachment = Seq(PolicyAttachment("1", "pl1"), PolicyAttachment("2", "pl2"))

  test("create") {
    (storageSpaceMapper.map _)
      .expects(request.dataProduct, *)
      .once()
      .returns(Right(StorageSpace("my-id", "bucket1", "folder1")))

    (storageSpaceGateway.create _)
      .expects(StorageSpace("my-id", "bucket1", "folder1"))
      .once()
      .returns(Right())

    (principalsMapper.map _)
      .expects(Set("dataProductOwner", "devGroup"))
      .returns(
        Map(
          "dataProductOwner" -> Right(CdpIamUser("", "dataProductOwner", "")),
          "devGroup"         -> Right(CdpIamGroup("devGroup", ""))
        )
      )

    (storageSpaceAclGateway.provisionAcl _)
      .expects(request.dataProduct, op, "dataProductOwner", "devGroup")
      .once()
      .returns(Right(policiesAttachment))

    val expected = Right(
      S3CdpResources(
        "my-id",
        "bucket1",
        "folder1",
        models.S3CdpAcl(policiesAttachment, Seq.empty[PolicyAttachment])
      )
    )

    val actual = s3CdpResourceGateway.create(ProvisionCommand("x", request))

    assert(actual == expected)
  }

  test("destroy") {
    (storageSpaceMapper.map _)
      .expects(request.dataProduct, *)
      .once()
      .returns(Right(StorageSpace("my-id", "bucket1", "folder1")))

    (storageSpaceAclGateway.unprovisionAcl _)
      .expects(op)
      .once()
      .returns(Right(policiesAttachment))

    val expected = Right(
      S3CdpResources(
        "my-id",
        "bucket1",
        "folder1",
        models.S3CdpAcl(Seq.empty[PolicyAttachment], policiesAttachment)
      )
    )

    val actual = s3CdpResourceGateway.destroy(ProvisionCommand("x", request))

    assert(actual == expected)
  }

  test("updateAcl") {
    val refs: Set[CdpIamPrincipals] = Set(
      CdpIamUser("", "user1", ""),
      CdpIamUser("", "user2", ""),
      CdpIamGroup("group1", "")
    )
    val rangerRole                  = RangerRoleGenerator.empty("")

    (storageSpaceAclGateway.updateAcl _)
      .expects(op, refs)
      .once()
      .returns(Right(rangerRole))

    val expected = Right(refs)

    val actual = s3CdpResourceGateway.updateAcl(ProvisionCommand("x", request), refs)

    assert(actual == expected)
  }

}
