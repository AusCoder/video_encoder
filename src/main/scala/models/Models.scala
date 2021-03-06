package models

import java.awt.image.BufferedImage
import java.nio.ByteBuffer

import scala.concurrent.duration.Duration
import scalaz.\/
import scalaz.concurrent.Task


case class Width(value: Int)
case class Height(value: Int)
case class Dimensions(width: Width, height: Height)


sealed trait Video
case class Mp4Video(content: ByteBuffer, dimensions: Dimensions, length: Duration) extends Video


sealed trait SequenceEncoderError
case class InvalidFrameDimensions(dimensions: Dimensions) extends SequenceEncoderError
case class BadlyFormedImage(data: BufferedImage) extends SequenceEncoderError
case class GeneralFailure(error: Throwable) extends SequenceEncoderError
case class BadImageDirectory(directory: String) extends SequenceEncoderError


trait SequenceEncoder {
  def encode(frames: Seq[BufferedImage]): Task[SequenceEncoderError \/ Video]
}