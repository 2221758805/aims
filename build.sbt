name := "aims"

organization := "org.jmotor"

version := "1.0.0-SNAPSHOT"

scalaVersion := "2.11.4"

resolvers ++= Dependencies.resolvers

libraryDependencies ++= Dependencies.libraries

import scalariform.formatter.preferences._

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(RewriteArrowSymbols, true)
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)