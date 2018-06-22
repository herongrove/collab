name := "protests"

libraryDependencies ++= {
  val procVer = "7.3.1"

  Seq(
    "org.clulab"          %%  "processors-main"          % procVer,
    "org.clulab"          %%  "processors-corenlp"       % procVer,
    "org.clulab"          %%  "processors-modelsmain"    % procVer,
    "org.clulab"          %%  "processors-modelscorenlp" % procVer,
    "org.clulab"          %%  "processors-odin"          % procVer
  )
}