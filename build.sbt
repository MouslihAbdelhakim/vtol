name := "vtol"

//takes from https://github.com/scala/scala/blob/2.13.x/src/compiler/scala/tools/nsc/settings/Warnings.scala
lazy val recommendedCompilerOptions = Seq(
  "-deprecation",
  "-encoding",
  "utf-8",
  "-explaintypes",
  "-feature",
  "-language:existentials",
  "-language:experimental.macros",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xcheckinit",
  "-Xfatal-warnings",
  "-Wdead-code",
  "-Wvalue-discard",
  "-Wnumeric-widen",
  "-Woctal-literal",
  "-Wextra-implicit",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:params",
  "-Ywarn-unused:linted",
  "-Xlint:adapted-args",
  "-Xlint:nullary-unit",
  "-Xlint:inaccessible",
  "-Xlint:infer-any",
  "-Xlint:missing-interpolator",
  "-Xlint:doc-detached",
  "-Xlint:private-shadow",
  "-Xlint:type-parameter-shadow",
  "-Xlint:poly-implicit-overload",
  "-Xlint:option-implicit",
  "-Xlint:delayedinit-select",
  "-Xlint:package-object-classes",
  "-Xlint:stars-align",
  "-Xlint:strict-unsealed-patmat",
  "-Xlint:constant",
  "-Xlint:unused",
  "-Xlint:nonlocal-return",
  "-Xlint:implicit-not-found",
  "-Xlint:serial",
  "-Xlint:valpattern",
  "-Xlint:eta-zero",
  "-Xlint:eta-sam",
  "-Xlint:deprecation",
  "-Xlint:byname-implicit",
  "-Xlint:recurse-with-default",
  "-Xlint:unit-special",
  "-Xlint:multiarg-infix",
  "-Xlint:implicit-recursion"
)

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.13.5",
  scalafmtOnCompile := true,
  scalacOptions ++= recommendedCompilerOptions,
  scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings")
)

lazy val buildSettings = Seq(
  assemblyOutputPath in assembly := file("./bin/vtol-flight-control.jar"),
  mainClass in assembly := Some("io.github.mouslihabdelhakim.vtol.Main")
)

lazy val D = new {
  lazy val Version = new {
    val fs2 = "2.5.0"
  }

  val fs2   = "co.fs2" %% "fs2-core" % Version.fs2
  val fs2io = "co.fs2" %% "fs2-io"   % Version.fs2
}

lazy val vtol = Project(
  id = "vtol",
  base = file(".")
).settings(moduleName := "vtol")
  .settings(commonSettings)
  .aggregate(`flight-controller`)

lazy val `flight-controller` = Project(
  id = "flight-controller",
  base = file("./flight-controller")
).settings(moduleName := "flight-controller")
  .settings(commonSettings)
  .settings(buildSettings)
  .settings(
    libraryDependencies ++= Seq(
      D.fs2,
      D.fs2io
    )
  )

addCommandAlias("validate", ";scalafmtCheck;scalafmtSbtCheck;test")
addCommandAlias("build", ";clean;validate;assembly")
