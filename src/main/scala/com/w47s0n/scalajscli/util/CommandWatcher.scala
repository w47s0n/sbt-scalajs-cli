package com.w47s0n.scalajscli.util

import scala.sys.process._
import java.io.{BufferedReader, InputStreamReader}
import scala.util.matching.Regex

object CommandWatcher {
  private def readStream(stream: java.io.InputStream, onLine: String => Unit): Unit = {
    val reader = new BufferedReader(new InputStreamReader(stream))
    Iterator.continually(reader.readLine()).takeWhile(_ != null).foreach(onLine)
  }

  def watch(cmd: CLIHelper.Cmd, successMessage: String): Unit = {

    val process = Process(
      cmd.command.split(" "),
      cmd.workingDir,
      "FORCE_COLOR" -> "1"
    ).run(
      new ProcessIO(
        _ => (),
        stdout =>
          readStream(
            stdout,
            line =>{
              if (cmd.successPattern.findFirstIn(LoggingUtils.stripAnsi(line)).isDefined)
                println(s"\n$successMessage")
            }
          ),
        stderr => readStream(stderr, line => println(s"⚠️ $line"))
      )
    )

    process.exitValue()
  }
}
