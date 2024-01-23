package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.zone

import it.agilelab.provisioning.commons.client.ranger.model.{ RangerSecurityZone, RangerSecurityZoneResources }
import org.scalatest.funsuite.AnyFunSuite

class RangerSecurityZoneGeneratorTest extends AnyFunSuite {

  test("default RangerSecurityZone") {
    val actual   = RangerSecurityZoneGenerator.securityZone(
      "name",
      "serviceName",
      Seq("bucketName"),
      Seq("path"),
      Seq("adminUser1"),
      Seq("adminUserGroup1"),
      Seq("auditUser1"),
      Seq("auditUserGroup1")
    )
    val expected = RangerSecurityZone(
      -1,
      "name",
      Map(
        "serviceName" -> RangerSecurityZoneResources(
          Seq(
            Map(
              "bucket" -> Seq("bucketName"),
              "path"   -> Seq("path")
            )
          )
        )
      ),
      isEnabled = true,
      Seq("adminUser1"),
      Seq("adminUserGroup1"),
      Seq("auditUser1"),
      Seq("auditUserGroup1")
    )
    assert(actual == expected)
  }

  test("securityZoneWithMergedServiceResources with empty zone") {
    val zone     = RangerSecurityZone(
      -1,
      "name",
      Map.empty,
      isEnabled = true,
      Seq("adminUser1"),
      Seq("adminUserGroup1"),
      Seq("auditUser1"),
      Seq("auditUserGroup1")
    )
    val actual   = RangerSecurityZoneGenerator.securityZoneWithMergedServiceResources(
      zone,
      "serviceName",
      Seq("bucket"),
      Seq("path")
    )
    val expected = RangerSecurityZone(
      -1,
      "name",
      Map(
        "serviceName" -> RangerSecurityZoneResources(
          Seq(
            Map(
              "bucket" -> Seq("bucket"),
              "path"   -> Seq("path")
            )
          )
        )
      ),
      isEnabled = true,
      Seq("adminUser1"),
      Seq("adminUserGroup1"),
      Seq("auditUser1"),
      Seq("auditUserGroup1")
    )
    assert(actual == expected)
  }

  test("securityZoneWithMergedServiceResources with non empty zone") {
    val zone     = RangerSecurityZone(
      -1,
      "name",
      Map(
        "impala" ->
          RangerSecurityZoneResources(
            Seq(
              Map(
                "database" -> Seq("db1_*"),
                "column"   -> Seq("*"),
                "table"    -> Seq("*")
              ),
              Map(
                "url"      -> Seq("s3a://bucket/folder1/*")
              )
            )
          )
      ),
      isEnabled = true,
      Seq("adminUser1"),
      Seq("adminUserGroup1"),
      Seq("auditUser1"),
      Seq("auditUserGroup1")
    )
    val actual   = RangerSecurityZoneGenerator.securityZoneWithMergedServiceResources(
      zone,
      "s3",
      Seq("bucket1"),
      Seq("path1")
    )
    val expected = RangerSecurityZone(
      -1,
      "name",
      Map(
        "s3"     ->
          RangerSecurityZoneResources(
            Seq(
              Map(
                "bucket" -> Seq("bucket1"),
                "path"   -> Seq("path1")
              )
            )
          ),
        "impala" ->
          RangerSecurityZoneResources(
            Seq(
              Map(
                "database" -> Seq("db1_*"),
                "column"   -> Seq("*"),
                "table"    -> Seq("*")
              ),
              Map(
                "url"      -> Seq("s3a://bucket/folder1/*")
              )
            )
          )
      ),
      isEnabled = true,
      Seq("adminUser1"),
      Seq("adminUserGroup1"),
      Seq("auditUser1"),
      Seq("auditUserGroup1")
    )
    assert(actual == expected)
  }

  test("securityZoneWithMergedServiceResources merge zone") {
    val zone     = RangerSecurityZone(
      -1,
      "name",
      Map(
        "s3" ->
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
      Seq("adminUser1"),
      Seq("adminUserGroup1"),
      Seq("auditUser1"),
      Seq("auditUserGroup1")
    )
    val actual   = RangerSecurityZoneGenerator.securityZoneWithMergedServiceResources(
      zone,
      "s3",
      Seq("bucket1"),
      Seq("path2")
    )
    val expected = RangerSecurityZone(
      -1,
      "name",
      Map(
        "s3" ->
          RangerSecurityZoneResources(
            Seq(
              Map(
                "bucket" -> Seq("bucket1"),
                "path"   -> Seq("path1", "path2")
              )
            )
          )
      ),
      isEnabled = true,
      Seq("adminUser1"),
      Seq("adminUserGroup1"),
      Seq("auditUser1"),
      Seq("auditUserGroup1")
    )
    assert(actual == expected)
  }

}
