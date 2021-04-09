package shopify

import enumeratum.EnumEntry.Snakecase
import enumeratum._

sealed abstract class PaymentResult extends EnumEntry with Snakecase

object PaymentResult extends Enum[PaymentResult] with PlayJsonEnum[PaymentResult] {

  val values = findValues

  case object Completed extends PaymentResult
  case object Failed    extends PaymentResult
  case object Pending   extends PaymentResult

}
