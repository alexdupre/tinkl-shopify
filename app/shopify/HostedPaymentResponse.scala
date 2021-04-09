package shopify

import java.time.Instant

import org.apache.commons.codec.digest.{HmacAlgorithms, HmacUtils}
import play.api.libs.json.{Json, OFormat}

// query parameters for redirect, form url-encoded for callback
case class HostedPaymentResponse(
    x_account_id: String,
    x_reference: String,
    x_currency: String,
    x_test: Boolean,
    x_amount: BigDecimal,
    x_gateway_reference: String,
    x_timestamp: Instant,
    x_result: PaymentResult,
    x_signature: Option[String],
    x_message: Option[String]
) {
  def toMap: Map[String, String] =
    Map(
      "x_account_id"        -> x_account_id,
      "x_reference"         -> x_reference,
      "x_currency"          -> x_currency,
      "x_test"              -> x_test.toString,
      "x_amount"            -> x_amount.underlying().toString,
      "x_gateway_reference" -> x_gateway_reference,
      "x_timestamp"         -> x_timestamp.toString,
      "x_result"            -> x_result.entryName
    ) ++ x_message.map(m => Map("x_message" -> m)).getOrElse(Map.empty) ++
      x_signature.map(s => Map("x_signature" -> s)).getOrElse(Map.empty)

  def toQueryParams: Map[String, Seq[String]] = toMap.view.mapValues(Seq(_)).toMap

  def sign(secret: String) = {
    val payload = toMap.toList.sortBy(_._1).map(kv => kv._1 + kv._2).mkString
    copy(x_signature = Some(new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret).hmacHex(payload)))
  }
}

object HostedPaymentResponse {
  implicit val format: OFormat[HostedPaymentResponse] = Json.format[HostedPaymentResponse]
}
