package it.agilelab.provisioning.storage.provisioner.service.gateway.ranger.role

sealed trait RoleType

object OwnerRoleType extends RoleType
object UserRoleType  extends RoleType
