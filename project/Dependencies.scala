import sbt._

trait Dependencies {

  lazy val scalaMeshVrs = "0.0.0-SNAPSHOT-94c691a.wit-365-cdp-common-l"
  lazy val scalaTestVrs = "3.1.0"
  lazy val scalaMockVrs = "4.4.0"

  lazy val scalaMeshS3Gat             = "it.agilelab.provisioning" %% "scala-mesh-aws-s3"              % scalaMeshVrs
  lazy val scalaMeshIamGat            = "it.agilelab.provisioning" %% "scala-mesh-aws-iam"             % scalaMeshVrs
  lazy val scalaMeshRepository        = "it.agilelab.provisioning" %% "scala-mesh-repository"          % scalaMeshVrs
  lazy val scalaMeshCdpDl             = "it.agilelab.provisioning" %% "scala-mesh-cdp-dl"              % scalaMeshVrs
  lazy val scalaMeshCdpEnv            = "it.agilelab.provisioning" %% "scala-mesh-cdp-env"             % scalaMeshVrs
  lazy val scalaMeshSelfService       = "it.agilelab.provisioning" %% "scala-mesh-self-service"        % scalaMeshVrs
  lazy val scalaMeshSelfServiceLambda = "it.agilelab.provisioning" %% "scala-mesh-self-service-lambda" % scalaMeshVrs

  lazy val commonDependencies: Seq[ModuleID] = Seq(
    scalaMeshS3Gat,
    scalaMeshIamGat,
    scalaMeshRepository,
    scalaMeshCdpDl,
    scalaMeshCdpEnv,
    scalaMeshSelfServiceLambda
  )

  private val http4sVersion                  = "0.23.18"
  lazy val http4sDependencies: Seq[ModuleID] = Seq(
    "org.http4s" %% "http4s-ember-client",
    "org.http4s" %% "http4s-ember-server",
    "org.http4s" %% "http4s-dsl",
    "org.http4s" %% "http4s-circe"
  ).map(_ % http4sVersion)

  lazy val catsDependencies: Seq[ModuleID]   = Seq(
    "org.typelevel" %% "cats-effect" % "3.4.8"
  )

  private val circeVersion                  = "0.14.5"
  lazy val circeDependencies: Seq[ModuleID] = Seq(
    "io.circe" %% "circe-core",
    "io.circe" %% "circe-generic",
    "io.circe" %% "circe-parser"
  ).map(_ % circeVersion) ++ Seq("io.circe" %% "circe-generic-extras" % "0.14.3")

  lazy val scalaTest                        = "org.scalatest" %% "scalatest" % scalaTestVrs % "test"
  lazy val scalaMock                        = "org.scalamock" %% "scalamock" % scalaMockVrs % "test"

  lazy val testDependencies: Seq[ModuleID] = Seq(
    scalaTest,
    scalaMock
  )

}

object Dependencies extends Dependencies
