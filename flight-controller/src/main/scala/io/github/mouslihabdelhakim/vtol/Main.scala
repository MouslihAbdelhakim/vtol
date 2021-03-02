package io.github.mouslihabdelhakim.vtol

import fs2.Stream
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import io.github.mouslihabdelhakim.vtol.services.led.Navio2RGBLed

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {

    Navio2RGBLed
      .make[IO](Blocker.liftExecutionContext(scala.concurrent.ExecutionContext.Implicits.global))
      .flatMap {
        _.write(
          Stream.evals(
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
                    Navio2RGBLed.Color.White,
                    Navio2RGBLed.Color.White
                  )
                )
                .flatten
            )
          )
        ).compile.drain
      }
      .as(ExitCode.Success)

  }

}
