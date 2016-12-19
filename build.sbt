name := """video_encoder"""

version := "1.0"

scalaVersion := "2.12.1"

fork in run := true

javaOptions += "-Xmx2G" // increase memory to store mp4 content buffer

lazy val commonSettings = Seq(

)

lazy val root = (project in file(".")).
  settings(
    name := "video_encoder",
    version := "1.0",
    scalaVersion := "2.12.1",
    test in assembly := {},
    mainClass in Compile := Some("Main")
  )

libraryDependencies ++= Seq(
  "org.jcodec" % "jcodec" % "0.1.9",
  "org.jcodec" % "jcodec-javase" % "0.1.9",
  "org.scalatest" % "scalatest_2.12" % "3.0.1" % "test",
  "org.scalaz" %% "scalaz-core" % "7.2.8",
  "org.scalaz" %% "scalaz-concurrent" % "7.2.8"
)
