package io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250

import cats.effect.{Sync, Timer}
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.pi4j.io.spi.{SpiChannel, SpiDevice, SpiFactory}
import io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250.SPIBasedImplementation.Register._

import scala.concurrent.duration._

class SPIBasedImplementation[F[_]](
    spiDevice: SpiDevice
)(implicit
    S: Sync[F],
    T: Timer[F]
) extends MPU2950[F] {
  import SPIBasedImplementation._

  override def init(): F[Unit] = for {
    _ <- testMPU2950Connection
    _ <- initMPU2950()
  } yield ()

  private def initMPU2950(): F[Unit] = for {
    _ <- slowWrite(PWR_MGMT_1.DEVICE_RESET)
    _ <- slowWrite(PWR_MGMT_1.CLKSEL)
    _ <- slowWrite(CONFIG.DLPF_CFG)
    _ <- slowWrite(SMPRT_DIV.SMPLRT_DIV)
    _ <- slowWrite(GYRO_CONFIG.FS_SEL)
    _ <- slowWrite(ACCEL_CONFIG.AFS_SEL)
  } yield ()

  private def testMPU2950Connection: F[Unit] = readByte(WHO_AM_I).map {
    case WHO_AM_I.ExpectedValue => ()
    case other                  => throw new Exception(s"expected ${WHO_AM_I.ExpectedValue}, but received ${other}")
  }

  private def slowWrite(parameter: Parameter): F[Unit] =
    for {
      _ <- S.delay {
             spiDevice.write(parameter.address, parameter.value)
           }
      _ <- T.sleep(50.milliseconds)
    } yield ()

  private def readByte(register: Register): F[Byte] = S.delay {
    spiDevice.write(register.READ, 0x00.toByte)(1)
  }

}

object SPIBasedImplementation {

  def apply[F[_]](implicit
      S: Sync[F],
      T: Timer[F]
  ): F[MPU2950[F]] =
    S.delay {
      SpiFactory.getInstance(
        SpiChannel.CS1,
        SPISpeed
      )
    }.map(new SPIBasedImplementation[F](_))

  private val SPISpeed = 20000000 // 20Mhz

  abstract class Register(protected val address: Byte) {
    import Register._
    val READ: Byte = (address | ReadFlag).toByte
  }

  object Register {
    val ReadFlag: Byte = 0x80.toByte

    case class Parameter(address: Byte, value: Byte)

    case object PWR_MGMT_1 extends Register(address = 0x6b.toByte) {
      val DEVICE_RESET = Parameter(address, 0x80.toByte) // resets all internal registers to their default values.
      val CLKSEL       = Parameter(address, 0x01.toByte) // Specifies the X axis gyroscope as the clock source of the device
    }

    case object CONFIG extends Register(address = 0x1a.toByte) {
      val DLPF_CFG = Parameter(address, 0x04.toByte) // enable the Digital Low Pass Filter
    }

    case object SMPRT_DIV extends Register(address = 0x19.toByte) {
      val SMPLRT_DIV = Parameter(address, 0x01.toByte) // keep the sample rate at 1kHz
    }

    case object GYRO_CONFIG extends Register(address = 0x1b.toByte) {
      val FS_SEL = Parameter(address, 0x18.toByte) // set the gyroscope full scale range to ±2000°/s
    }

    case object ACCEL_CONFIG extends Register(address = 0x1c.toByte) {
      val AFS_SEL = Parameter(address, 0x3.toByte) // set the full scale range of the accelerometer to ±16g
    }

    case object WHO_AM_I extends Register(address = 0x75.toByte) {

      val ExpectedValue = 0x71.toByte

    }

  }

}
