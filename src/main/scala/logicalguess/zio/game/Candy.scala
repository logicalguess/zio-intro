package logicalguess.zio.game

import java.io.IOException

import zio.console.{Console, putStrLn, getStrLn}
import zio.{App, IO, UIO, ZIO}

object Candy extends App {

    sealed trait Input
    case object Coin extends Input
    case object Turn extends Input
    case object Exit extends Input

    sealed trait Event
    case object CoinReceived extends Event
    case object CandyReleased extends Event
    case object InputIgnored extends Event
    case object Exited extends Event

    case class State(locked: Boolean, candies: Int, coins: Int)

    def run(args: List[String]) : ZIO[Environment, Nothing, Int] = {
        (for {
            _ <- candy
        } yield ())
          .fold(_ => 1, _ => 0)
    }

    val candy : ZIO[Console, IOException, Unit] = for {
        _ <- putStrLn(s"Let's start!")
        state = State(true, 5, 10)
        _ <- renderState(state)
        _ <- processLoop(state)
    } yield()

    def update: Input => State => (State, Event) = (i: Input) => (s: State) =>
        (i, s) match {
            case (Exit, _) => (s, Exited)
            case (_, State(_, 0, _)) => (s, InputIgnored)
            case (Coin, State(false, _, _)) => (s, InputIgnored)
            case (Turn, State(true, _, _)) => (s, InputIgnored)
            case (Coin, State(true, candy, coin)) =>
                (State(false, candy, coin + 1), CoinReceived)
            case (Turn, State(false, candy, coin)) =>
                (State(true, candy - 1, coin), CandyReleased)
            case (_, _) => (s, InputIgnored)
        }

    val getInput : ZIO[Console, IOException, Input] = for {
        line <- putStrLn(s"Please enter an input from: 'c', 't', or 'x'") *> getStrLn
        char <- line.toLowerCase.trim.headOption match {
            case None => putStrLn(s"You did not enter a character") *> getInput
            case Some('c') => UIO.succeed(Coin)
            case Some('t') => UIO.succeed(Turn)
            case Some('x') => UIO.succeed(Exit)
            case _ => putStrLn(s"Input not recognized. Valid inputs are: 'c', 't', or 'x'") *> getInput
        }
    } yield char

    def renderState(state: State) : ZIO[Console, IOException, Unit] = {
        putStrLn(state.toString)
    }

    def evaluate(in: Input, state: State): ZIO[Console, IOException, Boolean] = {
        in match {
            case Exit => putStrLn(s"Good bye!").as(false)
            case _ => IO.succeed(true)
        }
    }

    def processLoop(state: State) : ZIO[Console, IOException, Unit] = {
        for {
            input <- getInput
            se <- UIO.succeed(update(input)(state))
            s: State = se._1
            e: Event = se._2
            _ <- putStrLn(e.toString)
            _ <- renderState(s)
            loop <- evaluate(input, s)
            _ <- if (loop) processLoop(s) else UIO.succeed(s)
        } yield ()
    }
}
