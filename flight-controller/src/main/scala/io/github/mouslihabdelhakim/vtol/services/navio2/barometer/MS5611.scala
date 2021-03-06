package io.github.mouslihabdelhakim.vtol.services.navio2.barometer

import cats.effect.{Sync, Timer}

trait MS5611[F[_]] {

  def reset(): F[Unit]

}

object MS5611 {

  def apply[F[_]](implicit
      S: Sync[F],
      T: Timer[F]
  ): F[MS5611[F]] = Implementation[F]

}
