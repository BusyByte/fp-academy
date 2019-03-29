// Scala
val catsV = "1.6.0"
val catsEffectV = "0.10.1"
val fs2V = "0.10.6"
val specs2V = "4.3.4"

lazy val `homework` =
  project
    .in(file("modules/homework"))
    .settings(commonSettings)
    .settings(
      name := "fp-academy-homework",
      scalafmtOnCompile in ThisBuild := true,
      scalafmtTestOnCompile in ThisBuild := true
    )

lazy val commonSettings = Seq(
  organization := "net.nomadicalien",
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq("-Xmax-classfile-name", "242"),
  cancelable in Scope.Global := true,
  addCompilerPlugin("org.spire-math" % "kind-projector"      % "0.9.9" cross CrossVersion.binary),
  addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.2.4"),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "cats-core"         % catsV,
    "org.typelevel" %% "cats-effect"       % catsEffectV,
    "co.fs2"        %% "fs2-io"            % fs2V,
    "org.specs2"    %% "specs2-core"       % specs2V % Test,
    "org.specs2"    %% "specs2-scalacheck" % specs2V % Test
  )
)
