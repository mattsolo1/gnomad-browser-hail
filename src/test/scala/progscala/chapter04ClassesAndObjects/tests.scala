package classesandobjects

import org.scalatest._

class ClassesAndObjects extends FlatSpec with Matchers {
  "checksum accumulator v1" should "have members that can be reassigned but some that can't" in {
    val firstAccumulator = new ChecksumAccumulatorV1
    val secondAccumulator = new ChecksumAccumulatorV1

    firstAccumulator.publicSum = 3
    firstAccumulator.publicSum should be (3)

    //  doesn't compile, values cannot be reassigned
    // firstAccumulator = new CheckStoneAccumulator

    // doesn't compile, private variables cannot be accessed outside the object
    // firstAccumulator.privateSum = 1337
  }

  "checksum accumulator singleton object" should "accumulate bytes" in {
    ChecksumAccumulator.cache should be (Map())
    ChecksumAccumulator.calculate("Every value has an object") should be (-42)
    ChecksumAccumulator.cache should be (Map("Every value has an object" -> -42))
    ChecksumAccumulator.calculate("How many bytes?") should be (-137)
    ChecksumAccumulator.calculate("Unique new york, unique new york") should be (-248)
    ChecksumAccumulator.calculate("Every value has an object") should be (-42)
    ChecksumAccumulator.calculate("How many bytes?") should be (-137)
    ChecksumAccumulator.cache should be (Map(
      "Every value has an object" -> -42,
      "How many bytes?" -> -137,
      "Unique new york, unique new york" -> -248
    ))
  }
}