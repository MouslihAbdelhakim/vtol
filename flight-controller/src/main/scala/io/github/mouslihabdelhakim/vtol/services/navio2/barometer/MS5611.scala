package io.github.mouslihabdelhakim.vtol.services.navio2.barometer

import fs2.Stream
import cats.effect.{Sync, Timer}
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.MS5611.{BarometricPressure, CalibrationData}

import scala.concurrent.duration.FiniteDuration

trait MS5611[F[_]] {

  def reset(): F[Unit]

  def calibration(): F[CalibrationData]

  def digitalPressure(): F[Long]

  def digitalTemperature(): F[Long]

  def barometricPressure(calibrationData: CalibrationData): F[BarometricPressure]
}

object MS5611 {

  def stream[F[_]](
      sampleEvery: FiniteDuration
  )(implicit
      S: Sync[F],
      T: Timer[F]
  ): Stream[F, BarometricPressure] = for {
    impl <- Stream.eval(i2c[F])
    calibrationData <- Stream.eval(impl.calibration())
    barometricPressure <- Stream
                            .repeatEval(impl.barometricPressure(calibrationData))
                            .metered(sampleEvery)
  } yield barometricPressure

  def i2c[F[_]](implicit
      S: Sync[F],
      T: Timer[F]
  ): F[MS5611[F]] = I2CBasedImplementation[F]

  case class CalibrationData(
      C1: Long, // Pressure sensitivity
      C2: Long, // Pressure offset
      C3: Long, // Temperature coefficient
      C4: Long, // Temperature coefficient
      C5: Long, // Reference temperature
      C6: Long // Temperature coefficient
  )

  case class BarometricPressure(
      sensorTemperatureInMilliC: Long,
      pressureInMilliBar: Long
  )

}
