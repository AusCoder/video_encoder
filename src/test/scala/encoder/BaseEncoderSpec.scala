package encoder

import java.awt.image.BufferedImage
import java.io.File

import models.{Dimensions, Height, Width}
import org.jcodec.common.NIOUtils
import org.scalatest.{BeforeAndAfterAll, FlatSpec}

/**
  * Created by sebastianmueller on 19/12/2016.
  */
trait BaseEncoderSpec extends FlatSpec with BeforeAndAfterAll {
  var encoder: Encoder = null
  val defaultTestDimensions = Dimensions(Width(50), Height(100))

  override def beforeAll(): Unit = {
    encoder = new Encoder(new File("test_output.mp4"), dimensions = defaultTestDimensions, bufferSize = 800*1300*6*5)
  }

  override def afterAll(): Unit = {
    NIOUtils.closeQuietly(encoder.ch)
  }
}
