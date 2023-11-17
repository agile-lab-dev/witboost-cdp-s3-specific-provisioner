package it.agilelab.provisioning.storage.provisioner.core.models

/** Define a storage space within the cdp context
  * @param id: Unique identifiable string for the storage space
  * @param bucket: bucket
  * @param path: path
  * @param owners: List of owners
  * @param users: List of users
  */
// TODO here probably we need to remove owners and users as they are dynamic and comes from updateAcl request
case class StorageSpace(id: String, bucket: String, path: String, owners: Seq[String], users: Seq[String])
