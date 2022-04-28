enablePlugins(org.nlogo.build.NetLogoExtension, org.nlogo.build.ExtensionDocumentationPlugin)

name := "table"
version := "1.3.1"
isSnapshot := true

netLogoVersion := "6.2.2"
netLogoClassManager := "org.nlogo.extensions.table.TableExtension"
netLogoTestExtras += (baseDirectory.value / "examples")

javaSource in Compile := baseDirectory.value / "src" / "main"
javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path","-encoding", "us-ascii")

scalaVersion := "2.12.12"
scalaSource in Test := baseDirectory.value / "src" / "test"
scalacOptions ++= Seq("-deprecation", "-unchecked", "-Xfatal-warnings", "-feature", "-encoding", "us-ascii")

libraryDependencies ++= Seq(
  "com.google.code.gson" % "gson" % "2.8.6"
)
