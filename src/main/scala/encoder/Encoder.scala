package encoder

import java.awt.image.BufferedImage
import java.io.File
import java.nio.ByteBuffer

import org.jcodec.codecs.h264.{H264Encoder, H264Utils}
import org.jcodec.common.model.{ColorSpace, Picture}
import org.jcodec.common.{FileChannelWrapper, NIOUtils}
import org.jcodec.containers.mp4.{Brand, MP4Packet, TrackType}
import org.jcodec.containers.mp4.muxer.{FramesMP4MuxerTrack, MP4Muxer}
import org.jcodec.scale.{ColorUtil, Transform}
import org.jcodec.scale.AWTUtil

import scalaz.{-\/, \/, \/-}
import scalaz.concurrent.Task
import scala.concurrent.duration._
import scalaz.syntax.traverse._

import scalaz._, Scalaz._

/**
  * Created by doctor on 12/16/16.
  */
class Encoder(out: File) extends SequenceEncoder {
  var dimensions = Dimensions(Width(-1), Height(-1))
  val timescale = 25

  val ch: FileChannelWrapper = NIOUtils.writableFileChannel(out)
  val muxer: MP4Muxer = new MP4Muxer(ch, Brand.MP4)
  val outTrack: FramesMP4MuxerTrack = muxer.addTrack(TrackType.VIDEO, timescale)
  val outBuffer: ByteBuffer = ByteBuffer.allocate(800*1300*6) // buffer to hold mp4 video
  val encoder: H264Encoder = new H264Encoder()
  val transform: Transform = ColorUtil.getTransform(ColorSpace.RGB, encoder.getSupportedColorSpaces().head )

  val spsList = new java.util.ArrayList[ByteBuffer]()
  val ppsList = new java.util.ArrayList[ByteBuffer]()

  var frameNo = 0


  def encodeImage_test(img: BufferedImage) = {
    val toEncode = Picture.create(img.getWidth(), img.getHeight(), encoder.getSupportedColorSpaces().head)
    transform.transform(AWTUtil.fromBufferedImage(img), toEncode)

    outBuffer.clear()
    val result: ByteBuffer = encoder.encodeFrame(toEncode, outBuffer)

    spsList.clear()
    ppsList.clear()
    H264Utils.wipePS(result, spsList, ppsList)
    H264Utils.encodeMOVPacket(result)

    //outTrack.addFrame(new MP4Packet(result, frameNo, 25, 1*25, frameNo, true, null, frameNo, 0))
    for (i <- 1 to timescale) {
      outTrack.addFrame(new MP4Packet(result,
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
//    outTrack.addFrame(new MP4Packet(result,
//      frameNo,
//      timescale,
//      1,
//      frameNo,
//      true,
//      null,
//      frameNo,
//      0)
//    )

//    outTrack.addFrame(new MP4Packet(result,
//      frameNo,
//      1,
//      1,
//      frameNo,
//      true,
//      null,
//      frameNo,
//      0)
//    )
//    frameNo += 1
  }

  def encodeImage(img: BufferedImage): Task[SequenceEncoderError \/ ByteBuffer] = Task {

    val toEncode = Picture.create(img.getWidth(), img.getHeight(), encoder.getSupportedColorSpaces().head)
    transform.transform(AWTUtil.fromBufferedImage(img), toEncode)
    val result: ByteBuffer = encoder.encodeFrame(toEncode, outBuffer) // make encoder generic?

    spsList.clear()
    ppsList.clear()
    H264Utils.wipePS(result, spsList, ppsList)
    H264Utils.encodeMOVPacket(result)

    \/-(result)
  }

  def finish(): Unit = {
    outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList, 1))

    muxer.writeHeader()
    NIOUtils.closeQuietly(ch)
  }

  private def checkDimensions(img: BufferedImage): \/[SequenceEncoderError, BufferedImage] = {
    val d = Dimensions(width = Width(img.getWidth), height = Height(img.getHeight))
    if (d == dimensions) \/-(img)
    else -\/(InvalidFrameDimensions(d))
  }

  override def encode(frames: Seq[BufferedImage]) = Task {
    val w = frames.head.getWidth() // handle case when no head
    val h = frames.head.getHeight()
    dimensions = Dimensions(Width(w), Height(h))

    val x = frames
      .map(checkDimensions(_))
//      .toList
//      .sequenceU

    for (imgDis <- x) yield imgDis match {
      case -\/(err) => Unit
      case \/-(img) =>
        val toEncode = Picture.create(img.getWidth(), img.getHeight(), encoder.getSupportedColorSpaces().head)
        transform.transform(AWTUtil.fromBufferedImage(img), toEncode)
        outBuffer.clear()
        val result: ByteBuffer = encoder.encodeFrame(toEncode, outBuffer) // how to get a buffer big enough? or do we forget about the buffer?

        spsList.clear()
        ppsList.clear()
        H264Utils.wipePS(result, spsList, ppsList)
        H264Utils.encodeMOVPacket(result)

        // this writes lots of frames per second...
        for (i <- 1 to timescale) {
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
    }
    -\/(InvalidFrameDimensions(dimensions))


//    x match {
//      case -\/(err) => -\/(err)
//      case \/-(imgs) =>
//        imgs.foreach {
//          img =>
//            val toEncode = Picture.create(img.getWidth(), img.getHeight(), encoder.getSupportedColorSpaces().head)
//            transform.transform(AWTUtil.fromBufferedImage(img), toEncode)
//            outBuffer.clear()
//            val result: ByteBuffer = encoder.encodeFrame(toEncode, outBuffer) // how to get a buffer big enough? or do we forget about the buffer?
//
//            spsList.clear()
//            ppsList.clear()
//            H264Utils.wipePS(result, spsList, ppsList)
//            H264Utils.encodeMOVPacket(result)
//
//            // this writes lots of frames per second...
//            for (i <- 1 to timescale) {
//              outTrack.addFrame(new MP4Packet(
//                result,
//                frameNo,
//                timescale,
//                1,
//                frameNo,
//                true,
//                null,
//                frameNo,
//                0)
//              )
//              frameNo += 1
//            }
//      }
//      \/-(Mp4Video(outBuffer, dimensions, 211 seconds))
//    }

  }
}
