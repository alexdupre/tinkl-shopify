package tinkl

import play.api.libs.json.{JsResult, JsValue, Json, JsonConfiguration, Reads}
import play.api.libs.json.JsonNaming.SnakeCase

import java.time.Instant
import java.util.UUID

sealed trait Invoice {
  val url: String
  val price: BigDecimal
  val currency: String
  val invoiceTime: Instant
  val currentTime: Instant
  val guid: UUID
  val orderId: Option[String]
  val itemCode: Option[String]
  val notificationUrl: Option[String]
  val redirectUrl: Option[String]
  val cancelUrl: Option[String]
}

case class StandardInvoice(
    url: String,
    status: InvoiceStatus,
    btcAddress: String,
    btcSegwitAddress: String,
    btcPrice: BigDecimal,
    price: BigDecimal,
    currency: String,
    invoiceTime: Instant,
    expirationTime: Instant,
    currentTime: Instant,
    guid: UUID,
    rate: BigDecimal,
    orderId: Option[String],
    itemCode: Option[String],
    paymentConfidence: PaymentConfidence,
    notificationUrl: Option[String],
    redirectUrl: Option[String],
    cancelUrl: Option[String],
    payments: Option[Seq[Payment]]
) extends Invoice

case class DeferredInvoice(
    url: String,
    price: BigDecimal,
    currency: String,
    invoiceTime: Instant,
    currentTime: Instant,
    guid: UUID,
    orderId: Option[String],
    itemCode: Option[String],
    notificationUrl: Option[String],
    redirectUrl: Option[String],
    cancelUrl: Option[String],
    activationUrl: Option[String],
    activationPage: Option[String]
) extends Invoice

object Invoice {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit val read = new Reads[Invoice] {
    override def reads(json: JsValue): JsResult[Invoice] = {
      implicit val readStandardInvoice = Json.reads[StandardInvoice]
      implicit val readDeferredInvoice = Json.reads[DeferredInvoice]
      if ((json \ "status").asOpt[InvoiceStatus].contains(InvoiceStatus.Deferred)) json.validate[DeferredInvoice]
      else json.validate[StandardInvoice]
    }
  }
}
