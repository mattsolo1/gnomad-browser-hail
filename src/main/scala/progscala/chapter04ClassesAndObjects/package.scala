package classesandobjects

import scala.collection.mutable.Map

class ChecksumAccumulatorV1 {
  var publicSum = 0
  private var privateSum = 42
  // procedural method, no equal sign means returns Unit
  def add(b: Byte) { privateSum += b }
  def checksum(): Int = ~(privateSum & 0xFF) + 1
}

class ChecksumAccumulator {
  private var sum = 0
  def add(b: Byte) { sum += b }
  def checksum(): Int = ~(sum & 0xFF) + 1
}

object ChecksumAccumulator {

  val cache = Map[String, Int]()

  def calculate(string: String): Int = {
    if (cache.contains(string)) {
      cache(string)
    }
    else {
      val accumulator = new ChecksumAccumulator
      for (character <- string)
        accumulator.add(character.toByte)
      val cs = accumulator.checksum()
      cache += (string -> cs)
      cs
    }
  }
}
