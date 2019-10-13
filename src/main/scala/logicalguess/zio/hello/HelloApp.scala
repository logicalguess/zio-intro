package logicalguess.zio.hello

import java.io.IOException

import zio.console.{Console, getStrLn, putStrLn}
import zio.{App, ZIO}

object HelloApp extends App {

  def logic: ZIO[Console,IOException, Unit] =
    for {
      _ <- putStrLn("What's your name?")
      name <- getStrLn
      _ <- putStrLn("Hello, " + name + "!")
    } yield ()

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] =
    (for {
      _ <- logic
    } yield ())
      .fold(_ => 1, _ => 0)
}
