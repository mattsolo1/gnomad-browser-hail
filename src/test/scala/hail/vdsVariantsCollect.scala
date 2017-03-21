import org.scalatest._

import is.hail.HailContext

class VariantsCollectSpec extends FlatSpec with Matchers {

  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "object" should "do something specific" in {
    val variants = vds.variants.collect()
    variants.take(5).foreach(println)
  }
}