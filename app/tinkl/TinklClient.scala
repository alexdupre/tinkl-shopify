package tinkl

import play.api.Logger
import play.api.libs.json.{JsValue, Json, Reads}
import play.api.libs.ws.{WSClient, WSResponse}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class TinklClient(clientId: String, token: String, staging: Boolean, wsClient: WSClient)(implicit
    ec: ExecutionContext
) {

  private val logger = Logger(getClass)

  private val baseUrl = if (staging) "https://api-staging.tinkl.it/v1" else "https://api.tinkl.it/v1"

  private def initClient(path: String) = {
    wsClient.url(s"$baseUrl/$path").addHttpHeaders("X-CLIENT-ID" -> clientId, "X-AUTH-TOKEN" -> token)
  }

  private def checkForError(resp: WSResponse): Unit = {
    resp.header("X-Request-Id").foreach(v => logger.trace("Tinkl Request Id: " + v))
    if (resp.status / 100 != 2) {
      logger.warn("Tinkl Error Status: " + resp.status)
      logger.warn("Tinkl Error Payload: " + resp.body)
      sys.error("Tinkl API Error") // TODO
      /*
      { "errors": [ 'not_authorized' ] }
       */
    } else {
      logger.trace("Tinkl Response Payload:")
      logger.trace(Json.prettyPrint(resp.json))
    }
  }

  private def handleResponse[T](resp: WSResponse)(implicit reads: Reads[T]): T = {
    checkForError(resp)
    resp.json.as[T]
  }

  protected def get[T](path: String)(implicit reads: Reads[T]) = {
    initClient(path).get().map(handleResponse[T])
  }

  protected def post[T](path: String, params: JsValue)(implicit reads: Reads[T]) = {
    initClient(path).post(params).map(handleResponse[T])
  }

  protected def patch[T](path: String, params: JsValue = Json.obj())(implicit reads: Reads[T]) = {
    initClient(path).patch(params).map(handleResponse[T])
  }

  def getStatus(): Future[Status] = get[Status]("status")

  def createInvoice(request: InvoiceRequest): Future[Invoice] = post[Invoice]("invoices", Json.toJson(request))

  def activateInvoice(guid: UUID): Future[Invoice] = patch[Invoice](s"invoices/$guid/activate")

  def getInvoice(guid: UUID): Future[Invoice] = get[Invoice](s"invoices/$guid")

  def getInvoices( /*filter: InvoiceFilter*/ ): Future[JsValue] = get[JsValue]("invoices")

}
