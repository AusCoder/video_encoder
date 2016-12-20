package encoder

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import models._

import scalaz.-\/

/**
  * Created by sebastianmueller on 19/12/2016.
  */
class EncoderSpec extends BaseEncoderSpec {

  "validateImage" should "return left for bad dimensions" in {
    val w = defaultTestDimensions.width.value + 10
    val h = defaultTestDimensions.height.value
    val img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
    assert( encoder.validateImage(img) match {
      case -\/(InvalidFrameDimensions(_)) => true
      case _ => false
    })
  }

  "validateImage" should "return left for bad input image" in {
    val img: BufferedImage = ImageIO.read(new File("build.sbt"))
    assert (encoder.validateImage(img) match {
      case -\/(BadlyFormedImage(_)) => true
      case _ => false
    })
  }

  "encode" should "encode sample images" in {
    val img1 = new BufferedImage(defaultTestDimensions.width.value, defaultTestDimensions.height.value, BufferedImage.TYPE_INT_RGB)
    val img2 = new BufferedImage(defaultTestDimensions.width.value, defaultTestDimensions.height.value, BufferedImage.TYPE_INT_RGB)
    assert(encoder.encode(Seq(img1, img2)).unsafePerformSync.isRight)
  }
}
