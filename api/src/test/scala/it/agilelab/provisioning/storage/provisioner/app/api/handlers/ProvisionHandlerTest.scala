package it.agilelab.provisioning.storage.provisioner.app.api.handlers

import cats.effect.IO
import io.circe.Decoder
import it.agilelab.provisioning.api.generated.Resource
import it.agilelab.provisioning.api.generated.definitions.{
  DescriptorKind,
  ProvisioningRequest,
  ProvisioningStatus,
  SystemError,
  ValidationError
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

class ProvisionHandlerTest extends HandlerTestBase with MockFactory {

  "The server" should "return a 200 with COMPLETED on a successful provision" in {
    val provisioner                = mock[ProvisionerController[DpCdp, S3Cdp]]
    (provisioner
      .provision(_: ApiRequest.ProvisioningRequest)(
        _: Decoder[ProvisioningDescriptor[DpCdp]],
        _: Decoder[Component[S3Cdp]]
      ))
      .expects(*, *, *)
      .returns(Right(ApiResponse.completed("fakeid", None)))
    val handler                    = new SpecificProvisionerHandler(provisioner)
    val response: IO[Response[IO]] = new Resource[IO]()
      .routes(handler)
      .orNotFound
      .run(
        Request(method = Method.POST, uri = uri"/v1/provision")
          .withEntity(ProvisioningRequest(DescriptorKind.ComponentDescriptor, "a-yaml-descriptor"))
      )
    val expected                   = ProvisioningStatus(ProvisioningStatus.Status.Completed, "")

    check[ProvisioningStatus](response, Status.Ok, Some(expected)) shouldBe true
  }

  it should "return a 400 with a list of errors if an error happens on provision" in {
    val errors                     = Vector("first error", "second error")
    val provisioner                = mock[ProvisionerController[DpCdp, S3Cdp]]
    (provisioner
      .provision(_: ApiRequest.ProvisioningRequest)(
        _: Decoder[ProvisioningDescriptor[DpCdp]],
        _: Decoder[Component[S3Cdp]]
      ))
      .expects(*, *, *)
      .returns(Left(ApiError.validErr(errors: _*)))
    val handler                    = new SpecificProvisionerHandler(provisioner)
    val response: IO[Response[IO]] = new Resource[IO]()
      .routes(handler)
      .orNotFound
      .run(
        Request(method = Method.POST, uri = uri"/v1/provision")
          .withEntity(ProvisioningRequest(DescriptorKind.ComponentDescriptor, "a-yaml-descriptor"))
      )
    val expected                   = ValidationError(errors)

    check[ValidationError](response, Status.BadRequest, Some(expected)) shouldBe true
  }

  it should "return a 500 with meaningful error on provision exception" in {
    val error                      = "first error"
    val provisioner                = mock[ProvisionerController[DpCdp, S3Cdp]]
    (provisioner
      .provision(_: ApiRequest.ProvisioningRequest)(
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
        Request(method = Method.POST, uri = uri"/v1/provision")
          .withEntity(ProvisioningRequest(DescriptorKind.ComponentDescriptor, "a-yaml-descriptor"))
      )
    val expected                   = SystemError(error)

    check[SystemError](response, Status.InternalServerError, Some(expected)) shouldBe true
  }

  "The server" should "return a 200 with COMPLETED on a successful unprovision" in {
    val provisioner                = mock[ProvisionerController[DpCdp, S3Cdp]]
    (provisioner
      .unprovision(_: ApiRequest.ProvisioningRequest)(
        _: Decoder[ProvisioningDescriptor[DpCdp]],
        _: Decoder[Component[S3Cdp]]
      ))
      .expects(*, *, *)
      .returns(Right(ApiResponse.completed("fakeid", None)))
    val handler                    = new SpecificProvisionerHandler(provisioner)
    val response: IO[Response[IO]] = new Resource[IO]()
      .routes(handler)
      .orNotFound
      .run(
        Request(method = Method.POST, uri = uri"/v1/unprovision")
          .withEntity(ProvisioningRequest(DescriptorKind.ComponentDescriptor, "a-yaml-descriptor"))
      )
    val expected                   = ProvisioningStatus(ProvisioningStatus.Status.Completed, "")

    check[ProvisioningStatus](response, Status.Ok, Some(expected)) shouldBe true
  }

  it should "return a 400 with a list of errors if an error happens on unprovision" in {
    val errors                     = Vector("first error", "second error")
    val provisioner                = mock[ProvisionerController[DpCdp, S3Cdp]]
    (provisioner
      .unprovision(_: ApiRequest.ProvisioningRequest)(
        _: Decoder[ProvisioningDescriptor[DpCdp]],
        _: Decoder[Component[S3Cdp]]
      ))
      .expects(*, *, *)
      .returns(Left(ApiError.validErr(errors: _*)))
    val handler                    = new SpecificProvisionerHandler(provisioner)
    val response: IO[Response[IO]] = new Resource[IO]()
      .routes(handler)
      .orNotFound
      .run(
        Request(method = Method.POST, uri = uri"/v1/unprovision")
          .withEntity(ProvisioningRequest(DescriptorKind.ComponentDescriptor, "a-yaml-descriptor"))
      )
    val expected                   = ValidationError(errors)

    check[ValidationError](response, Status.BadRequest, Some(expected)) shouldBe true
  }

  it should "return a 500 with meaningful error on unprovision exception" in {
    val error                      = "first error"
    val provisioner                = mock[ProvisionerController[DpCdp, S3Cdp]]
    (provisioner
      .unprovision(_: ApiRequest.ProvisioningRequest)(
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
        Request(method = Method.POST, uri = uri"/v1/unprovision")
          .withEntity(ProvisioningRequest(DescriptorKind.ComponentDescriptor, "a-yaml-descriptor"))
      )
    val expected                   = SystemError(error)

    check[SystemError](response, Status.InternalServerError, Some(expected)) shouldBe true
  }

}
