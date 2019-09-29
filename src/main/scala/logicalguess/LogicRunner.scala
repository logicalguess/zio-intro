package logicalguess

import scalaz.zio._

case class LogicRunner[E <: Throwable, A](val logic: IO[E, A]) extends App {

  override def run(args: List[String]): IO[Nothing, ExitStatus] = {
    logic.redeemPure(
      e => {
        e.printStackTrace()
        ExitStatus.ExitNow(1)
      },
      r => {
        println(r)
        ExitStatus.ExitNow(0)
      }
    )
  }
}
