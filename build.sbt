import wartremover.WartRemover.autoImport.Wart
import sbt.Keys.{ csrConfiguration, updateClassifiers, updateSbtClassifiers }
import lmcoursier.definitions.Authentication

inThisBuild(
  Seq(
    organization := "it.agilelab.provisioning",
    scalaVersion := "2.13.2",
    version := ComputeVersion.version
  )
)

lazy val core = (project in file("core"))
  .settings(
    name := "storage-provisioner-core",
    libraryDependencies ++= Dependencies.commonDependencies ++ Dependencies.testDependencies,
    artifactorySettings,
    wartremoverSettings,
    app.k8ty.sbt.gitlab.K8tyGitlabPlugin.gitlabProjectId := "51376312"
  )
  .enablePlugins(K8tyGitlabPlugin)

lazy val service = (project in file("service"))
  .settings(
    name := "storage-provisioner-service",
    libraryDependencies ++= Dependencies.testDependencies,
    artifactorySettings,
    wartremoverSettings,
    app.k8ty.sbt.gitlab.K8tyGitlabPlugin.gitlabProjectId := "51376312"
  )
  .enablePlugins(K8tyGitlabPlugin)
  .dependsOn(
    core
  )

lazy val api = (project in file("api"))
  .settings(
    name := "storage-provisioner-api",
    libraryDependencies ++= Dependencies.testDependencies ++ Dependencies.http4sDependencies ++ Dependencies.circeDependencies
      ++ Dependencies.catsDependencies,
    artifactorySettings,
    wartremoverSettings,
    app.k8ty.sbt.gitlab.K8tyGitlabPlugin.gitlabProjectId := "51376312"
  )
  .settings(
    Compile / guardrailTasks := GuardrailHelpers.createGuardrailTasks((Compile / sourceDirectory).value / "openapi") {
      openApiFile =>
        List(
          ScalaServer(
            openApiFile.file,
            pkg = "it.agilelab.provisioning.api.generated",
            framework = "http4s",
            tracing = false
          )
        )
    },
    coverageExcludedPackages := "it.agilelab.provisioning.api.generated.*"
  )
  .enablePlugins(K8tyGitlabPlugin)
  .dependsOn(
    service
  )

lazy val root = (project in file("."))
  .settings(
    name := "storage-provisioner",
    mainClass in Compile := Some("it.agilelab.provisioning.storage.provisioner.app.Main"),
    artifactorySettings,
    dockerBuildOptions ++= Seq("--network=host"),
    dockerBaseImage := "openjdk:11-buster",
    dockerUpdateLatest := true,
    daemonUser := "daemon",
    Docker / version := (ThisBuild / version).value,
    Docker / packageName :=
      s"registry.gitlab.com/agilefactory/witboost.mesh/provisioning/cdp-refresh/witboost.mesh.provisioning.outputport.cdp.s3",
    Docker / dockerExposedPorts := Seq(8093)
  )
  .enablePlugins(JavaAppPackaging)
  .aggregate(
    core,
    service,
    api
  )
  .dependsOn(
    core,
    service,
    api
  )

lazy val wartremoverSettings = Seq(
  wartremoverErrors in (Compile, compile) ++= Warts.allBut(
    Wart.Any,
    Wart.Product,
    Wart.Nothing,
    Wart.Serializable,
    Wart.JavaSerializable,
    Wart.Equals,
    Wart.NonUnitStatements,
    Wart.DefaultArguments,
    Wart.ImplicitParameter,
    Wart.Throw,
    Wart.FinalCaseClass,
    Wart.ToString,
    Wart.IsInstanceOf,
    Wart.AsInstanceOf
  ),
  wartremoverExcluded += sourceManaged.value
)

lazy val artifactorySettings = Seq(
  resolvers ++= Seq(
    ExternalResolvers.clouderaResolver
  )
)
