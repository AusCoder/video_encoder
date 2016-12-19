import encoder._
import Helpers._
import models.GeneralFailure

import scalaz.{-\/, \/-}

/**
  * The main application. It assembles images from a directory into an mp4 video.
  * The directory name is passed as a command line parameter.
  */
object Main extends App {

  args.headOption match {
    case None =>
      println("please specify the directory containing the images")
    case Some(dirName) =>
      val bufImgs = for {
        imgNames <- listDir(dirName)
        imgs <- sequence(imgNames.map(readImage(_)).toList)
      } yield imgs

      bufImgs.flatMap(imgs => Encoder.encodeImages("output.mp4",imgs)) match {
        case -\/(err) =>
          println("an error occured: ")
          err match {
            case GeneralFailure(e) =>
              println(err)
              e.printStackTrace()
            case _ =>
              println(err)
          }
        case \/-(_) => println("success")
      }
  }
}
