package it.agilelab.provisioning.storage.provisioner.app.api.handlers

import cats.effect.IO
import io.circe.Decoder
import it.agilelab.provisioning.api.generated.Resource
import it.agilelab.provisioning.api.generated.definitions.{
  DescriptorKind,
  ProvisioningRequest,
  SystemError,
  ValidationError,
  ValidationResult
}
import it.agilelab.provisioning.mesh.self.service.api.controller.ProvisionerController
import it.agilelab.provisioning.mesh.self.service.api.model.{
  ApiError,
  ApiRequest,
  ApiResponse,
  Component,
  ProvisioningDescriptor
}
import it.agilelab.provisioning.storage.provisioner.app.api.SpecificProvisionerHandler
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, S3Cdp }
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{ Method, Request, Response, Status }
import org.scalamock.scalatest.MockFactory
import org.http4s.circe.CirceEntityDecoder._
import org.http4s.circe.CirceEntityEncoder._

class ValidateHandlerTest extends HandlerTestBase with MockFactory {

  "The server" should "return a 200 with no error when the descriptor validation succeeds" in {
    val provisioner                = mock[ProvisionerController[DpCdp, S3Cdp]]
    (provisioner
      .validate(_: ApiRequest.ProvisioningRequest)(
        _: Decoder[ProvisioningDescriptor[DpCdp]],
        _: Decoder[Component[S3Cdp]]
      ))
      .expects(*, *, *)
      .returns(Right(ApiResponse.valid()))
    val handler                    = new SpecificProvisionerHandler(provisioner)
    val response: IO[Response[IO]] = new Resource[IO]()
      .routes(handler)
      .orNotFound
      .run(
        Request(method = Method.POST, uri = uri"/v1/validate")
          .withEntity(ProvisioningRequest(DescriptorKind.ComponentDescriptor, "a-yaml-descriptor"))
      )
    val expected                   = ValidationResult(valid = true)

    check[ValidationResult](response, Status.Ok, Some(expected)) shouldBe true
  }

  it should "return a 200 with a list of errors when the validation fails" in {
    val errors                     = Vector("first error", "second error")
    val provisioner                = mock[ProvisionerController[DpCdp, S3Cdp]]
    (provisioner
      .validate(_: ApiRequest.ProvisioningRequest)(
        _: Decoder[ProvisioningDescriptor[DpCdp]],
        _: Decoder[Component[S3Cdp]]
      ))
      .expects(*, *, *)
      .returns(Right(ApiResponse.invalid(errors: _*)))
    val handler                    = new SpecificProvisionerHandler(provisioner)
    val response: IO[Response[IO]] = new Resource[IO]()
      .routes(handler)
      .orNotFound
      .run(
        Request(method = Method.POST, uri = uri"/v1/validate")
          .withEntity(ProvisioningRequest(DescriptorKind.ComponentDescriptor, "a-wrong-yaml-descriptor"))
      )

    val expected = ValidationResult(valid = false, error = Some(ValidationError(errors)))

    check[ValidationResult](response, Status.Ok, Some(expected)) shouldBe true
  }

  it should "return a 500 with with meaningful error on validate exception" in {
    val error                      = "first error"
    val provisioner                = mock[ProvisionerController[DpCdp, S3Cdp]]
    (provisioner
      .validate(_: ApiRequest.ProvisioningRequest)(
        _: Decoder[ProvisioningDescriptor[DpCdp]],
        _: Decoder[Component[S3Cdp]]
      ))
      .expects(*, *, *)
      .returns(Left(ApiError.sysErr(error)))
    val handler                    = new SpecificProvisionerHandler(provisioner)
    val response: IO[Response[IO]] = new Resource[IO]()
      .routes(handler)
      .orNotFound
      .run(
        Request(method = Method.POST, uri = uri"/v1/validate")
          .withEntity(ProvisioningRequest(DescriptorKind.ComponentDescriptor, "a-wrong-yaml-descriptor"))
      )
    val expected                   = SystemError(error)

    check[SystemError](response, Status.InternalServerError, Some(expected)) shouldBe true
  }

}
