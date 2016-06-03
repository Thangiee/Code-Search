package domain.service

import domain._
import cats.data._

trait FetchDoc[Search, Url, Doc] {
  def mkUrl: Reader[Search, Url]
  def fetch: ReaderT[IO, Url, Doc]
  def fetchDocFromSearch: ReaderT[IO, Search, Doc] = mkUrl.mapF[IO, Url](io.ops.pure) andThen fetch
}
