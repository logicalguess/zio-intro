package logicalguess.zio.stream

import logicalguess.zio.util.LogicRunner
import zio.UIO
import zio.stream.{Sink, Stream}

object Streams {

    val stream: Stream[Nothing, Int] = Stream.fromIterable(1 to 5)
  //  val stream: Stream[Nothing, Int] = Stream(1,2,3).merge(Stream(2,3,4))
  //  val logic = stream.foreach(i => putStrLn(i.toString))


    def streamReduce(total: Int, element: Int): Int = total + element
    val logic: UIO[Int] = stream.run(Sink.foldLeft(0)(streamReduce))


  def main(args: Array[String]): Unit = {
    LogicRunner(logic).main(Array.empty)
  }
}
