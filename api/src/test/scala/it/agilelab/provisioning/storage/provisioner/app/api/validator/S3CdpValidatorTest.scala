package it.agilelab.provisioning.storage.provisioner.app.api.validator

import cats.data.Validated
import cats.data.Validated.valid
import io.circe.Json
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClientError.DescribeDlErr
import it.agilelab.provisioning.mesh.self.service.api.model.Component.{ DataContract, OutputPort }
import it.agilelab.provisioning.mesh.self.service.api.model.{ DataProduct, ProvisionRequest }
import it.agilelab.provisioning.storage.provisioner.app.api.validator.S3CdpValidator.validator
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGateway
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGatewayError.DescribeCdpDlErr
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, S3Cdp }
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class S3CdpValidatorTest extends AnyFunSuite with MockFactory with Matchers {

  test("validator with x return y") {
    val cdpGateway = mock[CdpGateway]

    (cdpGateway.cdpEnvironmentExists _)
      .expects("cdpEnv")
      .returns(true)

    (cdpGateway.cdpDatalakeExists _)
      .expects("cdpEnv")
      .returns(true)

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
        devGroup = "devGroup",
        ownerGroup = "ownerGroup",
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
            folder = "mesh/domains/my-domain-name/data-products/my-dp-name/my-env/0/"
          )
        )
      )
    )

    val actual = validator(cdpGateway).validate(request)

    assert(actual == Right(valid(request)))
  }

  test("validator returns a Right with a ValidationFail if CDP environment doesn't exists") {
    val cdpGateway = mock[CdpGateway]

    (cdpGateway.cdpEnvironmentExists _)
      .expects("cdpEnv")
      .returns(false)

    (cdpGateway.cdpDatalakeExists _)
      .expects("cdpEnv")
      .returns(false)

    (cdpGateway.getStorageLocationBase _)
      .expects("cdpEnv")
      .returns(Left(DescribeCdpDlErr(DescribeDlErr("", new Throwable("")))))

    val request = ProvisionRequest(
      DataProduct[DpCdp](
        id = "urn:dmb:dp:my-dp-name:my-domain-name:0",
        name = "my-dp-name",
        domain = "my-domain-name",
        environment = "my-env",
        version = "0.1.2",
        dataProductOwner = "my-dp-owner",
        devGroup = "devGroup",
        ownerGroup = "ownerGroup",
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
            folder = "mesh/domains/my-domain-name/data-products/my-dp-name/my-env/0/"
          )
        )
      )
    )

    val actual = validator(cdpGateway).validate(request)

    assert(actual.isRight)
    actual.getOrElse(null) match {
      case Validated.Valid(_)   => fail("Expected invalid")
      case Validated.Invalid(e) => e.size should be(3)
    }
  }

}
