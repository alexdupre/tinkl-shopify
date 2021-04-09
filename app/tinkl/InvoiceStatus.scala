package tinkl

import enumeratum.EnumEntry.Snakecase
import enumeratum._

sealed abstract class InvoiceStatus extends EnumEntry with Snakecase

object InvoiceStatus extends Enum[InvoiceStatus] with PlayJsonEnum[InvoiceStatus] {

  val values = findValues

  case object Deferred extends InvoiceStatus
  case object Pending  extends InvoiceStatus
  case object Partial  extends InvoiceStatus
  case object Payed    extends InvoiceStatus
  case object Error    extends InvoiceStatus
  case object Expired  extends InvoiceStatus

}
