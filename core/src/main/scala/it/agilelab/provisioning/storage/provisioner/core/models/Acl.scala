package it.agilelab.provisioning.storage.provisioner.core.models

final case class Acl(
  owners: Seq[String],
  users: Seq[String]
)
