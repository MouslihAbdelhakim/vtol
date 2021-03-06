package io.github.mouslihabdelhakim.vtol.services.navio2.barometer

import scala.concurrent.duration._
import cats.syntax.functor._
import cats.syntax.flatMap._
import cats.effect.{Sync, Timer}
import com.pi4j.io.i2c.{I2CBus, I2CDevice, I2CFactory}
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.Implementation.Reset

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
            0x76 // address
          )
      )
    )
  }

  private val Reset = 0x1e.toByte

}
