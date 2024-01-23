package it.agilelab.provisioning.storage.provisioner.service.context

import cats.implicits.{ toBifunctorOps, toShow }
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.Logger
import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.commons.principalsmapping.{ CdpIamPrincipals, PrincipalsMapper }
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGateway
import it.agilelab.provisioning.storage.provisioner.service.context.ContextError.{ ClientError, ConfigurationError }

import scala.util.Try

final case class ProvisionerContext(
  deployRoleUser: String,
  deployRolePwd: String,
  s3Gateway: S3Gateway,
  cdpGateway: CdpGateway,
  principalsMapper: PrincipalsMapper[CdpIamPrincipals]
)

object ProvisionerContext {
  private val logger = Logger(getClass.getName)

  private val CdpDeployRoleUser     = "CDP_DEPLOY_ROLE_USER"
  private val CdpDeployRolePassword = "CDP_DEPLOY_ROLE_PASSWORD"

  def init(conf: Conf): Either[ContextError, ProvisionerContext] =
    for {
      deployRoleUser   <- conf
                            .get(CdpDeployRoleUser)
                            .leftMap { e =>
                              logger.error(e.show)
                              ConfigurationError(e)
                            }
      deployRolePwd    <- conf
                            .get(CdpDeployRolePassword)
                            .leftMap { e =>
                              logger.error(e.show)
                              ConfigurationError(e)
                            }
      s3Gateway        <- S3Gateway.defaultWithAudit().leftMap { e =>
                            logger.error(e.show)
                            ClientError("S3Gateway", e)
                          }
      cdpGateway       <- CdpGateway.default().leftMap { e =>
                            logger.error("", e)
                            ClientError("CdpGateway", e)
                          }
      principalsMapper <- new PrincipalsMapperPluginLoader().load(ApplicationConfiguration.provisionerConfig)
    } yield ProvisionerContext(
      deployRoleUser,
      deployRolePwd,
      s3Gateway,
      cdpGateway,
      principalsMapper
    )
}
