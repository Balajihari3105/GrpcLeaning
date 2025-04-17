ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.5"

lazy val root = (project in file("."))
  .settings(
    name := "GrpcLeaning"
  )


libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % "1.65.1",
  "io.grpc" % "grpc-protobuf" % "1.65.1",
  "io.grpc" % "grpc-stub" % "1.64.0",
  "org.scalatest" %% "scalatest" % "3.2.18" % Test
)

Compile / PB.targets := Seq(
  scalapb.gen() -> (Compile / sourceManaged).value / "scalapb"
)

//libraryDependencies ++= Seq(
//  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion % "protobuf"
//)


libraryDependencies ++= Seq(
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapb.compiler.Version.scalapbVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapb.compiler.Version.scalapbVersion
)