package it.agilelab.provisioning.storage.provisioner.service.gateway.mapper

import cats.Show
import cats.implicits.showInterpolator
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGatewayError

sealed trait StorageSpaceMapperError
object StorageSpaceMapperError {
  final case class StorageLocationNotFound(cdpGateway: CdpGatewayError) extends StorageSpaceMapperError
  final case class StorageLocationNotParsable(storageLocation: String)  extends StorageSpaceMapperError

  implicit def showStorageSpaceMapperError: Show[StorageSpaceMapperError] =
    Show.show {
      case StorageLocationNotFound(error)              => show"StorageLocationNotFound($error)"
      case StorageLocationNotParsable(storageLocation) => show"StorageLocationNotParsable($storageLocation)"
    }
}
