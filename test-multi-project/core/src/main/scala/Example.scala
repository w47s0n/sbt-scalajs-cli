package core

object Example {
  def greet(name: String): String = s"Hello, $name!"

  def main(args: Array[String]): Unit = {
    println(greet("World"))
  }
}
