name := """video_encoder"""

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "org.jcodec" % "jcodec" % "0.1.9",
  "org.jcodec" % "jcodec-javase" % "0.1.9",
  "org.scalatest" % "scalatest_2.11" % "3.0.0",
  "org.scalaz" %% "scalaz-core" % "7.2.8",
  "org.scalaz" %% "scalaz-concurrent" % "7.2.8"
)
