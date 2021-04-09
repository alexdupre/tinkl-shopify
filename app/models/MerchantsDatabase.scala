package models

import com.google.inject.Inject
import play.api.Configuration

import scala.concurrent.Future

// simulate remote db call to merchants database
class MerchantsDatabase @Inject() (configuration: Configuration) {

  def getShopifySecret(accountId: String) =
    Future.successful(configuration.get[Option[String]](s"credentials.$accountId.shopifyPassword"))

  def getTinklCredentials(accountId: String) = Future.successful(
    configuration.get[String](s"credentials.$accountId.tinklClientId") -> configuration.get[String](
      s"credentials.$accountId.tinklToken"
    )
  )

}
