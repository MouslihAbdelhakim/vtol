package io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250

import cats.effect.{Sync, Timer}
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250.MPU2950.ImuData._
import io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250.MPU2950.{CalibrationData, ImuData}
import io.github.mouslihabdelhakim.vtol.services.spi.SpiSensor
import io.github.mouslihabdelhakim.vtol.services.spi.SpiSensor.Register

import scala.concurrent.duration._

class SPIBasedImplementation[F[_]](
    sensor: SpiSensor[F]
)(implicit
    S: Sync[F]
) extends MPU2950[F] {
  import SPIBasedImplementation._
  import SPIBasedImplementation.Registers._
  import sensor._

  override def init(): F[Unit] = for {
    _ <- testMPU2950Connection
    _ <- initMPU2950()
  } yield ()

  override def read(calibrationData: CalibrationData): F[ImuData] =
    read16Bit2Complement(ACCEL_XOUT_H, 7).map { buffer =>
      import calibrationData._
      ImuData(
        Accelerations(
          xAxisInMeterPerSecondPerSecond = buffer(0) * GInMeterPerSecondPerSecond / accelerationDivider,
          yAxisInMeterPerSecondPerSecond = buffer(1) * GInMeterPerSecondPerSecond / accelerationDivider,
          zAxisInMeterPerSecondPerSecond = buffer(2) * GInMeterPerSecondPerSecond / accelerationDivider
        ),
        AngularRates(
          pitchAxisInRadPerSecond = ToRadians * buffer(4) / angularRateDivider,
          rollAxisInRadPerSecond = ToRadians * buffer(5) / angularRateDivider,
          yawAxisInRadPerSecond = ToRadians * buffer(6) / angularRateDivider
        )
      )

    }

  private def initMPU2950(): F[Unit] = for {
    _ <- slowWrite(DEVICE_RESET)
    _ <- slowWrite(CLKSEL)
    _ <- slowWrite(DLPF_CFG)
    _ <- slowWrite(SMPLRT_DIV)
    _ <- slowWrite(FS_SEL)
    _ <- slowWrite(AFS_SEL)
  } yield ()

  private def testMPU2950Connection: F[Unit] = readByte(WHO_AM_I).map {
    case WhoAmIExpectedValue => ()
    case other               => throw new Exception(s"expected ${WhoAmIExpectedValue}, but received ${other}")
  }

  private def slowWrite(state: State): F[Unit] =
    writeAndWait(state.register, state.value, DelayAfterWrite)

}

object SPIBasedImplementation {

  def apply[F[_]](implicit
      S: Sync[F],
      T: Timer[F]
  ): F[MPU2950[F]] =
    SpiSensor
      .apply(
        SpiSensor.Channel.CS1,
        SpiSensor.Speed.`20MHz`
      )
      .map(new SPIBasedImplementation[F](_))

  val PreSetCalibration = CalibrationData(
    accelerationDivider = 2048d, // because the full scale range of the accelerometer is ±16g
    angularRateDivider = 16.4d // because the the gyroscope full scale range is ±2000°/s
  )

  private val GInMeterPerSecondPerSecond = 9.80665
  private val ToRadians                  = Math.PI / 180

  private val DelayAfterWrite = 10.millis

  private object Registers {

    case class State(register: SpiSensor.Register, value: Byte)

    val PWR_MGMT_1   = Register(address = 0x6b.toByte)
    // resets all internal registers to their default values.
    val DEVICE_RESET = State(PWR_MGMT_1, 0x80.toByte)
    // Specifies the X axis gyroscope as the clock source of the device
    val CLKSEL       = State(PWR_MGMT_1, 0x01.toByte)

    val CONFIG   = Register(address = 0x1a.toByte)
    val DLPF_CFG = State(CONFIG, 0x04.toByte) // enable the Digital Low Pass Filter

    val SMPRT_DIV  = Register(address = 0x19.toByte)
    val SMPLRT_DIV = State(SMPRT_DIV, 0x00.toByte) // keep the sample rate at 1kHz

    val GYRO_CONFIG = Register(address = 0x1b.toByte)
    val FS_SEL      = State(GYRO_CONFIG, 0x18.toByte) // set the gyroscope full scale range to ±2000°/s

    val ACCEL_CONFIG = Register(address = 0x1c.toByte)
    val AFS_SEL      = State(ACCEL_CONFIG, 0x18.toByte) // set the full scale range of the accelerometer to ±16g

    val WHO_AM_I            = Register(address = 0x75.toByte)
    val WhoAmIExpectedValue = 0x71.toByte

    val ACCEL_XOUT_H = Register(address = 0x3b.toByte)

  }

}
