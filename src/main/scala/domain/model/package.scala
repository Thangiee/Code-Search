package domain

package object model {

  case class Search(term: String, page: Int)

  case class Code(value: String) extends AnyVal
  case class Url(value: String) extends AnyVal
}
