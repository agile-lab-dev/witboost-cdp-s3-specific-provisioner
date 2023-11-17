package it.agilelab.provisioning.storage.provisioner.app.api.validator

import cats.data.Validated.valid
import io.circe.Json
import it.agilelab.provisioning.mesh.self.service.api.model.Component.{ DataContract, OutputPort }
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.storage.provisioner.app.api.validator.S3CdpValidator.validator
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGateway
import it.agilelab.provisioning.storage.provisioner.core.models.{ Acl, DpCdp, S3Cdp }
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class S3CdpValidatorTest extends AnyFunSuite with MockFactory {

  test("validator with x return y") {
    val cdpGateway = mock[CdpGateway]

    (cdpGateway.getStorageLocationBase _)
      .expects("cdpEnv")
      .returns(Right("s3a://my-bucket/path"))

    val request = ProvisionRequest(
      DataProduct[DpCdp](
        id = "urn:dmb:dp:my-dp-name:my-domain-name:0",
        name = "my-dp-name",
        domain = "my-domain-name",
        environment = "my-env",
        version = "0.1.2",
        dataProductOwner = "my-dp-owner",
        specific = DpCdp(),
        components = Seq.empty[Json]
      ),
      Some(
        OutputPort[S3Cdp](
          id = "id",
          name = "name",
          description = "desc",
          version = "1",
          dataContract = DataContract(
            schema = Seq.empty
          ),
          specific = S3Cdp(
            cdpEnvironment = "cdpEnv",
            bucket = "my-bucket",
            folder = "mesh/domains/my-domain-name/data-products/my-dp-name/my-env/0/",
            acl = Acl(
              owners = Seq("x"),
              users = Seq.empty
            )
          )
        )
      )
    )

    val actual = validator(cdpGateway).validate(request)

    assert(actual == Right(valid(request)))
  }
}