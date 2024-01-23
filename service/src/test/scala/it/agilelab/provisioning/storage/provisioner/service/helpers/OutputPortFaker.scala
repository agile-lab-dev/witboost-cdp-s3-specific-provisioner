package it.agilelab.provisioning.storage.provisioner.service.helpers

import it.agilelab.provisioning.mesh.self.service.api.model.Component.{ DataContract, OutputPort }
import it.agilelab.provisioning.mesh.self.service.api.model.openmetadata.{ Column, ColumnDataType }

object OutputPortFaker {
  def apply[SPECIFIC](specific: SPECIFIC): OutputPortFakerBuilder[SPECIFIC] =
    OutputPortFakerBuilder(
      id = "urn:dmb:cmp:domain:dp-name:0:cmp-name",
      name = "cmp-name",
      description = "description",
      version = "0.0.1",
      dataContract = DataContract(
        schema = Seq(
          Column("id", ColumnDataType.INT, None, None, None, None, None, None, None, None, None, None)
        )
      ),
      specific = specific
    )
}

final case class OutputPortFakerBuilder[SPECIFIC](
  id: String,
  name: String,
  description: String,
  version: String,
  dataContract: DataContract,
  specific: SPECIFIC
) {
  def withId(id: String): OutputPortFakerBuilder[SPECIFIC]                           =
    this.copy(id = id)
  def withName(name: String): OutputPortFakerBuilder[SPECIFIC]                       =
    this.copy(name = name)
  def withDescription(description: String): OutputPortFakerBuilder[SPECIFIC]         =
    this.copy(description = description)
  def withVersion(version: String): OutputPortFakerBuilder[SPECIFIC]                 =
    this.copy(version = version)
  def withDataContract(dataContract: DataContract): OutputPortFakerBuilder[SPECIFIC] =
    this.copy(dataContract = dataContract)
  def withSpecific(specific: SPECIFIC): OutputPortFakerBuilder[SPECIFIC]             =
    this.copy(specific = specific)

  def build(): OutputPort[SPECIFIC] = OutputPort[SPECIFIC](
    id = id,
    name = name,
    description = description,
    version = version,
    dataContract = dataContract,
    specific = specific
  )
}
