package tinkl

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Json, JsonConfiguration}

case class InvoiceRequest(
    price: BigDecimal,
    currency: String,
    deferred: Boolean = false,
    timeLimit: Int = 900,
    orderId: Option[String] = None,
    itemCode: Option[String] = None,
    notificationUrl: Option[String] = None,
    redirectUrl: Option[String] = None,
    cancelUrl: Option[String] = None
)

object InvoiceRequest {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit val write  = Json.writes[InvoiceRequest]
}
