import org.scalatest._

import org.apache.spark.{SparkContext, SparkConf}

class BasicRDDSpec extends FlatSpec with Matchers {
  val conf = new SparkConf().setMaster("local").setAppName("test")
  val sc = new SparkContext(conf)

  "sc.parallelize" should "create a new RDD for wordcounting" in {
    val lines = sc.parallelize(List("pandas", "i like pandas"))
    val words = lines.flatMap(line => line.split(" "))
    words.foreach(println)
    val mapped = words.map(word => (word, 1))
    mapped.reduceByKey { case (x, y) => x + y }.foreach(println)
    val counts = mapped.reduceByKey { case (x, y) => x + y }.collect()
    counts.foreach(println)
    val countMap = counts.toMap
    countMap("pandas") should be (3)
  }


}
