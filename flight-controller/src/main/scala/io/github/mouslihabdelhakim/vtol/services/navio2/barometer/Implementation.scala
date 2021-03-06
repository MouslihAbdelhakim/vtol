package io.github.mouslihabdelhakim.vtol.services.navio2.barometer

import scala.concurrent.duration._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect.{Sync, Timer}
import com.pi4j.io.i2c.{I2CBus, I2CDevice, I2CFactory}
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.Implementation._
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.MS5611.CalibrationData

class Implementation[F[_]](
    i2CDevice: I2CDevice
)(implicit
    S: Sync[F],
    T: Timer[F]
) extends MS5611[F] {

  override def reset(): F[Unit] = for {
    _ <- S.delay(i2CDevice.write(Reset))
    _ <- T.sleep(100.milliseconds)
  } yield ()

  override def calibration(): F[CalibrationData] = for {
    c1 <- readTwoByteRegister(PromReadC1)
    c2 <- readTwoByteRegister(PromReadC2)
    c3 <- readTwoByteRegister(PromReadC3)
    c4 <- readTwoByteRegister(PromReadC4)
    c5 <- readTwoByteRegister(PromReadC5)
    c6 <- readTwoByteRegister(PromReadC6)
  } yield CalibrationData(c1, c2, c3, c4, c5, c6)

  private def readTwoByteRegister(register: Int): F[Long] = S.delay {
    val buffer = Array.ofDim[Byte](2)
    i2CDevice.read(register, buffer, 0, 2)
    (buffer(0) & 0xffL) * 256L + (buffer(1) & 0xffL)
  }

  def refreshAndReadPressureConversion(): F[Long] =

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

  private val Reset      = 0x1e.toByte
  private val PromReadC1 = 0xa2
  private val PromReadC2 = 0xa4
  private val PromReadC3 = 0xa6
  private val PromReadC4 = 0xa8
  private val PromReadC5 = 0xaa
  private val PromReadC6 = 0xac

}
