package io.github.mouslihabdelhakim.vtol.services.led

import fs2.{INothing, Stream}
import io.github.mouslihabdelhakim.vtol.services.led.Navio2RGBLed.Color

trait Navio2RGBLed[F[_]] {

  def write(colors: Stream[F, Color]): Stream[F, INothing]

}

object Navio2RGBLed {

  sealed abstract class LedState(
      val representation: List[Byte]
  )

  object LedState {
    case object Off extends LedState("255\n".getBytes.toList)
    case object On  extends LedState("0\n".getBytes.toList)
  }

  sealed abstract class Color(val red: LedState, val green: LedState, val blue: LedState)

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

}
