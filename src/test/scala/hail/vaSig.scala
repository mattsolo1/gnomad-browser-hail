import org.scalatest._

import is.hail.HailContext

class VariantsSigSpec extends FlatSpec with Matchers {

  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "object" should "do something specific" in {
    // println(vds.metadata.vaSignature.fields)
    // println(vds.metadata.vaSignature.fields(0))
    // println(vds.metadata.vaSignature.fields(3))
    // println(vds.metadata.vaSignature.fields(3))
    // println(vds.metadata.vaSignature.fields(3).fieldIdx(3))
  }
}