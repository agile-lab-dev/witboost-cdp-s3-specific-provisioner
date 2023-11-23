package it.agilelab.provisioning.storage.provisioner.core.gateway.cdp

import cats.implicits._
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClient
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClient
import CdpGatewayError.{ DatalakeNotFound, DescribeCdpDlErr, DescribeCdpEnvErr }

class DefaultCdpGateway(envClient: CdpEnvClient, dlClient: CdpDlClient) extends CdpGateway {

  override def getStorageLocationBase(environmentName: String): Either[CdpGatewayError, String] =
    for {
      env       <- envClient
                     .describeEnvironment(environmentName)
                     .leftMap(e => DescribeCdpEnvErr(e))
      datalakes <- dlClient
                     .findAllDl()
                     .leftMap(e => DescribeCdpDlErr(e))
      dl        <- datalakes
                     .find(_.getEnvironmentCrn == env.getCrn)
                     .toRight(DatalakeNotFound(env.getEnvironmentName))
      dlDesc    <- dlClient
                     .describeDl(dl.getDatalakeName)
                     .leftMap(e => DescribeCdpDlErr(e))
    } yield dlDesc.getCloudStorageBaseLocation

  override def cdpEnvironmentExists(cdpEnvironment: String): Boolean =
    envClient.describeEnvironment(cdpEnvironment).isRight

  override def cdpDatalakeExists(cdpEnvironment: String): Boolean = {
    val res = for {
      env       <- envClient
                     .describeEnvironment(cdpEnvironment)
                     .leftMap(e => DescribeCdpEnvErr(e))
      datalakes <- dlClient
                     .findAllDl()
                     .leftMap(e => DescribeCdpDlErr(e))
      dl        <- datalakes
                     .find(_.getEnvironmentCrn == env.getCrn)
                     .toRight(DatalakeNotFound(env.getEnvironmentName))
      dlDesc    <- dlClient
                     .describeDl(dl.getDatalakeName)
                     .leftMap(e => DescribeCdpDlErr(e))
    } yield dlDesc
    res.fold(_ => false, _ => true)
  }

}
