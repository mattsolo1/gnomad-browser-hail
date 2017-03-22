import org.scalatest._

import is.hail.HailContext

class KeyTableSpec extends FlatSpec with Matchers {

  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "object" should "do something specific" in {
    val kt = vds.variantsKT
  }
}