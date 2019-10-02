package logicalguess.zio.hello

import java.io.IOException

import zio.console.{getStrLn, putStrLn}
import zio.{App, ZIO}

object HelloApp extends App {

  def logic: ZIO[Environment,IOException, Unit] =
    for {
      _ <- putStrLn("What's your name?")
      n <- getStrLn
      _ <- putStrLn("Hello, " + n + "!")
    } yield ()

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    (for {
      _ <- logic
    } yield ())
      .fold(_ => 1, _ => 0)
}
