import org.scalatest._

import is.hail.HailContext
import is.hail.utils._

import org.json4s.jackson.{JsonMethods, Serialization}

class SchemaParsingTest extends FlatSpec with Matchers {

  val hc = HailContext()

  "object" should "do something specific" in {
    val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
    val metadataFile = vdsPath + "/metadata.json.gz"
    val json = hc.hadoopConf.readFile(metadataFile)(in => JsonMethods.parse(in))
    val something = true
    // something should be (true)
    // println(json)

    val vds = hc.read(vdsPath)
    vds.rdd.mapPartitions(it => {
      it.map { case (v, (va, gs)) =>
        println(va)
      }
    })
  }
}
