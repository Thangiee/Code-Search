package domain.io

import better.files.File
import cats.free.Free
import domain.{IO, Response}
import net.ruippeixotog.scalascraper.model.Document

sealed trait IOAdt[A]
case class Pure[T](t: T) extends IOAdt[T]
case class MkRequests(url: Vector[String]) extends IOAdt[Vector[Response]]
case class FetchDoc(url: String) extends IOAdt[Document]
case class Log(msg: String) extends IOAdt[Unit]
case class Write(path: String, content: String) extends IOAdt[File]

object ops {
  def pure[A](a: A): IO[A] = Free.liftF(Pure(a))
  def mkRequests(urls: Vector[String]): IO[Vector[Response]] = Free.liftF(MkRequests(urls))
  def log(msg: String): IO[Unit] = Free.liftF(Log(msg))
  def fetchDoc(url: String): IO[Document] = Free.liftF(FetchDoc(url))
  def write(path: String, content: String): IO[File] = Free.liftF(Write(path, content))
}
