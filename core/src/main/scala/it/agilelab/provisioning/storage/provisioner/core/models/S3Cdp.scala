package it.agilelab.provisioning.storage.provisioner.core.models

case class S3Cdp(
  cdpEnvironment: String,
  bucket: String,
  folder: String
)
