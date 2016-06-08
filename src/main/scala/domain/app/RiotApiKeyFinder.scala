package domain.app

import java.nio.charset.Charset
import java.util.Base64

import cats.data._
import cats.std.all._
import domain._
import domain.model._

import scala.concurrent.Future
import scala.concurrent.duration._

case class RiotApiKeyFinder(terms: String, min: Int, max: Int) {
  import io.interp.AsyncIO.IOExeCtx
  import io.ops._
  import service.interp.GitHubCodeSearch._

  val uuidRegx = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}".r

  def encode64(key: Key): String = Base64.getEncoder.encodeToString(key.value.getBytes(Charset.defaultCharset()))

  def findKeys(search: Search): IO[Vector[Key]] =
    for {
      _     <- log(s"Searching $search")
      keys  <- fetchDocFromSearch(search).map(findFromDoc(uuidRegx).map(Key(_)).run)
      keys2 <- Key.validate(keys.distinct)
      (validKeys, invalidKeys) = keys2
      _     <- log(s"$search yielded ${validKeys.size} valid keys and ${invalidKeys.size} invalid keys.")
    } yield validKeys

  def saveKeys(keys: Set[Key]): IO[Unit] = for {
    f1   <- write("./keys.txt", keys.map(_.value).mkString("\n"))
    _    <- log(s"Valid keys append to ${f1.pathAsString}")
    f2   <- write("./keys-encoded.txt", keys.map(encode64).mkString("\n"))
    _    <- log(s"Valid keys encoded append to ${f2.pathAsString}")
  } yield ()

  val step = StateT[Future, Program, Unit] {
    case p@Program(h +: t, keys, Running) =>
      io.interp.AsyncIO.run(findKeys(h))
        .map(newKeys => (Program(t, keys ++ newKeys, Throttle(6.seconds)), ())) // throttle for github api rate limit, 10 calls per minute
        .leftMap {
          case NonFatalErr(ex) => (p.copy(state = Error(ex)), ())
          case err: HttpErr => (p.copy(state = Error(new Throwable(err.toString))), ())
        }
        .merge
    case p@Program(_, _, Throttle(duration)) => Future { Thread.sleep(duration.toMillis); (p.copy(state = Running), ()) }
    case p@Program(_, _, _) => Future.successful((p.copy(state = Done), ()))
  }

  def simulate(prgm: Program): Future[Program] = step.runS(prgm).flatMap {
    case p@Program(_, _, Running)     => simulate(p) // keep running the program
    case p@Program(_, _, Throttle(_)) => simulate(p) // keep running the program
    case p: Program                   => Future.successful(p) // stop the program, state Done or Error
  }

  val genSearches = for {term <- terms.split('|').toVector; page <- min to max} yield Search(term, page)

  simulate(Program(genSearches, Set.empty[Key], Running)).map { prgm =>
    io.interp.AsyncIO.run(
      for {
        _ <- log(genReport(prgm))
        _ <- saveKeys(prgm.keys)
      } yield ()
    ).value.onComplete(_ => System.exit(0))
  }

  def genReport(prgm: Program): String = {
    val keyFoundReport = s"${prgm.keys.size} valid api keys were found:\n${prgm.keys.map(_.value).mkString("\n")}"
    prgm.state match {
      case Error(ex) =>
        s"""
          |Program encountered an error: ${ex.getMessage}
          |The following searches were not completed:
          |${prgm.searches.mkString("\n")}
          |
          |$keyFoundReport
          |
        """.stripMargin
      case _ =>
        s"""
          |Program finished!
          |
          |$keyFoundReport
          |
        """.stripMargin
    }
  }
}

object RiotApiKeyFinder extends App {
  val (terms, min, max) = (args(0), args(1).toInt, args(2).toInt)

  require(terms.nonEmpty, "Search terms can not be blank")
  require(min >= 1, s"Min page($min) cannot be less than 1")
  require(max >= 1, s"Max page($max) cannot be less than 1")
  require(min <= max, "Max page cannot be less than min page")

  RiotApiKeyFinder(terms, min, max)
}

sealed trait ProgramState
case object Running extends ProgramState
case object Done extends ProgramState
case class Throttle(duration: Duration) extends ProgramState
case class Error(throwable: Throwable) extends ProgramState

case class Program(searches: Vector[Search], keys: Set[Key] ,state: ProgramState)

