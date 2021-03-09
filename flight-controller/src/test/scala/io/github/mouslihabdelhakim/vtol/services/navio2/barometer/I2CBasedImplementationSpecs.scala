package io.github.mouslihabdelhakim.vtol.services.navio2.barometer

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.pi4j.io.i2c.I2CDevice
import org.mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

class I2CBasedImplementationSpecs
    extends AsyncWordSpec
    with AsyncIOSpec
    with Matchers
    with IdiomaticMockito
    with ArgumentMatchersSugar {

  "I2CBasedImplementation.reset" should {
    "Send 0x1E to the sensor" in {
      val i2CDevice = mock[I2CDevice]

      new I2CBasedImplementation[IO](i2CDevice).reset().map { _ =>
        i2CDevice.write(0x1e.toByte) was called
      }
    }
  }

}
