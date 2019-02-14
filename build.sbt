enablePlugins(org.nlogo.build.NetLogoExtension, org.nlogo.build.ExtensionDocumentationPlugin)

name := "table"

version := "1.3.0"

netLogoClassManager := "org.nlogo.extensions.table.TableExtension"

netLogoTarget :=
  org.nlogo.build.NetLogoExtension.directoryTarget(baseDirectory.value)

javaSource in Compile := baseDirectory.value / "src"

javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path",
  "-encoding", "us-ascii")

netLogoVersion := "6.0.4-9328ba6"
