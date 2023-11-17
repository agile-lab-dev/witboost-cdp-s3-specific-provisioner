package it.agilelab.provisioning.storage.provisioner.core

import com.cloudera.cdp.datalake.model.{ Datalake, DatalakeDetails }
import com.cloudera.cdp.environments.model.Environment
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClient
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClientError.FindAllDlErr
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClient
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClientError.DescribeEnvironmentErr
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGatewayError.{
  DatalakeNotFound,
  DescribeCdpDlErr,
  DescribeCdpEnvErr
}
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.DefaultCdpGateway
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

class DefaultCdpGatewayTest extends AnyFunSuite with MockFactory {

  val cdpEnvClient: CdpEnvClient = stub[CdpEnvClient]
  val cdpDlClient: CdpDlClient   = stub[CdpDlClient]
  val cdpGateway                 = new DefaultCdpGateway(cdpEnvClient, cdpDlClient)

  val env1      = new Environment()
  val env2      = new Environment()
  val dl1       = new Datalake()
  val dl2       = new Datalake()
  val dlDetails = new DatalakeDetails()

  env1.setCrn("env-1-crn")
  env1.setEnvironmentName("env-1")
  env2.setCrn("env-2-crn")
  env2.setEnvironmentName("env-2")
  dl1.setDatalakeName("dl-1")
  dl1.setEnvironmentCrn("env-1-crn")
  dl2.setDatalakeName("dl-2")
  dl2.setEnvironmentCrn("env-1-crn")
  dlDetails.setCloudStorageBaseLocation("s3a://abucket/apath")

  test("getStorageLocationBase return Right(StorageSpace)") {
    (cdpEnvClient.describeEnvironment _).when("env-1").returns(Right(env1))
    (cdpDlClient.findAllDl _).when().returns(Right(Seq(dl1, dl2)))
    (cdpDlClient.describeDl _).when("dl-1").returns(Right(dlDetails))

    val actual   = cdpGateway.getStorageLocationBase("env-1")
    val expected = Right("s3a://abucket/apath")
    assert(actual == expected)
  }

  test("getStorageLocationBase return Left(DescribeCdpEnvErr)") {
    (cdpEnvClient.describeEnvironment _).when(*).returns(Left(DescribeEnvironmentErr("env-1", null)))

    val actual   = cdpGateway.getStorageLocationBase("env-1")
    val expected = Left(DescribeCdpEnvErr(DescribeEnvironmentErr("env-1", null)))
    assert(actual == expected)
  }

  test("getStorageLocationBase return Left(DescribeDlError)") {
    (cdpEnvClient.describeEnvironment _).when("env-1").returns(Right(env1))
    (cdpDlClient.findAllDl _).when().returns(Left(FindAllDlErr(null)))

    val actual   = cdpGateway.getStorageLocationBase("env-1")
    val expected = Left(DescribeCdpDlErr(FindAllDlErr(null)))

    assert(actual == expected)
  }

  test("getStorageLocationBase return Left(DatalakeNotFound)") {
    (cdpEnvClient.describeEnvironment _).when("env-2").returns(Right(env2))
    (cdpDlClient.findAllDl _).when().returns(Right(Seq(dl1, dl2)))

    val actual   = cdpGateway.getStorageLocationBase("env-2")
    val expected = Left(DatalakeNotFound("env-2"))

    assert(actual == expected)
  }

}
