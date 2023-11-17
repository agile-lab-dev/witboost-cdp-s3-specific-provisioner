package it.agilelab.provisioning.storage.provisioner.service.gateway.storage

import it.agilelab.provisioning.storage.provisioner.core.models.StorageSpace

trait StorageSpaceGateway {
  def create(storageSpace: StorageSpace): Either[StorageSpaceGatewayError, Unit]
}
