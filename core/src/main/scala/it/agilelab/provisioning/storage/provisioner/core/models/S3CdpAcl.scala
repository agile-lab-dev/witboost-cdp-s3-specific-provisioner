package it.agilelab.provisioning.storage.provisioner.core.models

final case class S3CdpAcl(
  attachedPolicies: Seq[PolicyAttachment],
  detachedPolicies: Seq[PolicyAttachment]
)
