package it.agilelab.provisioning.storage.provisioner.core.gateway.cdp

import cats.implicits.{ toBifunctorOps, toShow }
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClient
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClient
import CdpGatewayError.CdpGatewayInitErr
import com.cloudera.cdp.datalake.model.Datalake
import com.cloudera.cdp.environments.model.Environment
import com.typesafe.scalalogging.Logger

/** CdpGateway
  *
  * Provide a method to retrieve the storage location base from environment name
  */
trait CdpGateway {

  /** Retrieve the Storage base location configure for a specific environment datalake
    * @param environmentName: EnvironmentName
    * @return Right(StorageSpace) or Left(CdpGatewayError)
    */
  def getStorageLocationBase(environmentName: String): Either[CdpGatewayError, String]

  /** Verify the existence of a CDP Environment
    * @param cdpEnvironment CDP environment name
    * @return true if the environment exists, false otherwise
    */
  def cdpEnvironmentExists(cdpEnvironment: String): Boolean

  /** Verify the existence of a CDP Datalake
    *
    * @param cdpEnvironment CDP environment name
    * @return true if the datalake exists, false otherwise
    */
  def cdpDatalakeExists(cdpEnvironment: String): Boolean

  /** Retrieve a CDP Environment from the specified name
    * @param environment name of the CDP environment
    * @return Either a [[CdpGatewayError]] if there was an error
    *         or the retrieved [[Environment]].
    */
  def getEnvironment(environment: String): Either[CdpGatewayError, Environment]

  /** Retrieve a CDP Datalake from the specified Environment
    * @param env the CDP environment
    * @return Either a [[CdpGatewayError]] if there was an error
    *         or the retrieved [[Datalake]].
    */
  def getDataLake(env: Environment): Either[CdpGatewayError, Datalake]

  /** Retrieve the Ranger host associated to the given CDP Datalake
    * @param dl the CDP Datalake
    * @return Either a [[CdpGatewayError]] if there was an error
    *         or the retrieved Ranger host.
    */
  def getRangerHost(dl: Datalake): Either[CdpGatewayError, String]

}

object CdpGateway {
  private val logger = Logger(getClass.getName)

  /** DefaultCdpGateway
    * @return
    */
  def default(): Either[CdpGatewayError, CdpGateway] =
    for {
      cdpEnvClient <- CdpEnvClient.defaultWithAudit().leftMap { e =>
                        logger.error(e.show)
                        CdpGatewayInitErr(e)
                      }
      cdpDlClient  <- CdpDlClient.defaultWithAudit().leftMap { e =>
                        logger.error(e.show)
                        CdpGatewayInitErr(e)
                      }
    } yield new DefaultCdpGateway(cdpEnvClient, cdpDlClient)
}
