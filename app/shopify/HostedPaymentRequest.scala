package shopify

import ai.x.play.json.Jsonx
import org.apache.commons.codec.digest.{HmacAlgorithms, HmacUtils}
import play.api.libs.json.Format

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

case class HostedPaymentRequest(
    x_account_id: String,
    x_amount: BigDecimal,
    x_currency: String,
    x_customer_billing_address1: Option[String],
    x_customer_billing_address2: Option[String],
    x_customer_billing_company: Option[String],
    x_customer_billing_city: Option[String],
    x_customer_billing_country: Option[String],
    x_customer_billing_phone: Option[String],
    x_customer_billing_state: Option[String],
    x_customer_billing_zip: Option[String],
    x_customer_email: Option[String],
    x_customer_first_name: Option[String],
    x_customer_last_name: Option[String],
    x_customer_phone: Option[String],
    x_customer_shipping_address1: Option[String],
    x_customer_shipping_address2: Option[String],
    x_customer_shipping_company: Option[String],
    x_customer_shipping_city: Option[String],
    x_customer_shipping_country: Option[String],
    x_customer_shipping_first_name: Option[String],
    x_customer_shipping_last_name: Option[String],
    x_customer_shipping_phone: Option[String],
    x_customer_shipping_state: Option[String],
    x_customer_shipping_zip: Option[String],
    x_description: Option[String],
    x_invoice: Option[String],
    x_reference: String,
    x_shop_country: String,
    x_shop_name: String,
    x_signature: String,
    x_test: Boolean,
    x_url_callback: String,
    x_url_cancel: String,
    x_url_complete: String
)

object HostedPaymentRequest {

  def validate(
      params: Map[String, Seq[String]],
      credentials: String => Future[Option[String]]
  )(implicit
      ec: ExecutionContext
  ): Future[(HostedPaymentRequest, String)] = {
    def parseRequest = {
      def parse[T](name: String, f: String => T) =
        try {
          f(params.get(name).flatMap(_.headOption).get)
        } catch {
          case NonFatal(_) => sys.error(s"Missing or invalid $name field")
        }

      def parseOpt[T](name: String, f: String => T) =
        try {
          params.get(name).flatMap(_.headOption).map(f)
        } catch {
          case NonFatal(_) => sys.error(s"Missing or invalid $name field")
        }

      import scala.reflect.runtime.universe._
      val m     = runtimeMirror(classOf[HostedPaymentRequest].getClassLoader)
      val c     = typeOf[HostedPaymentRequest].typeSymbol.asClass
      val cm    = m.reflectClass(c)
      val ctor  = typeOf[HostedPaymentRequest].decl(termNames.CONSTRUCTOR).asMethod
      val ctorm = cm.reflectConstructor(ctor)
      val cparams = ctor.paramLists.head.map { case s =>
        val paramName = s.name.toString
        val paramType = s.typeSignature.toString
        paramType match {
          case "String"         => parse(paramName, identity)
          case "BigDecimal"     => parse(paramName, BigDecimal.apply)
          case "Option[String]" => parseOpt(paramName, identity)
          case "Boolean"        => parse(paramName, java.lang.Boolean.parseBoolean)
          case _                => sys.error(s"Unexpected type: $paramType")
        }
      }
      ctorm(cparams: _*).asInstanceOf[HostedPaymentRequest]
    }

    for {
      req    <- Future(parseRequest)
      secret <- credentials(req.x_account_id)
    } yield {
      secret match {
        case None => sys.error("Unknown accountId")
        case Some(secret) =>
          val payload =
            params.view
              .filterKeys(k => k.startsWith("x_") && k != "x_signature")
              .toList
              .sortBy(_._1)
              .map(kv => kv._1 + kv._2.mkString)
              .mkString
          if (!req.x_signature.equalsIgnoreCase(new HmacUtils(HmacAlgorithms.HMAC_SHA_256, secret).hmacHex(payload)))
            sys.error("Invalid signature")
          (req, secret)
      }
    }
  }

  implicit val format: Format[HostedPaymentRequest] =
    Jsonx.formatCaseClass[HostedPaymentRequest]
}
