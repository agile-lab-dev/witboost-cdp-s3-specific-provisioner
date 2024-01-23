package it.agilelab.provisioning.storage.provisioner.service.gateway.mapper

import it.agilelab.provisioning.mesh.self.service.api.model.Component.OutputPort
import it.agilelab.provisioning.mesh.self.service.api.model.DataProduct
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, S3Cdp, StorageSpace }

class S3CdpStorageSpaceMapper extends StorageSpaceMapper {

  override def map(
    dataProduct: DataProduct[DpCdp],
    outputPort: OutputPort[S3Cdp]
  ): Either[StorageSpaceMapperError, StorageSpace] =
    Right(
      StorageSpace(
        "%s:%s".format(outputPort.id, dataProduct.environment),
        outputPort.specific.bucket,
        outputPort.specific.folder
      )
    )

}
