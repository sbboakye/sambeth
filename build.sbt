ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val scala3Version = "3.6.1"

lazy val sbboakye = "com.sbboakye"

lazy val scalatestVersion = "3.2.19"

lazy val root = (project in file("server"))
  .settings(
    name         := "server",
    scalaVersion := scala3Version,
    organization := sbboakye,
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-core"                 % "2.12.0",
      "org.typelevel"         %% "cats-effect"               % "3.5.7",
      "org.typelevel"         %% "log4cats-slf4j"            % "2.7.0",
      "ch.qos.logback"         % "logback-classic"           % "1.5.12",
      "com.github.pureconfig" %% "pureconfig-generic-scala3" % "0.17.8",
      "org.scalactic"         %% "scalactic"                 % scalatestVersion,
      "org.scalatest"         %% "scalatest"                 % scalatestVersion % "test"
    ),
    Compile / mainClass := Some("com.sbboakye.engine.Application")
  )
