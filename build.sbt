name := """hey"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala,SbtWeb)

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(  
  "org.sorm-framework" % "sorm" % "0.3.15",
  "com.h2database" % "h2" % "1.4.177",
  "com.amazonaws" % "aws-java-sdk" % "1.3.11"
)     

libraryDependencies += filters

libraryDependencies += jdbc

libraryDependencies += "org.apache.commons" % "commons-io" % "1.3.2"





