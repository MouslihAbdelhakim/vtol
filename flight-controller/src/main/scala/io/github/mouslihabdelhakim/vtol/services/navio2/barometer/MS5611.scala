package io.github.mouslihabdelhakim.vtol.services.navio2.barometer

import cats.effect.{Sync, Timer}
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.MS5611.CalibrationData

trait MS5611[F[_]] {

  def reset(): F[Unit]

  def calibration(): F[CalibrationData]

}

object MS5611 {

  def apply[F[_]](implicit
      S: Sync[F],
      T: Timer[F]
  ): F[MS5611[F]] = Implementation[F]

  case class CalibrationData(
      C1: Long, // Pressure sensitivity
      C2: Long, // Pressure offset
      C3: Long, // Temperature coefficient
      C4: Long, // Temperature coefficient
      C5: Long, // Reference temperature
      C6: Long // Temperature coefficient
  )

}
