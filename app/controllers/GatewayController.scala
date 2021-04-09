package controllers

import com.google.inject.Inject
import models.MerchantsDatabase
import play.api.{Configuration, Logging}
import play.api.libs.json.Json
import play.api.libs.ws.WSClient
import play.api.mvc.InjectedController
import shopify.{HostedPaymentRequest, HostedPaymentResponse, PaymentResult}
import tinkl.{InvoiceNotification, InvoiceRequest, InvoiceStatus, StandardInvoice, TinklClient}

import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.{ExecutionContext, Future}

class GatewayController @Inject() (db: MerchantsDatabase, conf: Configuration, wsClient: WSClient)(implicit
    ec: ExecutionContext
) extends InjectedController
    with Logging {

  private val timeLimit       = conf.get[Int]("tinkl.timeLimit")
  private val notificationUrl = conf.get[String]("tinkl.notificationUrl")

  // simulate redis cache
  private val cache = new ConcurrentHashMap[String, (HostedPaymentRequest, UUID)]()

  def index = Action(Ok("Tinkl <-> Shopify Demo Gateway"))

  def paymentInit = Action.async(parse.formUrlEncoded) { request =>
    HostedPaymentRequest.validate(request.body, db.getShopifySecret).flatMap { case (req, secret) =>
      logger.trace(Json.prettyPrint(Json.toJson(req)))
      if (req.x_currency != "EUR") {
        val resp = HostedPaymentResponse(
          req.x_account_id,
          req.x_reference,
          req.x_currency,
          req.x_test,
          req.x_amount,
          "",
          Instant.now,
          PaymentResult.Failed,
          None,
          Some("EUR is the only supported currency.")
        ).sign(secret)
        Future.successful(Redirect(req.x_url_complete, resp.toMap.view.mapValues(v => Seq(v)).toMap))
      } else {
        for {
          (clientId, token) <- db.getTinklCredentials(req.x_account_id)
          tinkl = new TinklClient(clientId, token, req.x_test, wsClient)
          invoice <- tinkl
            .createInvoice(
              InvoiceRequest(
                req.x_amount,
                req.x_currency,
                timeLimit = timeLimit,
                orderId = Some(req.x_reference),
                notificationUrl = Some(notificationUrl),
                redirectUrl = Some(s"https://tinkl-shopify.herokuapp.com/completed/${req.x_reference}"),
                cancelUrl = Some(req.x_url_cancel)
              )
            )
        } yield {
          cache.put(req.x_reference, (req, invoice.guid))
          Redirect(invoice.url)
        }
      }
    }
  }

  private def checkPayment(req: HostedPaymentRequest, guid: UUID): Future[Option[HostedPaymentResponse]] = {
    for {
      (clientId, token) <- db.getTinklCredentials(req.x_account_id)
      tinkl = new TinklClient(clientId, token, req.x_test, wsClient)
      invoice <- tinkl.getInvoice(guid)
      response <- invoice match {
        case i: StandardInvoice
            if i.status == InvoiceStatus.Payed && i.price == req.x_amount && i.currency == req.x_currency =>
          for {
            Some(secret) <- db.getShopifySecret(req.x_account_id)
          } yield Some(
            HostedPaymentResponse(
              req.x_account_id,
              req.x_reference,
              req.x_currency,
              req.x_test,
              req.x_amount,
              i.guid.toString,
              Instant.now,
              PaymentResult.Completed,
              None,
              None
            ).sign(secret)
          )
        case _ => Future.successful(None)
      }
    } yield response
  }

  def paymentNotification = Action.async(parse.json[InvoiceNotification]) { request =>
    logger.trace(request.body.toString)
    request.body.orderId.flatMap(oid => Option(cache.get(oid))) match {
      case Some((req, guid)) if guid == request.body.guid && request.body.status == InvoiceStatus.Payed =>
        for {
          hpr <- checkPayment(req, guid)
          _ <- hpr match {
            case Some(response) =>
              for {
                resp <- wsClient.url(req.x_url_callback).post(response.toQueryParams)
              } yield {
                logger.trace(s"Shopify response: ${resp.status}")
                if (resp.status != 200) sys.error(s"Callback HTTP error ${resp.status} with body: " + resp.body)
                // it may be fine to fail here if tinkl re-submit notifications on callback error,
                // but it should be better to store the result and retry internally
              }
            case _ => Future.unit
          }
        } yield Ok
      case _ => Future.successful(Ok)
    }
  }

// status=success or status=error
  def paymentCompleted(ref: String, status: String) = Action.async {
    Option(cache.get(ref)) match {
      case Some((req, guid)) =>
        for {
          hpr <- checkPayment(req, guid)
        } yield {
          // cleanup the cache?
          hpr match {
            case Some(response) => Redirect(req.x_url_complete, response.toQueryParams)
            case _              => Redirect(req.x_url_cancel)
          }
        }
      case _ => Future.successful(Ok)
    }
  }

}
