package tinkl

import play.api.libs.json.Json

import java.time.Instant

case class Status(status: String, date: Instant) {
  def isOk = status == "ok"
}

object Status {
  implicit val read = Json.reads[Status]
}
