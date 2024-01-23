package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.zone

import io.circe.Json
import it.agilelab.provisioning.commons.client.ranger.RangerClient
import it.agilelab.provisioning.commons.client.ranger.RangerClientError.{
  CreateSecurityZoneErr,
  FindSecurityZoneByNameErr,
  UpdateSecurityZoneErr
}
import it.agilelab.provisioning.commons.client.ranger.model.{
  RangerSecurityZone,
  RangerSecurityZoneResources,
  RangerService
}
import it.agilelab.provisioning.commons.http.HttpErrors.ConnectionErr
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.RangerGatewayTestSupport
import it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.zone.RangerSecurityZoneGatewayError.FindServiceErr
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class RangerSecurityZoneGatewayTest extends AnyFunSuite with MockFactory with RangerGatewayTestSupport {

  val client: RangerClient = stub[RangerClient]
  val gateway              = new RangerSecurityZoneGateway(client)

  val zone: RangerSecurityZone = RangerSecurityZone(
    1,
    "zone_name_0",
    Map(
      "nifi" -> RangerSecurityZoneResources(
        Seq(
          Map(
            "nifi-resource" -> Seq("res1", "res2")
          )
        )
      )
    ),
    isEnabled = true,
    List("adminUser1", "adminUser2"),
    List("adminUserGroup1", "adminUserGroup2"),
    List("auditUser1", "auditUser2"),
    List("auditUserGroup1", "auditUserGroup2")
  )

  val zoneUpdated: RangerSecurityZone = RangerSecurityZone(
    1,
    "zone_name_0",
    Map(
      "cm_s3" -> RangerSecurityZoneResources(
        Seq(
          Map(
            "bucket" -> Seq("bucket1"),
            "path"   -> Seq("path1")
          )
        )
      ),
      "nifi"  -> RangerSecurityZoneResources(
        Seq(
          Map(
            "nifi-resource" -> Seq("res1", "res2")
          )
        )
      )
    ),
    isEnabled = true,
    List("adminUser1", "adminUser2"),
    List("adminUserGroup1", "adminUserGroup2"),
    List("auditUser1", "auditUser2"),
    List("auditUserGroup1", "auditUserGroup2")
  )

  val s3Service: RangerService = RangerService(
    1,
    isEnabled = true,
    "S3",
    "cm_s3",
    "S3",
    Map(
      "cluster.name" -> "cdpDlName"
    )
  )

  val dp: DataProduct[Json] = DataProduct[Json](
    id = "urn:dmb:dp:domain:name:0",
    name = "name",
    domain = "domain",
    environment = "environment",
    version = "0.0.0",
    dataProductOwner = "dataProductOwner",
    devGroup = "devGroup",
    ownerGroup = "ownerGroup",
    specific = Json.obj(),
    components = Seq.empty
  )

  test("upsertSecurityZone return Right(RangerSecurityZone) when security zone already exists") {
    (client.findSecurityZoneByName _)
      .when("zone_name_0")
      .returns(Right(Some(zone)))

    (client.findAllServices _)
      .when()
      .returns(Right(List(s3Service, RangerService(1, isEnabled = true, "nifi", "cm_nifi", "NiFi", Map.empty))))

    (client.updateSecurityZone _)
      .when(zoneUpdated)
      .returns(Right(zoneUpdated))

    val actual   = gateway.upsertSecurityZone("zone_name_0", "S3", "cdpDlName", "bucket1", "path1", "adminUser")
    val expected = Right(zoneUpdated)
    assert(actual == expected)
  }

  test("upsertSecurityZone return Right(RangerSecurityZone) when security zone does not exists") {
    val newZone = RangerSecurityZone(
      -1,
      "zone_name_0",
      Map(
        "cm_s3" ->
          RangerSecurityZoneResources(
            Seq(
              Map(
                "bucket" -> Seq("bucket1"),
                "path"   -> Seq("path1")
              )
            )
          )
      ),
      isEnabled = true,
      Seq("adminUser"),
      Seq.empty,
      Seq("adminUser"),
      Seq.empty
    )
    (client.findSecurityZoneByName _)
      .when("zone_name_0")
      .returns(Right(None))

    (client.findAllServices _)
      .when()
      .returns(Right(List(s3Service, RangerService(1, isEnabled = true, "nifi", "cm_nifi", "NiFi", Map.empty))))

    (client.createSecurityZone _)
      .when(newZone)
      .returns(Right(newZone))

    val actual   = gateway.upsertSecurityZone("zone_name_0", "S3", "cdpDlName", "bucket1", "path1", "adminUser")
    val expected = Right(newZone)
    assert(actual == expected)
  }

  test(
    "upsertSecurityZone return Left(UpsertSecurityZoneErr(FindSecurityZoneByNameErr)) when security zone is not available"
  ) {
    (client.findSecurityZoneByName _)
      .when("zone_name_0")
      .returns(
        Left(
          FindSecurityZoneByNameErr("zone_name_0", ConnectionErr("xx", new IllegalArgumentException("x")))
        )
      )

    val actual = gateway.upsertSecurityZone("zone_name_0", "S3", "cdpDlName", "bucket1", "path1", "adminUser")

    assertUpsertZoneWithFindZoneErr(actual, "zone_name_0", ConnectionErr("xx", new IllegalArgumentException("x")))
  }

  test("upsertSecurityZone return Left(FindServiceErr) when service is not available") {
    (client.findSecurityZoneByName _)
      .when("zone_name_0")
      .returns(Right(Some(zone)))

    (client.findAllServices _)
      .when()
      .returns(Right(List.empty))

    val actual   = gateway.upsertSecurityZone("zone_name_0", "S3", "cdpDlName", "bucket1", "path1", "adminUser")
    val expected = Left(
      FindServiceErr("Unable to find service with type S3 in cluster cdpDlName.")
    )
    assert(actual == expected)
  }

  test("upsertSecurityZone return Left(UpsertSecurityZoneErr) when update fails") {
    (client.findSecurityZoneByName _)
      .when("zone_name_0")
      .returns(Right(Some(zone)))

    (client.findAllServices _)
      .when()
      .returns(Right(List(s3Service, RangerService(1, isEnabled = true, "nifi", "cm_nifi", "NiFi", Map.empty))))

    (client.updateSecurityZone _)
      .when(zoneUpdated)
      .returns(
        Left(
          UpdateSecurityZoneErr(zone, ConnectionErr("xx", new IllegalArgumentException("x")))
        )
      )

    val actual = gateway.upsertSecurityZone("zone_name_0", "S3", "cdpDlName", "bucket1", "path1", "adminUser")

    assertUpsertZoneWithUpdateZoneErr(actual, zone, ConnectionErr("xx", new IllegalArgumentException("x")))
  }

  test("upsertSecurityZone return Left(UpsertSecurityZoneErr) when create fails") {
    val zoneToBeCreated = RangerSecurityZone(
      -1,
      "zone_name_0",
      Map(
        "cm_s3" ->
          RangerSecurityZoneResources(
            Seq(
              Map(
                "bucket" -> Seq("bucket1"),
                "path"   -> Seq("path1")
              )
            )
          )
      ),
      isEnabled = true,
      Seq("adminUser"),
      Seq.empty,
      Seq("adminUser"),
      Seq.empty
    )
    (client.findSecurityZoneByName _)
      .when("zone_name_0")
      .returns(Right(None))

    (client.findAllServices _)
      .when()
      .returns(Right(List(s3Service, RangerService(1, isEnabled = true, "nifi", "cm_nifi", "NiFi", Map.empty))))

    (client.createSecurityZone _)
      .when(zoneToBeCreated)
      .returns(
        Left(
          CreateSecurityZoneErr(zoneToBeCreated, ConnectionErr("xx", new IllegalArgumentException("x")))
        )
      )

    val actual = gateway.upsertSecurityZone("zone_name_0", "S3", "cdpDlName", "bucket1", "path1", "adminUser")

    assertUpsertZoneWithCreateZoneErr(actual, zoneToBeCreated, ConnectionErr("xx", new IllegalArgumentException("x")))
  }

}
