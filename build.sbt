enablePlugins(org.nlogo.build.NetLogoExtension, org.nlogo.build.ExtensionDocumentationPlugin)

name := "table"
version := "2.0.0"
isSnapshot := true

netLogoVersion := "7.0.0-internal1-df97144"
netLogoClassManager := "org.nlogo.extensions.table.TableExtension"
netLogoTestExtras += (baseDirectory.value / "examples")

Compile / javaSource := baseDirectory.value / "src" / "main"
javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path","-encoding", "us-ascii", "--release", "11")

scalaVersion := "2.13.16"
Test / scalaSource := baseDirectory.value / "src" / "test"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-feature", "-encoding", "us-ascii", "-release", "11")

libraryDependencies ++= Seq(
  "com.google.code.gson" % "gson" % "2.8.6"
)
