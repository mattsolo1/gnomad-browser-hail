package example

object Hello extends Greeting with App {
  println(greeting)
}

trait Greeting {
  lazy val greeting: String = "hello world!!"

  def goodbye(name: String): String = {
    s"Goodbye, ${name}"
  }
}
