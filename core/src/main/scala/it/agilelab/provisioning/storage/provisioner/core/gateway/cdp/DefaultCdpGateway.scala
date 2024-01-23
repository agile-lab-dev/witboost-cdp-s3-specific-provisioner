package it.agilelab.provisioning.storage.provisioner.core.gateway.cdp

import cats.implicits._
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClient
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClient
import CdpGatewayError.{
  CdpDlWrongStatusErr,
  CdpEnvWrongStatusErr,
  DatalakeNotFound,
  DescribeCdpDlErr,
  DescribeCdpEnvErr,
  GetRangerHostErr
}
import com.cloudera.cdp.datalake.model.{ Datalake, DatalakeDetails }
import com.cloudera.cdp.environments.model.Environment

import scala.jdk.CollectionConverters.CollectionHasAsScala

class DefaultCdpGateway(envClient: CdpEnvClient, dlClient: CdpDlClient) extends CdpGateway {

  final private val EnvAvailable: String = "AVAILABLE"
  final private val DlRunning: String    = "RUNNING"

  override def getStorageLocationBase(environmentName: String): Either[CdpGatewayError, String] =
    for {
      env    <- getEnvironment(environmentName)
      dl     <- getDataLake(env)
      dlDesc <- dlClient
                  .describeDl(dl.getDatalakeName)
                  .leftMap(e => DescribeCdpDlErr(e))
    } yield dlDesc.getCloudStorageBaseLocation

  override def cdpEnvironmentExists(cdpEnvironment: String): Boolean =
    getEnvironment(cdpEnvironment).fold(_ => false, _ => true)

  override def getEnvironment(environment: String): Either[CdpGatewayError, Environment] = for {
    env <- envClient
             .describeEnvironment(environment)
             .leftMap(e => DescribeCdpEnvErr(e))
    _   <-
      if (EnvAvailable.equals(env.getStatus)) Right() else Left(CdpEnvWrongStatusErr(environment, env.getStatus))
  } yield env

  override def cdpDatalakeExists(cdpEnvironment: String): Boolean = {
    val res = for {
      env    <- getEnvironment(cdpEnvironment)
      dl     <- getDataLake(env)
      dlDesc <- dlClient
                  .describeDl(dl.getDatalakeName)
                  .leftMap(e => DescribeCdpDlErr(e))
    } yield dlDesc
    res.fold(_ => false, _ => true)
  }

  override def getDataLake(env: Environment): Either[CdpGatewayError, Datalake] = for {
    datalakes <- dlClient
                   .findAllDl()
                   .leftMap(e => DescribeCdpDlErr(e))
    dl        <- datalakes
                   .find(_.getEnvironmentCrn == env.getCrn)
                   .toRight(DatalakeNotFound(env.getEnvironmentName))
    _         <-
      if (DlRunning.equals(dl.getStatus)) Right() else Left(CdpDlWrongStatusErr(env.getEnvironmentName, dl.getStatus))
  } yield dl

  override def getRangerHost(dl: Datalake): Either[CdpGatewayError, String] = for {
    dlDesc         <- dlClient
                        .describeDl(dl.getDatalakeName)
                        .leftMap(e => DescribeCdpDlErr(e))
    rangerEndpoint <- retrieveRangerEndpoint(dlDesc)
    rangerHost     <- extractRangerHost(rangerEndpoint)
  } yield rangerHost

  private def retrieveRangerEndpoint(dl: DatalakeDetails): Either[CdpGatewayError, String] =
    dl.getEndpoints.getEndpoints.asScala
      .find(e => e.getServiceName === "RANGER_ADMIN" && e.getMode === "PAM")
      .map(_.getServiceUrl)
      .toRight(GetRangerHostErr("Unable to find ranger admin endpoint"))

  private def extractRangerHost(rangerHost: String): Either[CdpGatewayError, String] =
    rangerHost match {
      case s"http://$path"  => Right(path)
      case s"https://$path" => Right(path)
      case _                => Left(GetRangerHostErr("Unable to extract ranger endpoint"))
    }

}
