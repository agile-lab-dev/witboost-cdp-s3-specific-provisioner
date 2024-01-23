package it.agilelab.provisioning.storage.provisioner.service.gateway.policy

import it.agilelab.provisioning.mesh.self.service.api.model.Component.{ DataContract, OutputPort }
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import io.circe.Json
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, S3Cdp, StorageSpace }
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class AwsIamPolicyProviderTest extends AnyFunSuite with MockFactory {

  val policyProvider = new AwsIamPolicyProvider()

  val dataProduct: DataProduct[DpCdp] = DataProduct(
    id = "urn:dmb:dp:dm-name:dp-name:1",
    name = "dp-name",
    domain = "dm-name",
    environment = "environment",
    version = "version",
    dataProductOwner = "data product owner",
    devGroup = "devGroup",
    ownerGroup = "ownerGroup",
    specific = DpCdp(),
    components = Seq.empty[Json]
  )
  val component: OutputPort[S3Cdp]    = OutputPort(
    id = "urn:dmb:cmp:dm-name:dp-name:1:sources",
    name = "sources",
    description = "description",
    version = "version",
    dataContract = DataContract(
      schema = Seq.empty
    ),
    specific = S3Cdp(
      "cdpEnv",
      "a-path/x/",
      "env"
    )
  )

  test("getFullAccessPolicy") {
    val storageSpace = StorageSpace("my:id", "a-bucket", "a-path/x/")
    val actual       = policyProvider.getFullAccessPolicy(storageSpace)
    val expected     = AccessPolicy(
      "full-access.my.id",
      "{\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"s3:*\"],\"Resource\":[\"arn:aws:s3:::a-bucket/a-path/x/*\"]}]}"
    )
    assert(actual == expected)
  }

  test("getReadOnlyAccessPolicy") {
    val storageSpace = StorageSpace("my:id", "a-bucket", "a-path/x/")
    val actual       = policyProvider.getReadOnlyAccessPolicy(storageSpace)
    val expected     = AccessPolicy(
      "read-only.my.id",
      "{\"Statement\":[{\"Effect\":\"Allow\",\"Action\":[\"s3:GetObject\"],\"Resource\":[\"arn:aws:s3:::a-bucket/a-path/x/*\"]}]}"
    )
    assert(actual == expected)
  }
}
