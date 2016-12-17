import java.awt.image.BufferedImage
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.Files
import javax.imageio.ImageIO
import java.io.File

import encoder._

import scalaz.{-\/, Scalaz, \/, \/-, std}
import scalaz.syntax.traverse._
//import scalaz._
import Scalaz._

/**
  * Created by doctor on 12/16/16.
  */
object Hi {
  def main(args: Array[String]) = {

    val t = new Encoder(new File("test.mp4"))
    //t.encode(listImages("images") map ( file => ImageIO.read(file))).unsafePerformSync

    val x = listImages("images").map(readImage(_)).toList
    Helpers.sequence[BufferedImage](x).flatMap(imgs => t.encode(imgs).unsafePerformSync)
    t.finish()
    println("success")

  }

  def listImages(dir: String): Seq[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.filter(_.isFile).toSeq.sorted
    }
    else {
      Seq[File]()
    }
  }

  def readImage(file: File): SequenceEncoderError \/ BufferedImage = {
    try {
      \/-(ImageIO.read(file))
    }
    catch {
      case e: IOException =>
        println("hit bad file")
        -\/(BadlyFormedImage(file)) // need to put a byte array here.
    }
  }
}
