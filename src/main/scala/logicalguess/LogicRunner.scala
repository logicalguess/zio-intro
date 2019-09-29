package logicalguess

import zio._

case class LogicRunner[E <: Throwable, A](val logic: IO[E, A]) extends App {

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
