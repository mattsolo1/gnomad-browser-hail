package sangriaserver

import org.scalatest._
import is.hail.HailContext

class ServerTest extends FlatSpec with Matchers {
  val hc = HailContext()
  "object" should "do something specific" in {
    val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
    // Server.run(hc, vdsPath, "0.0.0.0", 8004)
  }
}
