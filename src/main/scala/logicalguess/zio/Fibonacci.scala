package logicalguess.zio

import zio.IO

object Fibonacci {

  def fib(n: Int): IO[Nothing, Int] =
    if (n <= 1)
      IO.succeed(1)
    else
      for {
        f1 <- fib(n - 2).fork
        f2 <- fib(n - 1).fork
        v2 <- f2.join
        v1 <- f1.join
      } yield v1 + v2

  def main(args: Array[String]): Unit = {
    val logic = fib(5)
    LogicRunner(logic).main(Array.empty)
  }
}
