package io.github.mouslihabdelhakim.vtol.services.navio2.barometer

import cats.syntax.functor._
import cats.syntax.flatMap._

import cats.effect.Sync
import com.pi4j.io.spi.{SpiChannel, SpiDevice, SpiFactory}
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.Implementation._
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.MS5611.CalibrationData

class Implementation[F[_]](
    spiDevice: SpiDevice
)(implicit
    S: Sync[F]
) extends MS5611[F] {

  override def reset(): F[Unit] = S.delay {
    spiDevice.write(Reset: _*)
  }.void

  override def promRead(): F[CalibrationData] =
    for {
      c1 <- readLong(PromReadC1)
      c2 <- readLong(PromReadC2)
      c3 <- readLong(PromReadC3)
      c4 <- readLong(PromReadC4)
      c5 <- readLong(PromReadC5)
      c6 <- readLong(PromReadC6)
    } yield CalibrationData(c1, c2, c3, c4, c5, c6)

  private def readLong(data: Array[Byte]): F[Long] =
    S.delay {
      val response = spiDevice.write(data: _*).drop(1)
      println(response.mkString)
      response(0) * 256L + response(1)
    }

}

object Implementation {

  def apply[F[_]](implicit S: Sync[F]): F[MS5611[F]] = {
    S.delay(
      new Implementation[F](
        SpiFactory.getInstance(
          SpiChannel.CS0, // https://github.com/emlid/Navio2/blob/0eb90b7d0ace9b88f886f3482c4f275b0a34efe8/Python/navio/ms5611.py#L103
          SpiDevice.DEFAULT_SPI_SPEED, // https://github.com/emlid/Navio2/blob/0eb90b7d0ace9b88f886f3482c4f275b0a34efe8/Python/navio/ms5611.py#L45
          SpiDevice.DEFAULT_SPI_MODE // page 6 in https://www.te.com/commerce/DocumentDelivery/DDEController?Action=showdoc&DocId=Data+Sheet%7FMS5611-01BA03%7FB%7Fpdf%7FEnglish%7FENG_DS_MS5611-01BA03_B.pdf%7FCAT-BLPS0036
        )
      )
    )
  }

  private val Reset: Array[Byte]      = Array(0x1e, 0x00, 0x00, 0x00).map(_.toByte)
  private val PromReadC1: Array[Byte] = Array(0xa2, 0x00, 0x00, 0x00).map(_.toByte)
  private val PromReadC2: Array[Byte] = Array(0xa4, 0x00, 0x00, 0x00).map(_.toByte)
  private val PromReadC3: Array[Byte] = Array(0xa6, 0x00, 0x00, 0x00).map(_.toByte)
  private val PromReadC4: Array[Byte] = Array(0xa8, 0x00, 0x00, 0x00).map(_.toByte)
  private val PromReadC5: Array[Byte] = Array(0xaa, 0x00, 0x00, 0x00).map(_.toByte)
  private val PromReadC6: Array[Byte] = Array(0xac, 0x00, 0x00, 0x00).map(_.toByte)

}
