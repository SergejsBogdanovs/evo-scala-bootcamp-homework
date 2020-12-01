name := "evo-scala-bootcamp-homework"

version := "0.1"

scalaVersion := "2.13.3"

// From https://tpolecat.github.io/2017/04/25/scalac-flags.html
scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-Ymacro-annotations",
  "-language:higherKinds",
  "-Ywarn-unused:params",
  "-Wunused:params",
  "-Ywarn-dead-code",
  "-Wdead-code",
  "-Ywarn-value-discard",
  "-Wvalue-discard"
)

val http4sVersion = "0.21.7"
val circeVersion = "0.13.0"
val circeConfigVersion = "0.7.0"
val playVersion = "2.8.2"
val doobieVersion = "0.9.0"
val catsVersion = "2.2.0"
val catsTaglessVersion = "0.11"
val epimetheusVersion = "0.4.2"
val catsScalacheckVersion = "0.2.0"
val fs2Version = "2.4.4"

val akkaVersion = "2.6.9"
val akkaHttpVersion = "10.1.11"
val akkaHttpCirceVersion = "1.31.0"

val log4CatsVersion = "1.1.1"

val scalaTestVersion = "3.1.0.0-RC2"
val h2Version = "1.4.200"

val tsecV = "0.2.0-M2"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats-core" % catsVersion,

  "org.http4s" %% "http4s-dsl" % http4sVersion,
  "org.http4s" %% "http4s-blaze-server" % http4sVersion,
  "org.http4s" %% "http4s-blaze-client" % http4sVersion,
  "org.http4s" %% "http4s-circe" % http4sVersion,

  "io.chrisdavenport" %% "log4cats-slf4j" % log4CatsVersion,

  "ch.qos.logback" % "logback-classic" % "1.2.3",

  "io.chrisdavenport" %% "epimetheus-http4s" % epimetheusVersion,
  "org.typelevel" %% "simulacrum" % "1.0.0",

  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-generic-extras" % circeVersion,
  "io.circe" %% "circe-optics" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "io.circe" %% "circe-fs2"% circeVersion,
  "io.circe" %% "circe-config" % circeConfigVersion,

  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,


"org.tpolecat" %% "doobie-core" % doobieVersion,
  "org.tpolecat" %% "doobie-h2" % doobieVersion,
  "org.tpolecat" %% "doobie-hikari" % doobieVersion,
  "org.tpolecat" %% "atto-core" % "0.8.0",

  "org.specs2" %% "specs2-core" % "4.8.0" % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "org.mockito" %% "mockito-scala" % "1.15.0" % Test,
  "org.scalaj" %% "scalaj-http" % "2.4.2" % Test,
  "org.tpolecat" %% "doobie-scalatest" % doobieVersion % Test,
  "io.chrisdavenport" %% "cats-scalacheck" % catsScalacheckVersion % Test,
  "org.scalatestplus" %% "scalatestplus-scalacheck" % scalaTestVersion % Test,
  "org.scalatestplus" %% "selenium-2-45" % scalaTestVersion % Test,

  "org.typelevel" %% "cats-tagless-macros" % catsTaglessVersion,

  "com.h2database" % "h2" % "1.4.200",
  "mysql" % "mysql-connector-java" % "8.0.22",


  "co.fs2" %% "fs2-core" % fs2Version,
  "co.fs2" %% "fs2-io" % fs2Version,

  "io.github.jmcardon" %% "tsec-http4s" % tsecV
)

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full)

fork in run := true
