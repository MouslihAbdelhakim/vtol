package io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250

import cats.Show
import cats.effect.{Sync, Timer}
import cats.syntax.show._
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

    object Accelerations {
      implicit val show: Show[Accelerations] = Show.show { acc =>
        val x = s"x: ${acc.xAxisInMeterPerSecondPerSecond}%2.2f m/s²"
        val y = s"y: ${acc.yAxisInMeterPerSecondPerSecond}%2.2f m/s²"
        val z = s"z: ${acc.zAxisInMeterPerSecondPerSecond}%2.2f m/s²"
        s"Accelerations(${x}, ${y}, ${z})"
      }
    }

    case class AngularRates(
        pitchAxisInRadPerSecond: Double,
        yawAxisInRadPerSecond: Double,
        rollAxisInRadPerSecond: Double
    )

    object AngularRates {
      implicit val show: Show[AngularRates] = Show.show { ang =>
        val pitch = s"x: ${ang.pitchAxisInRadPerSecond}%2.2f rad/s"
        val yaw   = s"y: ${ang.yawAxisInRadPerSecond}%2.2f rad/s"
        val roll  = s"z: ${ang.rollAxisInRadPerSecond}%2.2f rad/s"
        s"Accelerations(pitch: ${pitch}, yaw: ${yaw}, roll: ${roll})"
      }
    }

    implicit val show: Show[ImuData] = Show.show { data =>
      s"ImuData(${data.accelerations.show}, ${data.angularRates.show})"
    }
  }

}
