package domain.model

import cats.data.{Xor, XorT}
import cats.implicits.futureInstance
import domain._

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scalaj.http.HttpRequest

object Async {
  def right[A](a: A)(implicit ec: ExeCtx): Async[A] = XorT.right[Future, Err, A](Future.successful(a))

  def left[A](err: Err)(implicit ec: ExeCtx): Async[A] = XorT.left[Future, Err, A](Future.successful(err))

  def pure[A](a: A)(implicit ec: ExeCtx): Async[A] = XorT.pure[Future, Err, A](a)

  def fromTry[A](`try`: Try[A])(implicit ec: ExeCtx): Async[A] = XorT(Future {
    `try` match {
      case Success(a)  => Xor.Right(a)
      case Failure(ex) => Xor.left(NonFatalErr(ex))
    }
  })

  def fromHttpReq(req: HttpRequest)(implicit ec: ExeCtx): Async[Response] = fromTry(Try(req.asString))

  def fromHttpReq2xx(req: HttpRequest)(implicit ec: ExeCtx): Async[Response] = fromHttpReq(req)
    .flatMap(resp => if (resp.is2xx) Async.right(resp) else Async.left(HttpErr(resp.code, Url(req.url), resp.body)))
}
