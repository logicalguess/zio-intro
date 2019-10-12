package logicalguess.zio.util

import zio.console.Console
import zio.{App, ZIO}

case class LogicRunner[E <: Throwable, A](logic: ZIO[Console, E, A]) extends App {

  override def run(args: List[String]): ZIO[Environment, Nothing, Int] = {

    (for {
      out <- logic
    } yield out)
      .fold(
        e => {
          e.printStackTrace()
          1
        },
        r => {
          println(r)
          0
        }
      )
  }
}
