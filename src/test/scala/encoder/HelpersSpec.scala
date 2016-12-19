package encoder

import org.scalatest.FlatSpec
import Helpers._
import models._

import scalaz.-\/

class HelpersSpec extends FlatSpec {
  "listDir" should "return left for non existent directory" in {
    val fakeDir = "definitely_not_a_directory"
    assert(listDir(fakeDir).isLeft)
  }
}
