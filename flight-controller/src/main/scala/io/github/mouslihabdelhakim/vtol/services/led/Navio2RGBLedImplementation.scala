package io.github.mouslihabdelhakim.vtol.services.led

import cats.effect.{Blocker, Concurrent, ContextShift, Sync, Timer}
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.Stream
import fs2.io.file.writeAll
import io.github.mouslihabdelhakim.vtol.services.led.Navio2RGBLed.{Color, LedState}

import java.nio.file.{Path, Paths, StandardOpenOption}
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.duration._

class Navio2RGBLedImplementation[F[_]](
    blockingExecutionContext: Blocker,
    redLedPath: Path,
    greenLedPath: Path,
    blueLedPath: Path,
    rate: FiniteDuration
)(implicit
    C: Concurrent[F],
    CS: ContextShift[F],
    T: Timer[F]
) extends Navio2RGBLed[F] {

  override def write(colors: Stream[F, Color]): Stream[F, Unit] = {
    val throttledColors = colors.metered(rate)
    val redStream       = writeGamma(_.red, redLedPath, throttledColors)
    val greenStream     = writeGamma(_.green, greenLedPath, throttledColors)
    val blueStream      = writeGamma(_.blue, blueLedPath, throttledColors)

    redStream.merge(greenStream).merge(blueStream)
  }

  private def writeGamma(
      toSingleLedState: Color => LedState,
      ledPath: Path,
      colors: Stream[F, Color]
  ): Stream[F, Unit] = {
    colors.changes
      .flatMap(c => toSingleLedState(c).representation)
      .through(
        writeAll(
          ledPath,
          blocker = blockingExecutionContext,
          flags = Seq(StandardOpenOption.WRITE)
        )
      )
  }

}

object Navio2RGBLedImplementation {

  def apply[F[_]](
      blockingExecutionContext: Blocker
  )(implicit
      C: Concurrent[F],
      CS: ContextShift[F],
      T: Timer[F]
  ): F[Navio2RGBLed[F]] = {
    for {
      redLedPath <- Sync[F].delay(Paths.get("/sys/class/leds/rgb_led0/brightness"))
      greenLedPath <- Sync[F].delay(Paths.get("/sys/class/leds/rgb_led1/brightness"))
      blueLedPath <- Sync[F].delay(Paths.get("/sys/class/leds/rgb_led2/brightness"))
    } yield new Navio2RGBLedImplementation[F](
      blockingExecutionContext,
      redLedPath,
      greenLedPath,
      blueLedPath,
      1.second
    )
  }

}
