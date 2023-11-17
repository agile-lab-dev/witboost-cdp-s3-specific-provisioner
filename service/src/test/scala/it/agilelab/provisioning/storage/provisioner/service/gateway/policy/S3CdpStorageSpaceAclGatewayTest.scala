package it.agilelab.provisioning.storage.provisioner.service.gateway.policy

import it.agilelab.provisioning.aws.iam.gateway.IamGateway
import it.agilelab.provisioning.mesh.repository.Repository
import it.agilelab.provisioning.mesh.self.service.lambda.core.model.Role
import it.agilelab.provisioning.storage.provisioner.core.models.{ PolicyAttachment, S3CdpAcl, StorageSpace }
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class S3CdpStorageSpaceAclGatewayTest extends AnyFunSuite with MockFactory {

  val iamGateway: IamGateway = mock[IamGateway]

  val aclGateway = new S3CdpStorageSpaceAclGateway(
    new AwsIamPolicyProvider(),
    iamGateway
  )

  test("updateAcl") {
//    inSequence(
//      (iamGateway.putRolePolicy _).expects("iam-role-own1", "fa.my.id", *).once().returns(Right()),
//      (iamGateway.putRolePolicy _).expects("iam-role-own2", "fa.my.id", *).once().returns(Right()),
//      (iamGateway.safeDeleteRolePolicy _).expects("iam-role-x", "fa.my.id").once().returns(Right(false)),
//      (iamGateway.safeDeleteRolePolicy _).expects("iam-role-usr1", "fa.my.id").once().returns(Right(false)),
//      (iamGateway.safeDeleteRolePolicy _).expects("iam-role-usr2", "fa.my.id").once().returns(Right(true)),
//      (iamGateway.putRolePolicy _).expects("iam-role-usr1", "ro.my.id", *).once().returns(Right()),
//      (iamGateway.putRolePolicy _).expects("iam-role-usr2", "ro.my.id", *).once().returns(Right()),
//      (iamGateway.safeDeleteRolePolicy _).expects("iam-role-x", "ro.my.id").once().returns(Right(false)),
//      (iamGateway.safeDeleteRolePolicy _).expects("iam-role-own1", "ro.my.id").once().returns(Right(true)),
//      (iamGateway.safeDeleteRolePolicy _).expects("iam-role-own2", "ro.my.id").once().returns(Right(true))
//    )

    val actual   = aclGateway.updateAcl(
      StorageSpace(
        "my:id",
        "my-bucket",
        "my-path",
        Seq("own1", "own2"),
        Seq("usr1", "usr2")
      )
    )
    // TODO need to be updated when updateAcl is refactored
    val expected = Right(
//      S3CdpAcl(
//        Seq(
//          PolicyAttachment("iam-role-own1", "fa.my.id"),
//          PolicyAttachment("iam-role-own2", "fa.my.id"),
//          PolicyAttachment("iam-role-usr1", "ro.my.id"),
//          PolicyAttachment("iam-role-usr2", "ro.my.id")
//        ),
//        Seq(
//          PolicyAttachment("iam-role-x", "fa.my.id"),
//          PolicyAttachment("iam-role-usr1", "fa.my.id"),
//          PolicyAttachment("iam-role-usr2", "fa.my.id"),
//          PolicyAttachment("iam-role-x", "ro.my.id"),
//          PolicyAttachment("iam-role-own1", "ro.my.id"),
//          PolicyAttachment("iam-role-own2", "ro.my.id")
//        )
//      )
      S3CdpAcl(
        Seq.empty[PolicyAttachment],
        Seq.empty[PolicyAttachment]
      )
    )
    assert(actual == expected)
  }

}
