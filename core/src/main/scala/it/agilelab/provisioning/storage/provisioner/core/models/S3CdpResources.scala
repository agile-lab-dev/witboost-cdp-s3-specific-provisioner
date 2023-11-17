package it.agilelab.provisioning.storage.provisioner.core.models

import cats.Show

final case class S3CdpResources(
  id: String,
  bucket: String,
  path: String,
  policies: S3CdpAcl
)

object S3CdpResources {
  implicit def s3CdpResourcesShow: Show[S3CdpResources] = Show.fromToString[S3CdpResources]
}
