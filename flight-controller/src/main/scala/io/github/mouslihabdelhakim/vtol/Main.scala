package io.github.mouslihabdelhakim.vtol

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import io.github.mouslihabdelhakim.vtol.services.navio2.led.RGB

import scala.concurrent.duration._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    RGB
      .acquire[IO]
      .use(led =>
        Stream
          .evals(
            IO.pure(
              List
                .fill(100)(
                  List(
                    RGB.Color.Black,
                    RGB.Color.Red,
                    RGB.Color.Green,
                    RGB.Color.Blue,
                    RGB.Color.Cyan,
                    RGB.Color.Magenta,
                    RGB.Color.Yellow,
                    RGB.Color.White
                  )
                )
                .flatten
            )
          )
          .metered(100.millis)
          .through(led)
          .compile
          .drain
      )
      .as(ExitCode.Success)

}
