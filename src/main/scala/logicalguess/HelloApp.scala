package logicalguess

import java.io.IOException

import scalaz.zio.console._
import scalaz.zio.{App, IO}

object HelloApp extends App {

  def logic: IO[IOException, Unit] =
    for {
      _ <- putStrLn("What's your name?")
      n <- getStrLn
      _ <- putStrLn("Hello, " + n + "!")
    } yield ()

  override def run(args: List[String]): IO[Nothing, ExitStatus] =
    logic.
      attempt.
      map(_.fold(_ => 1, _ => 0)).
      map(ExitStatus.ExitNow(_))
}
