package io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250

import cats.Show
import cats.effect.{Sync, Timer}
import cats.syntax.show._
import fs2.Stream
import io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250.MPU2950.ImuData.{Accelerations, AngularRates}
import io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250.MPU2950.{CalibrationData, ImuData}

import scala.concurrent.duration.FiniteDuration

trait MPU2950[F[_]] {

  def init(): F[Unit]

  def read(calibrationData: CalibrationData): F[ImuData]

}

object MPU2950 {

  def spi[F[_]](implicit
      S: Sync[F],
      T: Timer[F]
  ): F[MPU2950[F]] = SPIBasedImplementation[F]

  def stream[F[_]](
      sampleEvery: FiniteDuration
  )(implicit
      S: Sync[F],
      T: Timer[F]
  ): Stream[F, ImuData] = for {
    imu <- Stream.eval(spi[F])
    _ <- Stream.eval(imu.init())
    data <- Stream
              .repeatEval(
                imu.read(SPIBasedImplementation.preSetCalibration)
              )
              .metered(sampleEvery)
  } yield data

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
        val x = f"x: ${acc.xAxisInMeterPerSecondPerSecond}%2.2f m/s²"
        val y = f"y: ${acc.yAxisInMeterPerSecondPerSecond}%2.2f m/s²"
        val z = f"z: ${acc.zAxisInMeterPerSecondPerSecond}%2.2f m/s²"
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
        val pitch = f"pitch: ${ang.pitchAxisInRadPerSecond}%2.2f rad/s"
        val yaw   = f"yaw: ${ang.yawAxisInRadPerSecond}%2.2f rad/s"
        val roll  = f"roll: ${ang.rollAxisInRadPerSecond}%2.2f rad/s"
        s"Accelerations(${pitch}, ${yaw}, ${roll})"
      }
    }

    implicit val show: Show[ImuData] = Show.show { data =>
      s"ImuData(${data.accelerations.show}, ${data.angularRates.show})"
    }
  }

}
