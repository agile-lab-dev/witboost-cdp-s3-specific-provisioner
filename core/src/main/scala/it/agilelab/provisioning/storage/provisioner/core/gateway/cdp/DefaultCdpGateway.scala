package it.agilelab.provisioning.storage.provisioner.core.gateway.cdp

import cats.implicits._
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClient
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClient
import CdpGatewayError.{
  CdpDlWrongStatusErr,
  CdpEnvWrongStatusErr,
  DatalakeNotFound,
  DescribeCdpDlErr,
  DescribeCdpEnvErr
}

class DefaultCdpGateway(envClient: CdpEnvClient, dlClient: CdpDlClient) extends CdpGateway {

  final private val EnvAvailable: String = "AVAILABLE"
  final private val DlRunning: String    = "RUNNING"

  override def getStorageLocationBase(environmentName: String): Either[CdpGatewayError, String] =
    for {
      env       <- envClient
                     .describeEnvironment(environmentName)
                     .leftMap(e => DescribeCdpEnvErr(e))
      _         <-
        if (EnvAvailable.equals(env.getStatus)) Right() else Left(CdpEnvWrongStatusErr(environmentName, env.getStatus))
      datalakes <- dlClient
                     .findAllDl()
                     .leftMap(e => DescribeCdpDlErr(e))
      dl        <- datalakes
                     .find(_.getEnvironmentCrn == env.getCrn)
                     .toRight(DatalakeNotFound(env.getEnvironmentName))
      _         <-
        if (DlRunning.equals(dl.getStatus)) Right() else Left(CdpDlWrongStatusErr(env.getEnvironmentName, dl.getStatus))
      dlDesc    <- dlClient
                     .describeDl(dl.getDatalakeName)
                     .leftMap(e => DescribeCdpDlErr(e))
    } yield dlDesc.getCloudStorageBaseLocation

  override def cdpEnvironmentExists(cdpEnvironment: String): Boolean = {
    val eitherEnv = envClient.describeEnvironment(cdpEnvironment)
    eitherEnv.fold(_ => false, e => EnvAvailable.equals(e.getStatus))
  }

  override def cdpDatalakeExists(cdpEnvironment: String): Boolean = {
    val res = for {
      env       <- envClient
                     .describeEnvironment(cdpEnvironment)
                     .leftMap(e => DescribeCdpEnvErr(e))
      _         <-
        if (EnvAvailable.equals(env.getStatus)) Right() else Left(CdpEnvWrongStatusErr(cdpEnvironment, env.getStatus))
      datalakes <- dlClient
                     .findAllDl()
                     .leftMap(e => DescribeCdpDlErr(e))
      dl        <- datalakes
                     .find(_.getEnvironmentCrn == env.getCrn)
                     .toRight(DatalakeNotFound(env.getEnvironmentName))
      _         <-
        if (DlRunning.equals(dl.getStatus)) Right() else Left(CdpDlWrongStatusErr(env.getEnvironmentName, dl.getStatus))
      dlDesc    <- dlClient
                     .describeDl(dl.getDatalakeName)
                     .leftMap(e => DescribeCdpDlErr(e))
    } yield dlDesc
    res.fold(_ => false, _ => true)
  }

}
