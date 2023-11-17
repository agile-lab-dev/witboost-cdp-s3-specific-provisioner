package it.agilelab.provisioning.storage.provisioner.app.config

import cats.data.Kleisli
import cats.effect.IO
import cats.implicits.toSemigroupKOps
import it.agilelab.provisioning.api.generated.{ Handler, Resource }
import it.agilelab.provisioning.mesh.self.service.api.controller.ProvisionerController
import it.agilelab.provisioning.storage.provisioner.app.api.SpecificProvisionerHandler
import it.agilelab.provisioning.storage.provisioner.app.routes.HealthCheck
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, S3Cdp }
import org.http4s.server.middleware.Logger
import org.http4s.{ Request, Response }

final class FrameworkDependencies(provisionerController: ProvisionerController[DpCdp, S3Cdp]) {

  private val provisionerHandler: Handler[IO] = new SpecificProvisionerHandler(provisionerController)
  private val provisionerService              = new Resource[IO]().routes(provisionerHandler)
  private val combinedServices                = HealthCheck.routes[IO]() <+> provisionerService

  private val withloggerService = Logger.httpRoutes[IO](
    logHeaders = false,
    logBody = true,
    redactHeadersWhen = _ => false,
    logAction = None
  )(combinedServices)

  val httpApp: Kleisli[IO, Request[IO], Response[IO]] = withloggerService.orNotFound

}
