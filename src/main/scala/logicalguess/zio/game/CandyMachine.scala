package logicalguess.zio.game

import java.io.IOException

import zio.console.{Console, getStrLn, putStrLn}
import zio.stream.{Sink, Stream}
import zio.{App, ZIO}

object CandyMachine extends App {

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
            _ <- program1
        } yield ())
          .fold(_ => 1, _ => 0)
    }

    def simulate(inputs: List[Input])(state: State): ZIO[Any, Nothing, State] = {
        def streamReduce(state: State, input: Input): State = CandyMachine.update(input)(state)._1
        Stream.fromIterable(inputs).run(Sink.foldLeft(state)(streamReduce))
    }

    val program : ZIO[Console, IOException, Unit] = for {
        _ <- putStrLn(s"Let's start!")
        state = State(true, 5, 10)
        _ <- renderState(state)
        _ <- processLoop(state)
    } yield()

    val program1 : ZIO[Console, IOException, Unit] = for {
        _ <- putStrLn(s"Let's test!")
        start = State(true, 5, 10)
        state <- simulate(List(Coin, Turn))(start)
        _ <- renderState(state)
        _ <- putStrLn("" + state.candies)
    } yield()

    val update: Input => State => (State, Event) = (i: Input) => (s: State) =>
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
            case Some('c') => ZIO.succeed(Coin)
            case Some('t') => ZIO.succeed(Turn)
            case Some('x') => ZIO.succeed(Exit)
            case _ => putStrLn(s"Input not recognized. Valid inputs are: 'c', 't', or 'x'") *> getInput
        }
    } yield char

    def recordEvent(event: Event) : ZIO[Console, IOException, Unit] = {
        putStrLn("EVENT -> " + event)
    }

    def renderState(state: State) : ZIO[Console, IOException, Unit] = {
        putStrLn("STATE -> " + state)
    }

    def evaluate(in: Input, state: State): ZIO[Console, IOException, Boolean] = {
        in match {
            case Exit => putStrLn(s"Good bye!").as(false)
            case _ => ZIO.succeed(true)
        }
    }

    def processLoop(state: State) : ZIO[Console, IOException, Unit] = {
        for {
            input <- getInput
            se <- ZIO.succeed(update(input)(state))
            s: State = se._1
            e: Event = se._2
            _ <- recordEvent(e)
            _ <- renderState(s)
            loop <- evaluate(input, s)
            _ <- if (loop) processLoop(s) else ZIO.succeed(s)
        } yield ()
    }
}
