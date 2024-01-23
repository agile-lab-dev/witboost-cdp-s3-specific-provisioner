package it.agilelab.provisioning.storage.provisioner.core.models

/** Define a storage space within the cdp context
  * @param id: Unique identifiable string for the storage space
  * @param bucket: bucket
  * @param path: path
  */
case class StorageSpace(id: String, bucket: String, path: String)
