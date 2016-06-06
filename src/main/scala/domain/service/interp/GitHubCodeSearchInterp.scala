package domain.service.interp

import cats.data.{Reader, _}
import domain.model.{Code, Search, Url}
import domain.service.GitHubCodeSearch
import domain.{io, _}
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.model.Document

import scala.util.matching.Regex

class GitHubCodeSearchInterp extends GitHubCodeSearch[Search, Url, Document, Code, String] {

  def mkUrl: Reader[Search, Url] = Reader(s =>
    Url(s"https://github.com/search?p=${s.page}&q=${s.term.split(" ").mkString("+")}&ref=searchresults&type=Code"))

  def find(patten: Regex) = ReaderT.apply[Vector, Code, String](code =>
    patten.findAllMatchIn(code.value).map(_.matched).toVector)

  def fetch = ReaderT.apply[IO, Url, Document](url => io.ops.fetchDoc(url.value))

  def extractCode = ReaderT.apply[Vector, Document, Code](doc =>
    (doc >> texts("td[class=blob-code blob-code-inner]")).map(Code(_)).toVector)
}

object GitHubCodeSearch extends GitHubCodeSearchInterp
