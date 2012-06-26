libraryDependencies <+= sbtVersion(v => "com.github.siasia" %% "xsbt-web-plugin" % (v + "-0.2.11"))

resolvers ++= Seq(
  "coda" at "http://repo.codahale.com",
  "Web plugin repo" at "http://siasia.github.com/maven2"
)
