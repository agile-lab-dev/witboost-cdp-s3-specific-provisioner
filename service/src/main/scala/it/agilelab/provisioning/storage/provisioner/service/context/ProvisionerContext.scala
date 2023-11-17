package it.agilelab.provisioning.storage.provisioner.service.context

import cats.implicits.{ toBifunctorOps, toShow }
import com.typesafe.scalalogging.Logger
import it.agilelab.provisioning.aws.iam.gateway.IamGateway
import it.agilelab.provisioning.aws.s3.gateway.S3Gateway
import it.agilelab.provisioning.commons.config.Conf
import it.agilelab.provisioning.storage.provisioner.service.context.ContextError.ClientError

final case class ProvisionerContext(
  s3Gateway: S3Gateway,
  iamGateway: IamGateway
)

object ProvisionerContext {
  private val logger                                             = Logger(getClass.getName)
  def init(conf: Conf): Either[ContextError, ProvisionerContext] =
    for {
      s3Gateway  <- S3Gateway.defaultWithAudit().leftMap { e =>
                      logger.error(e.show)
                      ClientError("S3Gateway", e)
                    }
      iamGateway <- IamGateway.defaultWithAudit().leftMap { e =>
                      logger.error(e.show)
                      ClientError("IamGateway", e)
                    }
    } yield ProvisionerContext(
      s3Gateway,
      iamGateway
    )
}
