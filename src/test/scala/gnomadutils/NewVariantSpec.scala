package gnomadutils

import is.hail.HailContext
import org.scalatest._

class NewVariants extends FlatSpec with Matchers {
  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)

  "object" should "do something specific" in {
    val variants = LossOfFunctionVariant.newVariants(vds)
  }
}