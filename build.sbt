lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "logicalguess",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "zio-intro",
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-zio" % "0.3.1"
    ),
    trapExit := false
  )
