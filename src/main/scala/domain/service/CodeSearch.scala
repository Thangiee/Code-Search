package domain.service

import cats.data._
import cats.implicits.vectorInstance

import scala.util.matching.Regex

trait CodeSearch[Doc, Code, Result] {
  def extractCode: ReaderT[Vector, Doc, Code]
  def find(pattern: Regex): ReaderT[Vector, Code, Result]
  def findFromDoc(pattern: Regex): ReaderT[Vector, Doc, Result] = extractCode andThen find(pattern)
}
