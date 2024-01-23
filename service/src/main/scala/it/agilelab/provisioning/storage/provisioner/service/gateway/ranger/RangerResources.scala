package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger

import it.agilelab.provisioning.commons.client.ranger.model.{ RangerResource, RangerSecurityZoneResources }

object RangerResources {
  private val BUCKET_KEY = "bucket"
  private val PATH_KEY   = "path"

  def bucketAndPath(bucketName: String, path: String): Map[String, RangerResource] = Map(
    BUCKET_KEY -> RangerResource(Seq(bucketName), isExcludes = false, isRecursive = false),
    PATH_KEY   -> RangerResource(Seq(path), isExcludes = false, isRecursive = true)
  )

  def s3SecurityZoneResources(
    bucketNames: Seq[String],
    paths: Seq[String]
  ): RangerSecurityZoneResources =
    RangerSecurityZoneResources(
      Seq(
        Map(
          BUCKET_KEY -> bucketNames,
          PATH_KEY   -> paths
        ).filter { case (_, seq) => seq.nonEmpty }
      ).filter(_.nonEmpty)
    )

}
