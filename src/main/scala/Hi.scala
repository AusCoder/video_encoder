import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.file.Files
import javax.imageio.ImageIO
import java.io.File
import org.jcodec.api.awt.SequenceEncoder

import encoder._
/**
  * Created by doctor on 12/16/16.
  */
object Hi {
  def main(args: Array[String]) = {

//    val enc = new SequenceEncoder(new File("test.mp4"))
//    enc.getEncoder().encodeImage


    val t = new Encoder(new File("test.mp4"))
//    val t = new SequenceEncoder(new File("test.mp4"))

//    listImages("images").foreach {
//      file =>
//        println(file)
//        val img = ImageIO.read(file)
//        t.encodeImage_test(img)
//    }

//    val byteBufs = listImages("images").map {
//      file =>
//        val img = ImageIO.read(file)
//        t.encodeImage(img)
//    }
//    byteBufs.foreach {
//      task =>
//        task.
//    }

    t.encode(listImages("images") map ( file => ImageIO.read(file))).unsafePerformSync

    t.finish()

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
}
