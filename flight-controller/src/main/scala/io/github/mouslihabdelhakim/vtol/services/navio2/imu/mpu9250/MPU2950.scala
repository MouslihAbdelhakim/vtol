package io.github.mouslihabdelhakim.vtol.services.navio2.imu.mpu9250

import cats.effect.Sync

trait MPU2950[F[_]] {

  def testConnection(): F[Boolean]

}

object MPU2950 {

  def spi[F[_]](implicit
      S: Sync[F]
  ): F[MPU2950[F]] = SPIBasedImplementation[F]

}
