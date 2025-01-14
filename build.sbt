ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val scala3Version = "3.6.1"

lazy val sbboakye = "com.sbboakye"

scalacOptions ++= Seq("-Xlog-implicits", "-explain")

lazy val scalatestVersion    = "3.2.19"
lazy val circeVersion        = "0.14.10"
lazy val doobieVersion       = "1.0.0-RC6"
lazy val scalaTestContainers = "0.41.5"
lazy val http4sVersion       = "0.23.30"

lazy val root = (project in file("server"))
  .settings(
    name         := "server",
    scalaVersion := scala3Version,
    organization := sbboakye,
    Test / unmanagedResourceDirectories += baseDirectory.value / "extra-resources",
    libraryDependencies ++= Seq(
      "org.typelevel"         %% "cats-core"                       % "2.12.0",
      "org.typelevel"         %% "cats-effect"                     % "3.5.7",
      "org.typelevel"         %% "log4cats-slf4j"                  % "2.7.0",
      "ch.qos.logback"         % "logback-classic"                 % "1.5.16",
      "com.github.pureconfig" %% "pureconfig-generic-scala3"       % "0.17.8",
      "org.tpolecat"          %% "doobie-core"                     % "1.0.0-RC6",
      "com.cronutils"          % "cron-utils"                      % "9.2.1",
      "org.tpolecat"          %% "doobie-hikari"                   % doobieVersion,
      "org.tpolecat"          %% "doobie-postgres"                 % doobieVersion,
      "org.tpolecat"          %% "doobie-postgres-circe"           % doobieVersion,
      "org.http4s"            %% "http4s-ember-client"             % http4sVersion,
      "org.http4s"            %% "http4s-ember-server"             % http4sVersion,
      "org.http4s"            %% "http4s-dsl"                      % http4sVersion,
      "org.http4s"            %% "http4s-circe"                    % http4sVersion,
      "io.circe"              %% "circe-core"                      % circeVersion,
      "io.circe"              %% "circe-generic"                   % circeVersion,
      "io.circe"              %% "circe-literal"                   % circeVersion,
      "io.circe"              %% "circe-parser"                    % circeVersion,
      "org.postgresql"         % "postgresql"                      % "42.7.5",
      "org.scalactic"         %% "scalactic"                       % scalatestVersion,
      "org.tpolecat"          %% "doobie-specs2"                   % doobieVersion       % "test",
      "org.tpolecat"          %% "doobie-scalatest"                % doobieVersion       % "test",
      "org.scalatest"         %% "scalatest"                       % scalatestVersion    % "test",
      "com.dimafeng"          %% "testcontainers-scala-postgresql" % scalaTestContainers % Test,
      "com.dimafeng"          %% "testcontainers-scala-scalatest"  % scalaTestContainers % Test,
      "org.typelevel"         %% "cats-effect-testing-scalatest"   % "1.6.0"             % Test
    ),
    Compile / mainClass := Some("com.sbboakye.engine.Application")
  )
