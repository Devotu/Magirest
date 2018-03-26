name := """Magirest"""
organization := "net.devotu"

version := "0.1-Base"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.4"

libraryDependencies += guice

testOptions += Tests.Argument(TestFrameworks.JUnit, "-a", "-q")
