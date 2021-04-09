package tinkl

import play.api.libs.json.JsonNaming.SnakeCase
import play.api.libs.json.{Json, JsonConfiguration}

import java.time.Instant

case class Payment(price: Long, txid: String, createdAt: Instant)

object Payment {
  implicit val config = JsonConfiguration(SnakeCase)
  implicit val read   = Json.reads[Payment]
}
