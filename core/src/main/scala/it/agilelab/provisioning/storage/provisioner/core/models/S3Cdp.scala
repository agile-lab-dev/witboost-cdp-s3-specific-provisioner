package it.agilelab.provisioning.storage.provisioner.core.models

// TODO: check what fields are needed here, probably acl is useless
case class S3Cdp(
  cdpEnvironment: String,
  bucket: String,
  folder: String,
  acl: Acl
)
