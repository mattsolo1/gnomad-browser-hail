package p {
  class Super {
    protected def myFunction() {  println("My functions!") }
  }
  class Sub extends Super {
    myFunction()
  }
  class Other {
    // (new Super).myFunction() // error: myFunction is not accessible
  }
}
