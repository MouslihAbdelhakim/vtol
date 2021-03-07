package io.github.mouslihabdelhakim.vtol.services.navio2.barometer

import scala.concurrent.duration._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect.{Sync, Timer}
import com.pi4j.io.i2c.{I2CBus, I2CDevice, I2CFactory}
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.Implementation._
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.MS5611.{BarometricPressure, CalibrationData}

class Implementation[F[_]](
    i2CDevice: I2CDevice
)(implicit
    S: Sync[F],
    T: Timer[F]
) extends MS5611[F] {

  override def reset(): F[Unit] = send(Reset)

  override def calibration(): F[CalibrationData] = for {
    c1 <- readTwoByteRegister(PromReadC1)
    c2 <- readTwoByteRegister(PromReadC2)
    c3 <- readTwoByteRegister(PromReadC3)
    c4 <- readTwoByteRegister(PromReadC4)
    c5 <- readTwoByteRegister(PromReadC5)
    c6 <- readTwoByteRegister(PromReadC6)
  } yield CalibrationData(c1, c2, c3, c4, c5, c6)

  override def digitalPressure(): F[Long] = for {
    _ <- send(ConvertD1OSR4096)
    d1 <- readThreeByteRegister(ADCRead)
  } yield d1

  override def digitalTemperature(): F[Long] = for {
    _ <- send(ConvertD2OSR4096)
    d1 <- readThreeByteRegister(ADCRead)
  } yield d1

  override def barometricPressure(
      calibrationData: CalibrationData
  ): F[BarometricPressure] = for {
    d1 <- digitalPressure()
    d2 <- digitalTemperature()
  } yield {
    import calibrationData._

    // Calculate second order temperature
    val dT = d2 - C5 * 256

    val firstOrderTEMP         = 2000L + dT * C6 / 8388608L
    val lessThan20cCoefficient = if (firstOrderTEMP < 2000) 1 else 0
    val lessThan15cCoefficient = if (firstOrderTEMP < 1500) 1 else 0

    def twoToThePower(a: Int): Long = 2L << (a - 1)

    val `2_23` = twoToThePower(23)
    val `2_16` = twoToThePower(16)
    val `2_7`  = twoToThePower(7)
    val `2_15` = twoToThePower(15)
    val `2_8`  = twoToThePower(8)
    val `2_21` = twoToThePower(21)

    val TEMP = firstOrderTEMP - (lessThan20cCoefficient * (dT * dT) / `2_23`)

    val OFF = C2 * `2_16` + (C4 * dT) / `2_7` - (
      (lessThan20cCoefficient * (5 * ((firstOrderTEMP - 2000) * (firstOrderTEMP - 2000)) / 2)) +
        (lessThan15cCoefficient * (7 * (firstOrderTEMP + 1500) * (firstOrderTEMP + 1500)))
    )

    val SENS = C1 * `2_15` + (C3 * dT) / `2_8` - (
      (lessThan20cCoefficient * (5 * ((firstOrderTEMP - 2000) * (firstOrderTEMP - 2000)) / (2 * 2))) +
        (lessThan15cCoefficient * (11 * ((firstOrderTEMP + 155) * (firstOrderTEMP + 155)) / 2))
    )

    val P = (d1 * SENS / `2_21` - OFF) / `2_15`

    BarometricPressure(
      sensorTemperatureInMilliC = TEMP,
      pressureInMilliBar = P
    )

  }

  private def send(command: Byte): F[Unit] =
    for {
      _ <- S.delay(i2CDevice.write(command))
      _ <- T.sleep(10.milliseconds)
    } yield ()

  private def readTwoByteRegister(register: Int): F[Long] = S.delay {
    val buffer = Array.ofDim[Byte](2)
    i2CDevice.read(register, buffer, 0, 2)
    (buffer(0) & 0xffL) * 256L + (buffer(1) & 0xffL)
  }

  private def readThreeByteRegister(register: Int): F[Long] = S.delay {
    val buffer = Array.ofDim[Byte](3)
    i2CDevice.read(register, buffer, 0, 3)
    ((buffer(0) & 0xff) * 65536) + ((buffer(1) & 0xffL) * 256L) + (buffer(2) & 0xffL)
  }

}

object Implementation {

  def apply[F[_]](implicit
      S: Sync[F],
      T: Timer[F]
  ): F[MS5611[F]] = {
    S.delay(
      new Implementation[F](
        I2CFactory
          .getInstance(
            I2CBus.BUS_1
          )
          .getDevice(
            0x77 // address https://github.com/emlid/Navio2/blob/0eb90b7d0ace9b88f886f3482c4f275b0a34efe8/Python/navio/ms5611.py#L103
          )
      )
    )
  }

  // commands
  private val Reset            = 0x1e.toByte
  private val ConvertD1OSR4096 = 0x48.toByte
  private val ConvertD2OSR4096 = 0x58.toByte

  // registers
  private val PromReadC1 = 0xa2
  private val PromReadC2 = 0xa4
  private val PromReadC3 = 0xa6
  private val PromReadC4 = 0xa8
  private val PromReadC5 = 0xaa
  private val PromReadC6 = 0xac
  private val ADCRead    = 0x00

}
