package it.agilelab.provisioning.storage.provisioner.core

import com.cloudera.cdp.CdpServiceException
import com.cloudera.cdp.datalake.model.{ Datalake, DatalakeDetails }
import com.cloudera.cdp.environments.model.Environment
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClient
import it.agilelab.provisioning.commons.client.cdp.dl.CdpDlClientError.FindAllDlErr
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClient
import it.agilelab.provisioning.commons.client.cdp.env.CdpEnvClientError.DescribeEnvironmentErr
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGatewayError.{
  CdpDlWrongStatusErr,
  CdpEnvWrongStatusErr,
  DatalakeNotFound,
  DescribeCdpDlErr,
  DescribeCdpEnvErr
}
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.DefaultCdpGateway
import org.scalamock.scalatest.MockFactory
import org.scalatest.funsuite.AnyFunSuite

import java.util

class DefaultCdpGatewayTest extends AnyFunSuite with MockFactory {

  val cdpEnvClient: CdpEnvClient = stub[CdpEnvClient]
  val cdpDlClient: CdpDlClient   = stub[CdpDlClient]
  val cdpGateway                 = new DefaultCdpGateway(cdpEnvClient, cdpDlClient)

  val env1       = new Environment()
  val env2       = new Environment()
  val envStopped = new Environment()
  val dl1        = new Datalake()
  val dl2        = new Datalake()
  val dlStopped  = new Datalake()
  val dlDetails  = new DatalakeDetails()

  env1.setCrn("env-1-crn")
  env1.setEnvironmentName("env-1")
  env1.setStatus("AVAILABLE")
  env2.setCrn("env-2-crn")
  env2.setEnvironmentName("env-2")
  env2.setStatus("AVAILABLE")
  envStopped.setCrn("env-3-crn")
  envStopped.setEnvironmentName("env-3")
  envStopped.setStatus("ENV_STOPPED")
  dl1.setDatalakeName("dl-1")
  dl1.setEnvironmentCrn("env-1-crn")
  dl1.setStatus("RUNNING")
  dl2.setDatalakeName("dl-2")
  dl2.setEnvironmentCrn("env-1-crn")
  dl2.setStatus("RUNNING")
  dlStopped.setDatalakeName("dl-3")
  dlStopped.setEnvironmentCrn("env-2-crn")
  dlStopped.setStatus("STOPPED")
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

  test("getStorageLocationBase return Left(CdpEnvWrongStatusErr)") {
    (cdpEnvClient.describeEnvironment _).when(*).returns(Right(envStopped))
    (cdpDlClient.findAllDl _).when().returns(Right(Seq(dl1, dl2)))

    val actual   = cdpGateway.getStorageLocationBase("env-3")
    val expected = Left(CdpEnvWrongStatusErr("env-3", "ENV_STOPPED"))
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

  test("getStorageLocationBase return Left(CdpDlWrongStatusErr)") {
    (cdpEnvClient.describeEnvironment _).when("env-2").returns(Right(env2))
    (cdpDlClient.findAllDl _).when().returns(Right(Seq(dlStopped)))

    val actual   = cdpGateway.getStorageLocationBase("env-2")
    val expected = Left(CdpDlWrongStatusErr("env-2", "STOPPED"))

    assert(actual == expected)
  }

  test("cdpEnvironmentExists return false if env is not existing") {
    val cdpException = new CdpServiceException(
      "request_id",
      404,
      new util.HashMap[String, util.List[String]](),
      "NOT_FOUND",
      "Environment with name 'env-1' was not found"
    )
    (cdpEnvClient.describeEnvironment _).when(*).returns(Left(DescribeEnvironmentErr("env-1", cdpException)))

    val actual   = cdpGateway.cdpEnvironmentExists("env-1")
    val expected = false
    assert(actual == expected)
  }

  test("cdpEnvironmentExists return false if env is not available") {
    (cdpEnvClient.describeEnvironment _).when(*).returns(Right(envStopped))

    val actual   = cdpGateway.cdpEnvironmentExists("env-3")
    val expected = false
    assert(actual == expected)
  }

  test("cdpEnvironmentExists return true if env exists and is available") {
    (cdpEnvClient.describeEnvironment _).when(*).returns(Right(env1))

    val actual   = cdpGateway.cdpEnvironmentExists("env-1")
    val expected = true
    assert(actual == expected)
  }

  test("cdpDatalakeExists return false if env is not existing") {
    val cdpException = new CdpServiceException(
      "request_id",
      404,
      new util.HashMap[String, util.List[String]](),
      "NOT_FOUND",
      "Environment with name 'env-1' was not found"
    )
    (cdpEnvClient.describeEnvironment _).when("env-1").returns(Left(DescribeEnvironmentErr("env-1", cdpException)))

    val actual   = cdpGateway.cdpDatalakeExists("env-1")
    val expected = false

    assert(actual == expected)
  }

  test("cdpDatalakeExists return false if env is not available") {
    (cdpEnvClient.describeEnvironment _).when(*).returns(Right(envStopped))

    val actual   = cdpGateway.cdpDatalakeExists("env-3")
    val expected = false

    assert(actual == expected)
  }

  test("cdpDatalakeExists return false if dl is not existing") {
    (cdpEnvClient.describeEnvironment _).when(*).returns(Right(env2))
    (cdpDlClient.findAllDl _).when().returns(Right(Seq(dl1, dl2)))

    val actual   = cdpGateway.cdpDatalakeExists("env-2")
    val expected = false

    assert(actual == expected)
  }

  test("cdpDatalakeExists return false if dl is not running") {
    (cdpEnvClient.describeEnvironment _).when(*).returns(Right(env2))
    (cdpDlClient.findAllDl _).when().returns(Right(Seq(dlStopped)))

    val actual   = cdpGateway.cdpDatalakeExists("env-2")
    val expected = false

    assert(actual == expected)
  }

  test("cdpDatalakeExists return true if env and dl are existing and running") {
    (cdpEnvClient.describeEnvironment _).when(*).returns(Right(env1))
    (cdpDlClient.findAllDl _).when().returns(Right(Seq(dl1)))
    (cdpDlClient.describeDl _).when("dl-1").returns(Right(dlDetails))

    val actual   = cdpGateway.cdpDatalakeExists("env-1")
    val expected = true

    assert(actual == expected)
  }

}
