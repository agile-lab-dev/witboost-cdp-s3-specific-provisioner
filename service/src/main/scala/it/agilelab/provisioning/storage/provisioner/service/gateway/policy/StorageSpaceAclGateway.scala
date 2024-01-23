package it.agilelab.provisioning.storage.provisioner.service.gateway.policy

import it.agilelab.provisioning.commons.client.ranger.model.RangerRole
import it.agilelab.provisioning.commons.principalsmapping.CdpIamPrincipals
import it.agilelab.provisioning.mesh.self.service.api.model.Component.OutputPort
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import it.agilelab.provisioning.mesh.self.service.core.gateway.ComponentGatewayError
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, PolicyAttachment, S3Cdp }

/** An AclGateway trait
  * Which is able to generate and attach policies for S3 bucket/folder
  */
trait StorageSpaceAclGateway {

  def provisionAcl(
    dp: DataProduct[DpCdp],
    op: OutputPort[S3Cdp],
    ownerUser: String,
    ownerGroup: String
  ): Either[ComponentGatewayError, Seq[PolicyAttachment]]

  def unprovisionAcl(
    op: OutputPort[S3Cdp]
  ): Either[ComponentGatewayError, Seq[PolicyAttachment]]

  def updateAcl(
    op: OutputPort[S3Cdp],
    refs: Set[CdpIamPrincipals]
  ): Either[ComponentGatewayError, RangerRole]

}
