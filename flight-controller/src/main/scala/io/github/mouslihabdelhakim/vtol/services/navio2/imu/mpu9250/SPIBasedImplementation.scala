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
    readASingleByte(ReadWhoAmI).map(_ == 0x71.toByte)

  private def readASingleByte(register: Byte): F[Byte] = S.delay {
    spiDevice.write(register, Filler)(1)
  }

}

object SPIBasedImplementation {

  def apply[F[_]](implicit
      S: Sync[F]
  ): F[MPU2950[F]] =
    S.delay {
      SpiFactory.getInstance(
        SpiChannel.CS1,
        SpiDevice.DEFAULT_SPI_SPEED
      )
    }.map(new SPIBasedImplementation[F](_))

  private val Filler: Byte     = 0x00
  private val ReadFlag: Byte   = 0x80.toByte
  private val ReadWhoAmI: Byte = (0x75 | ReadFlag).toByte

}
