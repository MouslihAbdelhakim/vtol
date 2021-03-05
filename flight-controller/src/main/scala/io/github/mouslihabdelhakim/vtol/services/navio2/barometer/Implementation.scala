package io.github.mouslihabdelhakim.vtol.services.navio2.barometer

import cats.syntax.functor._

import cats.effect.Sync
import com.pi4j.io.spi.{SpiChannel, SpiDevice, SpiFactory}
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.Implementation.Command._

class Implementation[F[_]](
    spiDevice: SpiDevice
)(implicit
    S: Sync[F]
) extends MS5611[F] {

  override def reset(): F[Unit] = S.delay {
    spiDevice.write(Reset.value: _*)
  }.void

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

  sealed abstract class Command(val value: Array[Byte])

  object Command {

    case object Reset extends Command(Array(0x1e, 0x00, 0x00, 0x00))
  }

}
