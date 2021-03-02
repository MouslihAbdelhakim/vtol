package io.github.mouslihabdelhakim.vtol.services.led

import cats.Eq
import cats.effect.{Blocker, Concurrent, ContextShift, Resource, Sync}
import cats.syntax.flatMap._
import cats.syntax.functor._
import fs2.io.file.writeAll
import fs2.{Pipe, Pure, Stream}

import java.nio.file.{Path, Paths, StandardOpenOption}

object Navio2RGBLed {

  def apply[F[_]](implicit
      C: Concurrent[F],
      CS: ContextShift[F]
  ): Resource[F, Pipe[F, Color, Unit]] =
    Blocker.apply[F].evalMap { ec =>
      for {
        redLedPath <- Sync[F].delay(Paths.get("/sys/class/leds/rgb_led0/brightness"))
        greenLedPath <- Sync[F].delay(Paths.get("/sys/class/leds/rgb_led1/brightness"))
        blueLedPath <- Sync[F].delay(Paths.get("/sys/class/leds/rgb_led2/brightness"))
      } yield apply(ec, redLedPath, greenLedPath, blueLedPath)
    }

  def apply[F[_]](
      ec: Blocker,
      redLed: Path,
      greenLed: Path,
      blueLed: Path
  )(implicit
      C: Concurrent[F],
      CS: ContextShift[F]
  ): Pipe[F, Color, Unit] = colors => {
    val redStream   = writeGamma(_.red, redLed, colors, ec)
    val greenStream = writeGamma(_.green, greenLed, colors, ec)
    val blueStream  = writeGamma(_.blue, blueLed, colors, ec)
    redStream.merge(greenStream).merge(blueStream)
  }

  sealed abstract class LedState(
      val representation: Stream[Pure, Byte]
  )

  object LedState {
    case object Off extends LedState(Stream.emits("255\n".getBytes))
    case object On  extends LedState(Stream.emits("0\n".getBytes.toList))
    implicit val eq: Eq[LedState] = Eq.fromUniversalEquals[LedState]
  }

  sealed abstract class Color(val red: LedState, val green: LedState, val blue: LedState) extends Product with Serializable

  object Color {
    import LedState._
    case object Black   extends Color(red = Off, green = Off, blue = Off)
    case object Red     extends Color(red = On, green = Off, blue = Off)
    case object Green   extends Color(red = Off, green = On, blue = Off)
    case object Blue    extends Color(red = Off, green = Off, blue = On)
    case object Cyan    extends Color(red = Off, green = On, blue = On)
    case object Magenta extends Color(red = On, green = Off, blue = On)
    case object Yellow  extends Color(red = On, green = On, blue = Off)
    case object White   extends Color(red = On, green = On, blue = On)

  }

  private def writeGamma[F[_]](
      toSingleLedState: Color => LedState,
      ledPath: Path,
      colors: Stream[F, Color],
      ec: Blocker
  )(implicit
      CS: ContextShift[F],
      S: Sync[F]
  ): Stream[F, Unit] = {
    colors
      .map(toSingleLedState)
      .changes
      .flatMap(_.representation)
      .through(
        writeAll(
          ledPath,
          blocker = ec,
          flags = Seq(StandardOpenOption.WRITE)
        )
      )
  }

}
