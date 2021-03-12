package io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250

import cats.syntax.functor._
import cats.effect.Sync
import com.pi4j.io.spi.{SpiChannel, SpiDevice, SpiFactory}

class SPIBasedImplementation[F[_]](
    spiDevice: SpiDevice
)(implicit
    S: Sync[F]
) extends MPU2950[F] {
  import SPIBasedImplementation._

  override def testConnection(): F[Boolean] =
    S.delay {
      spiDevice.write(ReadWhoAmI, Filler)(1) == ExpectedWhoAmIResponse
    }

}

object SPIBasedImplementation {

  def apply[F[_]](implicit
      S: Sync[F]
  ): F[MPU2950[F]] =
    S.delay {
      SpiFactory.getInstance(
        SpiChannel.CS1,
        SPISpeed
      )
    }.map(new SPIBasedImplementation[F](_))

  private val SPISpeed = 20000000 // 20Mhz

  // Reading
  private val ReadFlag: Byte = 0x80.toByte
  private val Filler: Byte   = 0x00

  // registers
  private val ReadWhoAmI: Byte = (0x75 | ReadFlag).toByte

  // expected response
  private val ExpectedWhoAmIResponse = 0x71.toByte

}
