import cats.free.Free
import cats.~>
import domain.io.IOAdt

import scala.concurrent.ExecutionContext
import scala.language.higherKinds
import scalaj.http.HttpResponse

package object domain {
  type IO[A] = Free[IOAdt, A]
  type Interpreter[M[_]] = (IOAdt ~> M)
  type Response = HttpResponse[String]
  type ExeCtx = ExecutionContext
}
