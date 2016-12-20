package encoder

import java.awt.image.BufferedImage
import java.io.{ByteArrayOutputStream, File}
import java.nio.ByteBuffer
import javax.imageio.ImageIO

import org.jcodec.codecs.h264.{H264Encoder, H264Utils}
import org.jcodec.common.model.{ColorSpace, Picture}
import org.jcodec.common.{FileChannelWrapper, NIOUtils}
import org.jcodec.containers.mp4.{Brand, MP4Packet, TrackType}
import org.jcodec.containers.mp4.muxer.{FramesMP4MuxerTrack, MP4Muxer}
import org.jcodec.scale.{ColorUtil, Transform}
import org.jcodec.scale.AWTUtil

import scalaz.{-\/, \/, \/-, _}
import scalaz.concurrent.Task
import scala.concurrent.duration._
import models._
import Helpers._

/** main encoder class
  *
  * encodes images at a rate of 1 fps
  * maintains internal state with buffer and encoder dimensions
  *
  * @param out file name
  */
class Encoder(out: File, dimensions: Dimensions, bufferSize: Int = 800*1300*6*130) extends SequenceEncoder {

  /* timescale for the mp4 video, 1 means 1fps */
  val timescale = 1

  /* buffer to hold output video */
  val outBuffer: ByteBuffer = ByteBuffer.allocate(bufferSize)

  /* internal jcodec objects */
  val ch: FileChannelWrapper = NIOUtils.writableFileChannel(out)
  val muxer: MP4Muxer = new MP4Muxer(ch, Brand.MP4)
  val outTrack: FramesMP4MuxerTrack = muxer.addTrack(TrackType.VIDEO, timescale)
  val encoder: H264Encoder = new H264Encoder()
  val transform: Transform = ColorUtil.getTransform(ColorSpace.RGB, encoder.getSupportedColorSpaces().head )
  val spsList = new java.util.ArrayList[ByteBuffer]()
  val ppsList = new java.util.ArrayList[ByteBuffer]()

  /* keeps track of the current frame number */
  var frameNo = 0

  /** encodes a buffered image into a bytebuffer and adds to the encoders internal video buffer
    *
    * @param img
    * @return ByteBuffer containing encoded image
    */
  def encodeFrame(img: BufferedImage): ByteBuffer = {

    val toEncode = Picture.create(img.getWidth(), img.getHeight(), encoder.getSupportedColorSpaces().head)
    transform.transform(AWTUtil.fromBufferedImage(img), toEncode)
    val result: ByteBuffer = encoder.encodeFrame(toEncode, outBuffer)

    spsList.clear()
    ppsList.clear()
    H264Utils.wipePS(result, spsList, ppsList)
    H264Utils.encodeMOVPacket(result)

    result
  }

  /**
    * checks that the dimensions of image match the encoders dimensions
    */
  def validateImage(img: BufferedImage): \/[SequenceEncoderError, BufferedImage] = {
    try {
      val d = Dimensions(width = Width(img.getWidth), height = Height(img.getHeight))
      if (d == dimensions) \/-(img)
      else -\/(InvalidFrameDimensions(d))
    }
    catch {
      case e: Exception => // catch any
        -\/(BadlyFormedImage(img))
    }
  }

  /** encodes a sequence of frames and writes them to a file
    *
    * @param frames
    * @return scalaz task
    */
  override def encode(frames: Seq[BufferedImage]): Task[\/[SequenceEncoderError, Mp4Video]] = Task {

    val validatedImages: List[\/[SequenceEncoderError, BufferedImage]] = frames
      .map(validateImage(_))
      .toList

    sequence[BufferedImage](validatedImages) match { // use flatmap...
      case -\/(err) => -\/(err)
      case \/-(imgs) =>
        imgs.foreach {
          img =>
            val result = encodeFrame(img)
            outTrack.addFrame(new MP4Packet(
              result,
              frameNo,
              timescale,
              1,
              frameNo,
              true,
              null,
              frameNo,
              0)
            )
            frameNo += 1
        }
        \/-(Mp4Video(outBuffer, dimensions, imgs.length seconds))
    }
  }

  /**
    * Completes the encoding process
    * closes the output file
    */
  def writeHeaders(): Unit = {
    outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList, 0))
    muxer.writeHeader()
  }

  /**
    * closes the output file
    */
  def closeOutputFile(): Unit = {
    NIOUtils.closeQuietly(ch)
  }
}

/**
  * Companion object that encodes a sequence of images to a file and returns an Mp4Video object
  *
  * This is not a pure function! It creates an mp4 video file.
  */
object Encoder {
  def encodeImages(outFilename: String, imgs: Seq[BufferedImage]): \/[SequenceEncoderError, Mp4Video] = {
    try {
      val w = imgs.head.getWidth()
      val h = imgs.head.getHeight()
      val dims = Dimensions(Width(w), Height(h))

      val t = new Encoder(new File(outFilename), dims)
      t.encode(imgs).unsafePerformSync match {
        case -\/(err) =>
          t.closeOutputFile()
          -\/(err)
        case \/-(v) =>
          t.writeHeaders()
          t.closeOutputFile()
          \/-(v)
      }
    }
    catch {
      case e: Exception => -\/(GeneralFailure(e))
    }
  }
}
