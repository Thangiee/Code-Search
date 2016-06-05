package domain.model

sealed trait Err
case class HttpErr(code: Int, url: Url, msg: String) extends Err
case class NonFatalErr(ex: Throwable) extends Err
