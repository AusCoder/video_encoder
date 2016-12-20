package encoder

import java.awt.image.BufferedImage
import java.io.{File, IOException}
import javax.imageio.ImageIO

import scalaz.{-\/, \/, \/-}
import models._


object Helpers {
  /** inverts a list of disjunctions to a disjunction of list
    *
    * this is equivalent to scalaz's sequenceU method
    * @param xs
    * @return disjunction of error and list
    */
  def sequence[A](xs: List[\/[SequenceEncoderError, A]]) = xs.foldRight(\/-(List(): List[A]): \/[SequenceEncoderError, List[A]])( (dis, disL) =>
    dis.flatMap( bi => disL.map(listBi => bi :: listBi))
  )

  /** Lists contents of a directory
    *
    * @param dir
    * @return disjunction of error and file list
    */
  def listDir(dir: String): \/[SequenceEncoderError, Seq[File]] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      \/-(d.listFiles.filter(_.isFile).toSeq.sorted)
    }
    else {
      -\/(BadImageDirectory(dir))
    }
  }

  /** reads an image file into a buffered image
    *
    * @param file
    * @return disjunction of error and buffered image
    */
  def readImage(file: File): SequenceEncoderError \/ BufferedImage = {
    try {
      \/-(ImageIO.read(file))
    }
    catch {
      case e: IOException =>
        -\/(GeneralFailure(e))
    }
  }
}