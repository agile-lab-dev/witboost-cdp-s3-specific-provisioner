package it.agilelab.provisioning.storage.provisioner.service.gateway.policy

import com.cloudera.cdp.datalake.model.Datalake
import com.cloudera.cdp.environments.model.Environment
import it.agilelab.provisioning.commons.client.ranger.RangerClient
import it.agilelab.provisioning.commons.principalsmapping.{ CdpIamGroup, CdpIamPrincipals, CdpIamUser }
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGateway
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, PolicyAttachment, S3Cdp }
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.policy.RangerPolicyGateway
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.provider.{
  RangerGateway,
  RangerGatewayProvider
}
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.role.{
  OwnerRoleType,
  RangerRoleGateway,
  RangerRoleGenerator,
  UserRoleType
}
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.zone.{
  RangerSecurityZoneGateway,
  RangerSecurityZoneGenerator
}
import it.agilelab.provisioning.storage.provisioner.service.helpers.{ OutputPortFaker, ProvisionRequestFaker }
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class S3CdpStorageSpaceAclGatewayTest extends AnyFunSuite with MockFactory {

  val cdpGateway: CdpGateway               = mock[CdpGateway]
  val rangerGateway: RangerGatewayProvider = mock[RangerGatewayProvider]
  val rangerClient: RangerClient           = mock[RangerClient]

  val rangerSecurityZoneGateway: RangerSecurityZoneGateway = mock[RangerSecurityZoneGateway]
  val rangerRoleGateway: RangerRoleGateway                 = mock[RangerRoleGateway]
  val rangerPolicyGateway: RangerPolicyGateway             = mock[RangerPolicyGateway]

  val aclGateway = new S3CdpStorageSpaceAclGateway(
    cdpGateway,
    rangerGateway,
    "adminUser"
  )

  val datalake = new Datalake()
  datalake.setEnvironmentCrn("cdpEnvCrn")
  datalake.setDatalakeName("dlName")

  test("provisionAcl") {

    (cdpGateway.getEnvironment _).expects(*).returns(Right(new Environment()))
    (cdpGateway.getDataLake _).expects(*).returns(Right(datalake))
    (cdpGateway.getRangerHost _).expects(*).returns(Right("http://ranger-host"))
    (rangerGateway.getRangerClient _).expects(*).returns(Right(rangerClient))
    (rangerGateway.getRangerGateway _)
      .expects(*, *)
      .returns(
        new RangerGateway(
          zoneGateway = rangerSecurityZoneGateway,
          roleGateway = rangerRoleGateway,
          policyGateway = rangerPolicyGateway,
          datalake = datalake
        )
      )
    (rangerSecurityZoneGateway.upsertSecurityZone _)
      .expects(
        "domain_dp_name_0",
        "S3",
        "dlName",
        "bucket1",
        "mesh/domains/domain/data-products/dp-name/poc/0/*",
        "adminUser"
      )
      .returns(
        Right(
          RangerSecurityZoneGenerator.empty("domain_dp_name_0")
        )
      )
    (rangerRoleGateway.upsertRole _)
      .expects("domain_dp_name_0", OwnerRoleType, *, *, *, *, *)
      .returns(
        Right(
          RangerRoleGenerator.empty("")
        )
      )
    (rangerRoleGateway.upsertRole _)
      .expects("domain_dp_name_0_cmp_name", UserRoleType, *, *, *, *, *)
      .returns(
        Right(
          RangerRoleGenerator.empty("")
        )
      )
    (rangerPolicyGateway.upsertComponentPolicy _)
      .expects("domain_dp_name_0_cmp_name", "bucket1", "folder1", *, *, "domain_dp_name_0")
      .returns(Right(PolicyAttachment(id = "111", policyName = "pl1")))

    val o          = OutputPortFaker(S3Cdp(cdpEnvironment = "cdpEnvironment1", bucket = "bucket1", folder = "folder1")).build()
    val request    = ProvisionRequestFaker[DpCdp, S3Cdp](DpCdp())
      .withComponent(
        o
      )
      .build()
    val ownerUser  = "ownerUser1"
    val ownerGroup = "ownerGroup1"

    val expected = Right(
      Seq(
        PolicyAttachment("111", "pl1")
      )
    )

    val actual = aclGateway.provisionAcl(request.dataProduct, o, ownerUser, ownerGroup)

    assert(actual == expected)
  }

  test("unprovisionAcl") {
    (cdpGateway.getEnvironment _).expects(*).returns(Right(new Environment()))
    (cdpGateway.getDataLake _).expects(*).returns(Right(datalake))
    (cdpGateway.getRangerHost _).expects(*).returns(Right("http://ranger-host"))
    (rangerGateway.getRangerClient _).expects(*).returns(Right(rangerClient))
    (rangerGateway.getRangerGateway _)
      .expects(*, *)
      .returns(
        new RangerGateway(
          zoneGateway = rangerSecurityZoneGateway,
          roleGateway = rangerRoleGateway,
          policyGateway = rangerPolicyGateway,
          datalake = datalake
        )
      )
    (rangerPolicyGateway.deleteComponentPolicy _)
      .expects("domain_dp_name_0_cmp_name", "domain_dp_name_0")
      .returns(
        Right(Seq(PolicyAttachment(id = "111", policyName = "pl1"), PolicyAttachment(id = "222", policyName = "pl2")))
      )
    (rangerRoleGateway.deleteUserRole _)
      .expects("domain_dp_name_0_cmp_name")
      .returns(Right(()))
    val o        = OutputPortFaker(S3Cdp(cdpEnvironment = "cdpEnvironment1", bucket = "bucket1", folder = "folder1")).build()
    val expected = Right(
      Seq(
        PolicyAttachment("111", "pl1"),
        PolicyAttachment("222", "pl2")
      )
    )

    val actual = aclGateway.unprovisionAcl(o)

    assert(actual == expected)
  }

  test("updateAcl") {
    (cdpGateway.getEnvironment _).expects(*).returns(Right(new Environment()))
    (cdpGateway.getDataLake _).expects(*).returns(Right(datalake))
    (cdpGateway.getRangerHost _).expects(*).returns(Right("http://ranger-host"))
    (rangerGateway.getRangerClient _).expects(*).returns(Right(rangerClient))
    (rangerGateway.getRangerGateway _)
      .expects(*, *)
      .returns(
        new RangerGateway(
          zoneGateway = rangerSecurityZoneGateway,
          roleGateway = rangerRoleGateway,
          policyGateway = rangerPolicyGateway,
          datalake = datalake
        )
      )
    val userRole                    = RangerRoleGenerator.empty("")
    (rangerRoleGateway.upsertRole _)
      .expects("domain_dp_name_0_cmp_name", UserRoleType, *, *, *, List("user1", "user2"), List("group1"))
      .returns(
        Right(
          userRole
        )
      )
    val o                           = OutputPortFaker(S3Cdp(cdpEnvironment = "cdpEnvironment1", bucket = "bucket1", folder = "folder1")).build()
    val refs: Set[CdpIamPrincipals] = Set(
      CdpIamUser("", "user1", ""),
      CdpIamUser("", "user2", ""),
      CdpIamGroup("group1", "")
    )
    val expected                    = Right(userRole)

    val actual = aclGateway.updateAcl(o, refs)

    assert(actual == expected)
  }

}
