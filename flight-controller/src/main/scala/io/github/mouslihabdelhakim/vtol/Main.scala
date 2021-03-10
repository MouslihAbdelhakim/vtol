package io.github.mouslihabdelhakim.vtol

import cats.effect.{ExitCode, IO, IOApp}
import cats.syntax.parallel._
import fs2.Stream
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.MS5611
import io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250.MPU2950
import io.github.mouslihabdelhakim.vtol.services.navio2.led.RGB

import scala.concurrent.duration._

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    val rgpStream = RGB
      .acquire[IO]
      .use(led =>
        Stream
          .evals(
            IO.pure(
              List
                .fill(10)(
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

    val barometer = MS5611
      .stream[IO](2000.milliseconds) // hopefully we can have a 50hz loop
      .map(println)
      .compile
      .drain

    val imu = MPU2950
      .spi[IO]
      .flatMap(_.testConnection())
      .map(result => println(s"mpu2950 test ${result}"))

    List(
      barometer,
      rgpStream,
      imu
    ).parSequence.as(ExitCode.Success)
  }

}
