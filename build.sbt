lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "logicalguess",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "zio-intro",
    libraryDependencies ++= Seq(
//      "org.scalaz" %% "scalaz-zio" % "0.3.1",
      "org.scalaz" %% "scalaz-core" % "7.2.28",
      "dev.zio" %% "zio" % "1.0.0-RC12-1",
      "dev.zio" %% "zio-streams" % "1.0.0-RC12-1",
      "org.scalatest" %% "scalatest" % "3.0.8"
    ),
    trapExit := false
  )
