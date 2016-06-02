name := "Code-Search"

version := "1.0"

scalaVersion := "2.11.8"

scalacOptions ++= Seq("-Xexperimental", "-Ybackend:GenBCode", "-Ydelambdafy:method", "-target:jvm-1.8", "-Yopt:l:classpath")

resolvers += Resolver.sonatypeRepo("releases")

libraryDependencies += "org.scala-lang.modules" % "scala-java8-compat_2.11" % "0.7.0"

libraryDependencies ++= Seq(
  "org.typelevel" %% "cats" % "0.6.0",
  "com.github.pathikrit"  %% "better-files" % "2.16.0",
  "org.scalaj" %% "scalaj-http" % "2.3.0",
  "net.ruippeixotog" %% "scala-scraper" % "1.0.0"
)

// testing
libraryDependencies ++= Seq(
  "org.scalatest"          %% "scalatest"       % "2.2.5"     % "test",
  "org.scalacheck"         %% "scalacheck"      % "1.12.4"    % "test"
)

