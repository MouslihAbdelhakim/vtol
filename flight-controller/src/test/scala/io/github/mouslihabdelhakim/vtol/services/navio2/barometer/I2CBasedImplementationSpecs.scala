package io.github.mouslihabdelhakim.vtol.services.navio2.barometer

import cats.effect.IO
import cats.effect.testing.scalatest.AsyncIOSpec
import com.pi4j.io.i2c.I2CDevice
import io.github.mouslihabdelhakim.vtol.services.navio2.barometer.MS5611.CalibrationData
import org.mockito._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec

import java.nio.{ByteBuffer, IntBuffer}

class I2CBasedImplementationSpecs
    extends AsyncWordSpec
    with AsyncIOSpec
    with Matchers
    with IdiomaticMockito
    with ArgumentMatchersSugar {

  "I2CBasedImplementation.reset" should {
    "Send 0x1E to the sensor" in {
      val mockedI2CDevice = mock[I2CDevice]

      new I2CBasedImplementation[IO](mockedI2CDevice).reset().map { _ =>
        mockedI2CDevice.write(0x1e.toByte) was called
      }
    }
  }

  "I2CBasedImplementation.calibration" should {
    "read two bytes from 6 registered and converts them to a long" in {
      val mockedI2CDevice = i2cDeviceWith(
        Map(
          0xa2 -> Array(0x27.toByte, 0x10.toByte), // 10000
          0xa4 -> Array(0x26.toByte, 0x64.toByte), // 9828
          0xa6 -> Array(0x00.toByte, 0x00.toByte), // 0
          0xa8 -> Array(0x7f.toByte, 0xff.toByte), // 32767
          0xaa -> Array(0x00.toByte, 0x07.toByte), // 7
          0xac -> Array(0x00.toByte, 0x46.toByte) // 70
        )
      )

      new I2CBasedImplementation[IO](mockedI2CDevice).calibration().asserting {
        _ shouldBe CalibrationData(10000, 9828, 0, 32767, 7, 70)
      }
    }

  }

  "I2CBasedImplementation.digitalPressure" should {

    "send 0x48 to the sensor" in {
      val mockedI2CDevice = mock[I2CDevice]

      new I2CBasedImplementation[IO](mockedI2CDevice).digitalPressure().map { pressure =>
        mockedI2CDevice.write(0x48.toByte) was called
        pressure shouldBe 0
      }
    }

    "read three bytes from 0x00 and convert it to long" in {
      val mockedI2CDevice = i2cDeviceWith(
        Map(
          0x00 -> Array(0x95.toByte, 0xf7.toByte, 0xba.toByte) // 9828282
        )
      )

      new I2CBasedImplementation[IO](mockedI2CDevice).digitalPressure().asserting {
        _ shouldBe 9828282
      }
    }

  }

  "I2CBasedImplementation.digitalTemperature" should {

    "send 0x58 to the sensor" in {
      val mockedI2CDevice = mock[I2CDevice]

      new I2CBasedImplementation[IO](mockedI2CDevice).digitalTemperature().map { pressure =>
        mockedI2CDevice.write(0x58.toByte) was called
        pressure shouldBe 0
      }
    }

    "read three bytes from 0x00 and convert it to long" in {
      val mockedI2CDevice = i2cDeviceWith(
        Map(
          0x00 -> Array(0x95.toByte, 0xf7.toByte, 0xba.toByte) // 9828282
        )
      )

      new I2CBasedImplementation[IO](mockedI2CDevice).digitalTemperature().asserting {
        _ shouldBe 9828282
      }
    }

  }

  def i2cDeviceWith(buffers: Map[Int, Array[Byte]]) = new I2CDevice {

    override def read(address: Int, buffer: Array[Byte], offset: Int, size: Int): Int = {

      buffers
        .get(address)
        .map {
          _.slice(offset, size + offset)
        }
        .toList
        .flatten
        .zipWithIndex
        .foreach { case (x, i) =>
          buffer.update(i, x)
        }

      buffer.size
    }

    override def write(b: Byte): Unit = ()

    override def write(address: Int, buffer: Array[Byte], offset: Int, size: Int): Unit = ???

    override def getAddress: Int = ???

    override def write(buffer: Array[Byte], offset: Int, size: Int): Unit = ???

    override def write(buffer: Array[Byte]): Unit = ???

    override def write(address: Int, b: Byte): Unit = ???

    override def write(address: Int, buffer: Array[Byte]): Unit = ???

    override def read(): Int = ???

    override def read(buffer: Array[Byte], offset: Int, size: Int): Int = ???

    override def read(address: Int): Int = ???

    override def ioctl(command: Long, value: Int): Unit = ???

    override def ioctl(command: Long, data: ByteBuffer, offsets: IntBuffer): Unit = ???

    override def read(
        writeBuffer: Array[Byte],
        writeOffset: Int,
        writeSize: Int,
        readBuffer: Array[Byte],
        readOffset: Int,
        readSize: Int
    ): Int = ???
  }

}
