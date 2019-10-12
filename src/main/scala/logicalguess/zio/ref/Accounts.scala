package logicalguess.zio.ref

import logicalguess.zio.util.LogicRunner
import zio.{IO, Ref}

object Accounts {

  val from: IO[Nothing, Ref[Int]] = Ref.make(100)

  val to1: Ref[Int] => IO[Nothing, Boolean] =
    (act: Ref[Int]) =>
      act.get.flatMap(
        v => act.modify(v0 => if (v0 == v) (true, v - 30) else (false, v))
      )

  val to2: Ref[Int] => IO[Nothing, Boolean] =
    (act: Ref[Int]) =>
      act.get.flatMap(
        v => act.modify(v0 => if (v0 == v) (true, v - 50) else (false, v))
      )

  val logic: IO[Nothing, Int] = for {
    accountR <- from
    _ <- to1(accountR).zipPar(to2(accountR))
    v <- accountR.get
  } yield v

  def main(args: Array[String]): Unit = {
    LogicRunner(logic).main(Array.empty)
  }
}
