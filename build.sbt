name := """hey"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(  
  "org.sorm-framework" % "sorm" % "0.3.15",
  "com.h2database" % "h2" % "1.4.177",
  "org.xerial" % "sqlite-jdbc" % "3.7.2"
)     

libraryDependencies += filters

libraryDependencies += jdbc

