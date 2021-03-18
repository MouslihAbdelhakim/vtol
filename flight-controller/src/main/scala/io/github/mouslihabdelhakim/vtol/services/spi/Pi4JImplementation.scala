package io.github.mouslihabdelhakim.vtol.services.spi

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.effect.{Sync, Timer}
import com.pi4j.io.spi.SpiDevice
import io.github.mouslihabdelhakim.vtol.services.spi.Pi4JImplementation._

import scala.concurrent.duration.FiniteDuration

class Pi4JImplementation[F[_]](spiDevice: SpiDevice)(implicit
    S: Sync[F],
    T: Timer[F]
) extends SpiSensor[F] {

  override def write(
      register: SpiSensor.Register,
      data: Byte
  ): F[Unit] = S.delay {
    write(register.writeAddress, data)
  }.void

  override def writeAndWait(
      register: SpiSensor.Register,
      data: Byte,
      duration: FiniteDuration
  ): F[Unit] = write(register, data).flatTap(_ => T.sleep(duration))

  override def readByte(register: SpiSensor.Register): F[Byte] = S.delay {
    write(register.readAddress, FillerByte).head
  }

  override def read16Bit2Complement(register: SpiSensor.Register, length: Int): F[Vector[Short]] = S.delay {
    to16Bit2sComplement(
      write(
        register.readAddress :: List.fill((length * 2))(FillerByte): _*
      )
    )
  }

  private def write(bytes: Byte*): Array[Byte] = spiDevice.write(bytes: _*).drop(1)

}

object Pi4JImplementation {

  private val FillerByte = 0x00.toByte

  private def to16Bit2sComplement(high: Byte, low: Byte): Short =
    (((((high & 0xff) << 8) | (low & 0xff)) << (32 - 14)) >> (32 - 14)).toShort

  private def to16Bit2sComplement(bytes: Array[Byte]): Vector[Short] =
    bytes
      .sliding(size = 2, step = 2)
      .foldLeft(List.empty[Short]) { (acc, bytes) =>
        to16Bit2sComplement(bytes(0), bytes(1)) :: acc
      }
      .reverse
      .toVector

}
