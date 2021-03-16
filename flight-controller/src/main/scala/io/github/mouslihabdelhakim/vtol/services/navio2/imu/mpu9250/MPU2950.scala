package io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250

import cats.effect.{Sync, Timer}
import io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250.MPU2950.{CalibrationData, ImuData}
import io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250.MPU2950.ImuData.{Accelerations, AngularRates}

trait MPU2950[F[_]] {

  def init(): F[Unit]

  def read(calibrationData: CalibrationData): F[ImuData]

}

object MPU2950 {

  def spi[F[_]](implicit
      S: Sync[F],
      T: Timer[F]
  ): F[MPU2950[F]] = SPIBasedImplementation[F]

  case class CalibrationData(
      accelerationDivider: Double,
      angularRateDivider: Double
  )

  case class ImuData(
      accelerations: Accelerations,
      angularRates: AngularRates
  )

  object ImuData {
    case class Accelerations(
        xAxisInMeterPerSecondPerSecond: Double,
        yAxisInMeterPerSecondPerSecond: Double,
        zAxisInMeterPerSecondPerSecond: Double
    )

    case class AngularRates(
        pitchAxisInRadPerSecond: Double,
        yawAxisInRadPerSecond: Double,
        rollAxisInRadPerSecond: Double
    )
  }

}
