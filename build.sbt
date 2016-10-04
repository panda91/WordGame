name := """WordScramble"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "org.mongodb" % "mongodb-driver" % "3.3.0",
  "org.webjars" % "angularjs" % "1.5.8",
  "org.webjars" % "requirejs" % "2.3.2",
  "org.webjars" % "bootstrap" % "3.3.7-1",
  "org.webjars" % "angular-ui-bootstrap" % "1.3.3"
)     

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

pipelineStages := Seq(rjs, digest, gzip)


fork in run := true

EclipseKeys.preTasks := Seq(compile in Compile)
// Java project. Don't expect Scala IDE
EclipseKeys.projectFlavor := EclipseProjectFlavor.Java
// Use .class files instead of generated .scala files for views and routes
EclipseKeys.createSrc := EclipseCreateSrc.ValueSet(EclipseCreateSrc.ManagedClasses, EclipseCreateSrc.ManagedResources)

fork in run := true