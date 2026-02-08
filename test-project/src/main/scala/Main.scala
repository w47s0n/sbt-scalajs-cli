import scala.scalajs.js
import org.scalajs.dom
import org.scalajs.dom.document

object Main {
  def main(args: Array[String]): Unit = {
    println("Scala.js app started!")

    val container = document.getElementById("app")
    if (container != null) {
      container.innerHTML = """
        <div style="font-family: system-ui; padding: 2rem;">
          <h1>🚀 Scala.js + Vite</h1>
          <p>Hello from Scala.js!</p>
          <button id="counterBtn">Click me: 0</button>
        </div>
      """

      setupCounter()
    }
  }

  def setupCounter(): Unit = {
    var count = 0
    val button = document.getElementById("counterBtn")

    button.addEventListener("click", { (_: dom.Event) =>
      count += 1
      button.textContent = s"Click me: $count"
    })
  }
}
