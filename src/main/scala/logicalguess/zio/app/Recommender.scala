package logicalguess.zio.app

import zio.clock.Clock
import zio.console.Console
import zio.{DefaultRuntime, Ref, Schedule, ZIO, console}

// based on http://cloudmark.github.io/A-Journey-To-Zio/

object Recommender {
  case class User(id: Long, email: String, name: String)
  case class Product(id: Long, description: String)
  case class Recommendations(email: String, name: String, products: List[Product])

  trait Writer[W] {
    def writer: Ref[Vector[W]]
  }

  // Writer helpers:
  def log[W](w: W): ZIO[Writer[W], Nothing, Unit] = ZIO.accessM[Writer[W]](_.writer.update(vector => vector :+ w)).unit
  def getLogs[W]: ZIO[Writer[W], Nothing, Vector[W]] = ZIO.accessM[Writer[W]](_.writer.get)
  def clearLogs[W]: ZIO[Writer[W], Nothing, Unit] = ZIO.accessM[Writer[W]](_.writer.set(Vector()))

  val printLogs: ZIO[Console with Writer[String], Nothing, Unit] = for {
    logs <- getLogs[String]
    _ <- console.putStrLn(logs.mkString("\n"))
  } yield ()

  // Types and Helpers
  type Error = String
  type Log = Vector[String]

  def getRecommendationsById(userId: Long): ZIO[Writer[String], Nothing, List[Product]] =
    userId match {
      case 1 => ZIO.succeed(List(Product(1L, "Scala Rocks"), Product(2L, "Functional Programming")))
      case _ => ZIO.succeed(List.empty[Product])
    }

  def tryGettingUserByEmail(email: String): ZIO[Writer[String], Error, User] =
    email match {
      case "john@noreply.com" => ZIO.succeed(User(1L, "john@noreply.com", "John"))
      case _ => log(s"LOG -> User $email is not valid") *> ZIO.fail(s"ERROR -> User $email not found")
    }

  def tryGettingRecommendationsByEmail(email: String): ZIO[Writer[String] with Clock, Error, Recommendations] =
    for {
      _         <- log(s"LOG -> Searching for user $email")
      user      <- tryGettingUserByEmail(email).retry(Schedule.recurs(2))
      _         <- log(s"LOG -> Getting recommendations for user $email")
      products  <- getRecommendationsById(user.id)
    } yield Recommendations(user.email, user.name, products)

  def logic(email: String): ZIO[Console with Writer[String] with Clock, Error, Recommendations] = for {
    recs <- tryGettingRecommendationsByEmail(email)//.ensuring(printLogs)
    _ <- printLogs // not reached without ensuring if error
  } yield (recs)

  def main(args: Array[String]): Unit = {
    val email = "johnx@noreply.com"

    val runtime = new DefaultRuntime{}

    val zio = for {
      w <- Ref.make[Log](Vector[String]())
      live: Console with Clock with Writer[String] = new Console.Live with Clock.Live with Writer[String] {
        def writer: Ref[Vector[String]] = w
      }
      result <- logic(email).provide(live).catchAllCause(c => console.putStrLn("" + c.failures(0)))
    } yield result

    println(s"Recommendations for user $email: " + runtime.unsafeRun(zio))
  }

}