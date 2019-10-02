package logicalguess.zio.game

import java.io.IOException

import zio.console.{Console, putStrLn}
import zio.{App, IO, UIO, ZIO}

object Candy extends App {

    sealed trait Input
    case object Coin extends Input
    case object Turn extends Input
    case object Exit extends Input

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

    def update: Input => State => State = (i: Input) => (s: State) =>
        (i, s) match {
            case (Exit, _) => s
            case (_, State(_, 0, _)) => s
            case (Coin, State(false, _, _)) => s
            case (Turn, State(true, _, _)) => s
            case (Coin, State(true, candy, coin)) =>
                State(false, candy, coin + 1)
            case (Turn, State(false, candy, coin)) =>
                State(true, candy - 1, coin)
        }

    val getInput : ZIO[Console, IOException, Input] = for {
        line <- putStrLn(s"Please enter an input from 'c', 't', or 'x'") *> getStrLn
        char <- line.toLowerCase.trim.headOption match {
            case None => putStrLn(s"You did not enter a character") *> getInput
            case Some('c') => UIO.succeed(Coin)
            case Some('t') => UIO.succeed(Turn)
            case Some('x') => UIO.succeed(Exit)
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

    def processLoop(state: State) : ZIO[Console, IOException, State] = {
        for {
            input <- getInput
            state <- UIO.succeed(update(input)(state))
            _ <- renderState(state)
            loop <- evaluate(input, state)
            state <- if (loop) processLoop(state) else UIO.succeed(state)
        } yield state
    }
}
