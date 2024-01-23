package it.agilelab.provisioning.storage.provisioner.service.helpers

import io.circe.Json
import it.agilelab.provisioning.mesh.self.service.api.model.{ Component, DataProduct, ProvisionRequest }

object ProvisionRequestFaker {

  def apply[SPECIFIC, COMP_SPECIFIC](
    specific: SPECIFIC
  ): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC] =
    ProvisionRequestFakerBuilder(
      id = "urn:dmb:dp:domain:dp-name:0",
      name = "dp-name",
      domain = "domain:domain",
      environment = "poc",
      version = "0.0.1",
      dataProductOwner = "dataProductOwner",
      devGroup = "devGroup",
      ownerGroup = "ownerGroup",
      specific = specific,
      components = Seq.empty,
      component = None
    )
}

case class ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC](
  id: String,
  name: String,
  domain: String,
  environment: String,
  version: String,
  dataProductOwner: String,
  devGroup: String,
  ownerGroup: String,
  specific: SPECIFIC,
  components: Seq[Json],
  component: Option[Component[COMP_SPECIFIC]]
) {
  def withId(id: String): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC]                    =
    this.copy(id = id)
  def withName(name: String): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC]                =
    this.copy(name = name)
  def withDomain(domain: String): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC]            =
    this.copy(domain = domain)
  def withEnvironment(environment: String): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC]  =
    this.copy(environment = environment)
  def withVersion(version: String): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC]          =
    this.copy(version = version)
  def withDataProductOwner(
    dataProductOwner: String
  ): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC]                                         =
    this.copy(dataProductOwner = dataProductOwner)
  def withDevGroup(devGroup: String): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC]        =
    this.copy(devGroup = devGroup)
  def withOwnerGroup(ownerGroup: String): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC]    =
    this.copy(ownerGroup = ownerGroup)
  def withSpecific(specific: SPECIFIC): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC]      =
    this.copy(specific = specific)
  def withComponents(components: Seq[Json]): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC] =
    this.copy(components = components)
  def withComponent(
    component: Component[COMP_SPECIFIC]
  ): ProvisionRequestFakerBuilder[SPECIFIC, COMP_SPECIFIC]                                         =
    this.copy(component = Some(component))

  def build(): ProvisionRequest[SPECIFIC, COMP_SPECIFIC] =
    ProvisionRequest(
      dataProduct = DataProduct[SPECIFIC](
        id = id,
        name = name,
        domain = domain,
        environment = environment,
        version = version,
        dataProductOwner = dataProductOwner,
        devGroup = devGroup,
        ownerGroup = ownerGroup,
        specific = specific,
        components = components
      ),
      component = component
    )

}
