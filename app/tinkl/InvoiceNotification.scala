package tinkl

import play.api.libs.json.{Json, JsonConfiguration}
import play.api.libs.json.JsonNaming.SnakeCase

import java.time.Instant
import java.util.UUID

case class InvoiceNotification(
    guid: UUID,
    price: BigDecimal,
    currency: String,
    status: InvoiceStatus,
    invoiceTime: Instant,
    orderId: Option[String],
    itemCode: Option[String]
)

object InvoiceNotification {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit val read   = Json.reads[InvoiceNotification]
}
