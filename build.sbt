name := "reactive" ++ "-" ++ "step1"

scalaVersion := "2.11.12"

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Xexperimental"
)

scalacOptions in Test += "-Ywarn-value-discard:false" // since this often appears in expectNext(expected) testing style in streams

val akkaVersion = "2.5.21"
val akkaHttpVersion = "10.1.8"

libraryDependencies ++= Seq(
  "com.typesafe.akka"        %% "akka-stream"              % akkaVersion,
  "com.typesafe.akka"        %% "akka-stream-testkit"      % akkaVersion % Test,
  "com.typesafe.akka"        %% "akka-stream-typed"        % akkaVersion,
  "com.typesafe.akka"        %% "akka-testkit"             % akkaVersion,


  "org.scalacheck"           %% "scalacheck"               % "1.13.5"    % Test,
  "junit"                    % "junit"                     % "4.10"      % Test,
  "com.typesafe.akka"        %% "akka-slf4j"               % akkaVersion,
  "ch.qos.logback"           % "logback-classic"           % "1.2.3"
)

// https://mvnrepository.com/artifact/org.scalatest/scalatest
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % Test

// courseId := "changeme"

// parallelExecution in Test := false
