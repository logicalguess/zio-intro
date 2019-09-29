package logicalguess

import java.io.IOException

import zio.console.{Console, getStrLn, putStrLn}
import zio.{App, IO, UIO, ZIO}

object Hangman extends App {
    lazy val words : List[String] = scala.io.Source.fromResource("words.txt").getLines.toList

    case class State(name: String, guesses: Set[Char] = Set.empty[Char], word: String) {
        final def failures : Int = (guesses -- word.toSet).size
        final def playerLost: Boolean = failures >= 10
        final def playerWon : Boolean = (word.toSet -- guesses).size == 0
    }

    def run(args: List[String]) : ZIO[Environment, Nothing, Int] = {
        (for {
            _ <- hangman
        } yield ())
          .fold(_ => 1, _ => 0)
    }


    val hangman : ZIO[Console, IOException, Unit] = for {
        _ <- putStrLn("Welcome to purely functional hangman")
        name <- getName
        _ <- putStrLn(s"Welcome $name. Let's begin!")
        word <- chooseWord
        state = State(name, Set(), word)
        _ <- renderState(state)
        _ <- gameLoop(state)
    } yield()

    val getName : ZIO[Console, IOException, String] = for {
        _ <- putStrLn("What is your name: ")
        name <- getStrLn
    } yield name

    def nextInt(max: Int) : IO[Nothing, Int] =
        UIO.succeed(scala.util.Random.nextInt(max))

    val chooseWord: IO[IOException, String] = for {
        rand <- nextInt(words.length)
    } yield words.lift(rand).getOrElse("Bug in the program!")

    val getChoice : ZIO[Console, IOException, Char] = for {
        line <- putStrLn(s"Please enter a letter") *> getStrLn
        char <- line.toLowerCase.trim.headOption match {
            case None => putStrLn(s"You did not enter a character") *> getChoice
            case Some(x) => UIO.succeed(x)
        }
    } yield char

    def renderState(state: State) : ZIO[Console, IOException, Unit] = {
        val word = state.word.toList.map(c =>
            if (state.guesses.contains(c)) s" $c " else "   "
        ).mkString("")
        val line = List.fill(state.word.length)(" - ").mkString("")
        val guesses = " Guesses: " + state.guesses.toList.sorted.mkString("")
        val text = word + "\n" + line + "\n\n" + guesses + "\n"
        putStrLn(text)
    }

    def evaluate(guess: Char, state: State): ZIO[Console, IOException, Boolean] = {
        if (state.playerWon) putStrLn(s"Congratulations ${state.name} you won the game!").as(false)
        else if (state.playerLost) putStrLn(s"Sorry ${state.name} you lost the game. The word was ${state.word}").map(_ => false).as(false)
        else if (state.word.contains(guess)) putStrLn(s"You guessed correctly!").as(true)
        else putStrLn(s"Letter '$guess' not in the word!").as(true)
    }

    def gameLoop(state: State) : ZIO[Console, IOException, State] = {
        for {
            guess <- getChoice
            state <- UIO.succeed(state.copy(guesses = state.guesses + guess))
            _ <- renderState(state)
            loop <- evaluate(guess, state)
            state <- if (loop) gameLoop(state) else UIO.succeed(state)
        } yield state
    }
}
