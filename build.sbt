enablePlugins(org.nlogo.build.NetLogoExtension)

name := "table"

netLogoClassManager := "org.nlogo.extensions.table.TableExtension"

netLogoTarget :=
  org.nlogo.build.NetLogoExtension.directoryTarget(baseDirectory.value)

javaSource in Compile := baseDirectory.value / "src"

javacOptions ++= Seq("-g", "-deprecation", "-Xlint:all", "-Xlint:-serial", "-Xlint:-path",
  "-encoding", "us-ascii")

val netLogoJarURL =
    Option(System.getProperty("netlogo.jar.url")).getOrElse("http://ccl.northwestern.edu/netlogo/5.3.0/NetLogo.jar")

val netLogoJarOrDependency = {
  import java.io.File
  import java.net.URI
  if (netLogoJarURL.startsWith("file:"))
    Seq(unmanagedJars in Compile += new File(new URI(netLogoJarURL)))
  else
    Seq(libraryDependencies += "org.nlogo" % "NetLogo" % "5.3.0" from netLogoJarURL)
}

netLogoJarOrDependency
