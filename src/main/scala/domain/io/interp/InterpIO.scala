package domain.io.interp

import java.util.concurrent.Executors

import cats._
import cats.implicits.{futureInstance, vectorInstance}
import cats.syntax.all._
import domain._
import domain.io._
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.concurrent.ExecutionContext
import scala.language.higherKinds
import scala.util.Try
import scalaj.http.Http

trait InterpIO[M[_]] {
  def interpreter: Interpreter[M]
  def run[A](io: IO[A])(implicit M: Monad[M]): M[A] = io.foldMap(interpreter)
}

object AsyncIO extends InterpIO[Async] {
  implicit val IOExeCtx = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(16))
  lazy val browser = JsoupBrowser()

  def interpreter: Interpreter[Async] = new Interpreter[Async] {
    def apply[A](fa: IOAdt[A]): Async[A] = fa match {
      case Pure(a)          => Async.pure(a)
      case MkRequests(urls) => urls.map(url => Async.fromHttpReq(Http(url))).sequence
      case FetchDoc(url)    => Async.fromHttpReq2xx(Http(url)).map(resp => browser.parseString(resp.body))
      case Log(msg)         => Async.right(println(msg))
      case Write(path, content) =>
        import better.files._
        Async.fromTry(Try(File(path) << content))
    }
  }
}