package it.agilelab.provisioning.storage.provisioner.app.api.validator

import cats.implicits.toBifunctorOps
import it.agilelab.provisioning.commons.validator.Validator
import it.agilelab.provisioning.mesh.self.service.api.model.Component.OutputPort
import it.agilelab.provisioning.mesh.self.service.api.model.{ Component, DataProduct, ProvisionRequest }
import it.agilelab.provisioning.storage.provisioner.core.gateway.cdp.CdpGateway
import it.agilelab.provisioning.storage.provisioner.core.models.{ DpCdp, S3Cdp }

object S3CdpValidator {

  def validator(
    cdpGateway: CdpGateway
  ): Validator[ProvisionRequest[DpCdp, S3Cdp]] =
    Validator[ProvisionRequest[DpCdp, S3Cdp]]
      .rule(
        r => isRightComponent(r.component),
        _ => "The provided component is not accepted by this provisioner"
      )
      .rule(
        r => isRightFolder(r),
        r =>
          s"Specific.Folder is not valid. Valid values start with mesh/domains/${r.dataProduct.domain}/data-products/${r.dataProduct.name}/${r.dataProduct.environment}/${getDPMajorVersion(r.dataProduct)}/"
      )
      .rule(
        r => isRightBucket(r, cdpGateway),
        _ =>
          s"Specific.Bucket is not valid. Valid bucket are the same configured on the Cdp Environment specified in the request"
      )

  def isRightComponent(c: Option[Component[S3Cdp]]): Boolean =
    c.fold(false)(o =>
      o.isInstanceOf[OutputPort[S3Cdp]] &&
        o.asInstanceOf[OutputPort[S3Cdp]].specific.isInstanceOf[S3Cdp]
    )

  def isRightFolder(request: ProvisionRequest[DpCdp, S3Cdp]): Boolean =
    request match {
      case ProvisionRequest(dp, c: Option[OutputPort[S3Cdp]]) if isRightComponent(c) =>
        c.fold(false)(o =>
          // TODO: check this structure. What happens if we have 2 S3 OP for the same DP?
          o.specific.folder.startsWith(
            s"mesh/domains/${dp.domain}/data-products/${dp.name}/${dp.environment}/${getDPMajorVersion(dp)}/"
          )
        )
      case _                                                                         => false
    }

  def isRightBucket(request: ProvisionRequest[DpCdp, S3Cdp], cdpGateway: CdpGateway): Boolean =
    request match {
      case ProvisionRequest(_, c: Option[OutputPort[S3Cdp]]) if isRightComponent(c) =>
        c.fold(false)(o =>
          cdpGateway
            .getStorageLocationBase(o.specific.cdpEnvironment)
            // TODO check also bucket logic
            .map {
              case s"s3a://$bucket/$_" => o.specific.bucket == bucket
              case s"s3a://$bucket/"   => o.specific.bucket == bucket
              case s"s3a://$bucket"    => o.specific.bucket == bucket
              case s"s3://$bucket/$_"  => o.specific.bucket == bucket
              case _                   => false
            }
            .leftMap(_ => false)
            .merge
        )
      case _                                                                        => false
    }

  private def getDPMajorVersion(dp: DataProduct[DpCdp]): String =
    dp.id match {
      case s"urn:dmb:dp:$_:$_:$major" => major
      case _                          => dp.version
    }

}
