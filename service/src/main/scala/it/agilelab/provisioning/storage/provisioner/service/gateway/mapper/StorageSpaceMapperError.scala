package it.agilelab.provisioning.storage.provisioner.service.gateway.mapper

import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGatewayError

sealed trait StorageSpaceMapperError
object StorageSpaceMapperError {
  final case class StorageLocationNotFound(cdpGateway: CdpGatewayError) extends StorageSpaceMapperError
  final case class StorageLocationNotParsable(storageLocation: String)  extends StorageSpaceMapperError
}
