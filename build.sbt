name := "mockito-scala-as-dependency"

version := "0.2"

scalaVersion := "2.12.6"

libraryDependencies ++= Seq(
  "org.mockito" %% "mockito-scala" % "0.0.14" % Test,
  "org.mockito" % "mockito-core" % "2.21.0" % Test,
  "org.scalatest" %% "scalatest" % "3.0.5" % Test
)