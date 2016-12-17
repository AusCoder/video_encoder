package encoder

import java.awt.image.BufferedImage
import java.io.File

import scala.concurrent.duration.Duration
import java.nio.ByteBuffer

import scalaz.concurrent.Task
import scalaz.{-\/, \/, \/-}


/**
  * Created by doctor on 12/16/16.
  */

case class Width(value: Int)
case class Height(value: Int)
case class Dimensions(width: Width, height: Height)

sealed trait Video
case class Mp4Video(content: ByteBuffer, dimensions: Dimensions, length: Duration) extends Video

sealed trait SequenceEncoderError
case class InvalidFrameDimensions(dimensions: Dimensions) extends SequenceEncoderError
//case class BadlyFormedImage(data: Array[Byte]) extends SequenceEncoderError
case class BadlyFormedImage(data: File) extends SequenceEncoderError
case class GeneralFailure(error: Throwable) extends SequenceEncoderError


trait SequenceEncoder {
  def encode(frames: Seq[BufferedImage]): Task[SequenceEncoderError \/ Video]
}

object Helpers {
  def sequence[A](xs: List[\/[SequenceEncoderError, A]]) = xs.foldRight(\/-(List(): List[A]): \/[SequenceEncoderError, List[A]])( (dis, disL) =>
    dis.flatMap( bi => disL.map(listBi => bi :: listBi))
  )
}