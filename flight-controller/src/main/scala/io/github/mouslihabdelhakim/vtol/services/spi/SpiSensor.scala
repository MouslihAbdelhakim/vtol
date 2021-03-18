package io.github.mouslihabdelhakim.vtol.services.spi

import cats.effect.{Sync, Timer}
import com.pi4j.io.spi.SpiFactory
import io.github.mouslihabdelhakim.vtol.services.spi.SpiSensor.Register

import scala.concurrent.duration.FiniteDuration

trait SpiSensor[F[_]] {

  def write(register: Register, data: Byte): F[Unit]

  def writeAndWait(register: Register, data: Byte, duration: FiniteDuration): F[Unit]

  def readByte(register: Register): F[Byte]

  def read16Bit2Complement(register: Register, length: Int): F[Vector[Short]]

}

object SpiSensor {

  def apply[F[_]](
      channel: Channel,
      speed: Speed
  )(implicit
      S: Sync[F],
      T: Timer[F]
  ): F[SpiSensor[F]] =
    S.delay {
      new Pi4JImplementation[F](
        SpiFactory.getInstance(
          Channel.toPi4J(channel),
          speed.value
        )
      )
    }

  case class Register private (writeAddress: Byte, readAddress: Byte)

  object Register {
    private val ReadFlag               = 0x80.toByte
    def apply(address: Byte): Register = Register(writeAddress = address, readAddress = (address | ReadFlag).toByte)
  }

  sealed trait Channel

  object Channel {
    def toPi4J(channel: Channel): com.pi4j.io.spi.SpiChannel = channel match {
      case CS0 => com.pi4j.io.spi.SpiChannel.CS0
      case CS1 => com.pi4j.io.spi.SpiChannel.CS1
    }

    case object CS0 extends Channel
    case object CS1 extends Channel
  }

  sealed abstract class Speed(val value: Int)

  object Speed {
    case object `1MHz`  extends Speed(1000000)
    case object `20MHz` extends Speed(20000000)
  }

}
