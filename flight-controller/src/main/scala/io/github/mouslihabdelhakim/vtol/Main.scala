package io.github.mouslihabdelhakim.vtol

import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import io.github.mouslihabdelhakim.vtol.services.led.Navio2RGBLed
import scala.concurrent.duration._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    Navio2RGBLed
      .apply[IO]
      .use(led =>
        Stream
          .evals(
            IO.pure(
              List
                .fill(100)(
                  List(
                    Navio2RGBLed.Color.Black,
                    Navio2RGBLed.Color.Red,
                    Navio2RGBLed.Color.Green,
                    Navio2RGBLed.Color.Blue,
                    Navio2RGBLed.Color.Cyan,
                    Navio2RGBLed.Color.Magenta,
                    Navio2RGBLed.Color.Yellow,
                    Navio2RGBLed.Color.White
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
