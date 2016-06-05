import cats.data.XorT
import cats.free.Free
import cats.~>
import domain.io.IOAdt
import domain.model.Err

import scala.concurrent.{ExecutionContext, Future}
import scala.language.higherKinds
import scalaj.http.HttpResponse

package object domain {
  type IO[A] = Free[IOAdt, A]
  type Interpreter[M[_]] = (IOAdt ~> M)
  type Response = HttpResponse[String]
  type ExeCtx = ExecutionContext

  type Async[A] = XorT[Future, Err, A]
  val Async = model.Async
}
