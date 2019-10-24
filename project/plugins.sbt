addSbtPlugin("org.scalaxb" % "sbt-scalaxb" % "1.7.1")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.9.0")
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")
//addSbtPlugin("com.eed3si9n" % "sbt-sequential" % "0.1.0")
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
addSbtPlugin("com.typesafe.sbt" % "sbt-site" % "1.4.0")

addSbtPlugin("io.gatling" % "gatling-sbt" % "3.0.0")

addSbtPlugin("org.clapper" % "sbt-editsource" % "1.0.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.10.0-RC1")
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.4.2")
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "1.0.0")
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0")
//addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.9.6")
addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.7")
//addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.3")
//addSbtPlugin("com.lucidchart" % "sbt-scalafmt" % "1.16")

addSbtPlugin("com.github.xuwei-k" % "sbt-class-diagram" % "0.2.1")
addSbtPlugin("org.duhemm" % "sbt-errors-summary" % "0.6.3")


addSbtPlugin("pl.project13.scala" % "sbt-jmh" % "0.3.7")
// if you use coursier, you must use sbt-scalafmt-coursier
// addSbtPlugin("com.lucidchart" % "sbt-scalafmt-coursier" % "1.16")
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.4.1")
libraryDependencies += "com.spotify" % "docker-client" % "8.9.0"


addSbtPlugin("com.typesafe.sbt" % "sbt-git" % "1.0.0")
//addSbtPlugin("ph.samson" % "sbt-groovy" % "0.2.0-beta4")

//addSbtPlugin("ch.epfl.scala" % "sbt-release-early" % "2.1.1")
//This plugin depends on the following sbt plugins:
//sbt-dynver, version 2.0.0.
//sbt-pgp, version 1.0.1.
//sbt-bintray, version 0.5.1.
//sbt-sonatype, version 1.1.
//If you already depend on them, remove them from your plugins.sbt file. The addSbtPlugin line above will bring them in automatically.
addSbtPlugin("com.dwijnand" % "sbt-dynver" % "4.0.0")
//addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.0-M2")
//addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.6")


// ---
//addSbtPlugin("io.sysa" % "sbt-package-courier" % "0.2.0")

//addSbtPlugin("me.amanj" %% "sbt-deploy" % "2.3.3") // TODO or native packager?



//addSbtPlugin("io.get-coursier" % "sbt-coursier" % "2.0.0-RC2-1")
//addSbtCoursier

//addSbtPlugin("org.scala-sbt" % "sbt-autoversion" % "1.0.0")
//addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.11")

//addSbtPlugin("com.tapad.sbt" % "sbt-marathon" % "0.2.1")
//addSbtPlugin("com.tapad.sbt" % "sbt-marathon-templating" % "0.2.1")
//addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.0")


// mimaPreviousArtifacts := previousStableVersion.value.map(organization.value %% name.value % _).toSet
//imaPreviousArtifacts := Set("com.example" %% "my-library" % "1.2.3")
//addSbtPlugin("com.typesafe" % "sbt-mima-plugin" % "0.6.0")

//"org.jetbrains" %% "sbt-structure-core" % "<version>"

//addSbtPlugin("com.lightbend.sbt" % "sbt-javaagent" % "0.1.5")

//addSbtPlugin("com.github.cuzfrog" % "sbt-tmpfs" % "0.3.4")

//addSbtPlugin("com.github.scalaprops" % "sbt-scalaprops" % "0.3.2")
//
//scalapropsSettings
//scalapropsVersion := "0.6.1"
//or
//scalapropsWithScalaz
//scalapropsVersion := "0.6.1"

//addSbtPlugin("de.heikoseeberger" % "sbt-header" % "5.2.0")

//addSbtPlugin("org.scala-sbt" % "sbt-contraband" % "0.4.4")

//addSbtPlugin("com.earldouglas" % "sbt-lit" % "0.0.4")

//addSbtPlugin("org.zjulambda.scala" % "sbt-stainless" % "0.2.0")

//addSbtPlugin("ch.epfl.scala" % "sbt-missinglink" % "0.1.0")
