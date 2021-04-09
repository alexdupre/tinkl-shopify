package tinkl

import enumeratum.EnumEntry.Snakecase
import enumeratum._

sealed abstract class PaymentConfidence extends EnumEntry with Snakecase

object PaymentConfidence extends Enum[PaymentConfidence] with PlayJsonEnum[PaymentConfidence] {

  val values = findValues

  case object Unknown            extends PaymentConfidence
  case object SeenButUnconfirmed extends PaymentConfidence
  case object Confirmed          extends PaymentConfidence
  // others?!

}
