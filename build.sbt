lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "logicalguess",
      scalaVersion := "2.12.7",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "zio-intro",
    libraryDependencies ++= Seq(
      "org.scalaz" %% "scalaz-zio" % "0.3.1",
      "dev.zio" %% "zio" % "1.0.0-RC12-1",
      "dev.zio" %% "zio-streams" % "1.0.0-RC12-1",
      "dev.zio" %% "zio-interop-cats" % "2.0.0.0-RC2",
      "org.typelevel" %% "cats-core" % "2.0.0-RC2"
    ),
    trapExit := false
  )
