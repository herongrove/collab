import sbt.Tests

name := "collab"

lazy val commonScalacOptions = Seq(
  "-feature",
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  // "-Ywarn-value-discard",
  "-Ywarn-unused",
  "-encoding", "utf8"
)

lazy val commonSettings = Seq(
  // NOTE: spark is still stuck at 2.11 : (
  scalaVersion := "2.11.11", // "2.12.4",
  crossScalaVersions := Seq("2.11.11", "2.12.4"),
  // we want to use -Ywarn-unused-import most of the time
  scalacOptions ++= commonScalacOptions,
  scalacOptions += "-Ywarn-unused-import",
  // -Ywarn-unused-import is annoying in the console
  scalacOptions in (Compile, console) := commonScalacOptions,
  // show test duration
  testOptions in Test += Tests.Argument("-oD"),
  // avoid dep. conflict in assembly task for webapp
  excludeDependencies += "commons-logging" % "commons-logging",
  parallelExecution in Test := false
)

libraryDependencies ++= {
  val procVer = "7.4.4"

  Seq(
    "org.clulab"          %%  "processors-main"          % procVer,
    "org.clulab"          %%  "processors-corenlp"       % procVer,
    "org.clulab"          %%  "processors-modelsmain"    % procVer,
    "org.clulab"          %%  "processors-modelscorenlp" % procVer,
    "org.clulab"          %%  "processors-odin"          % procVer,
    "org.slf4j"           %   "slf4j-api"                % "1.7.10",
    "org.slf4j"           %   "slf4j-simple"             % "1.7.5"
  )
}