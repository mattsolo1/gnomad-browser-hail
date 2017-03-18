package filterIntervals

import is.hail.HailContext
import org.scalatest._

class FilterVariantsByIntervalSpec extends FlatSpec with Matchers {
  val hc = HailContext()

  "filterVariantsByInterval" should "return pcsk9 varaints given an interval" in {
    val interval_PCSK9 = "1:55505221-55530525"
    val gnomad_exomes_autosomes = "/Users/msolomon/Data/gnomad/release-170228/gnomad.exomes.r2.0.1.sites.autosomes.vds"
    val filteredVariants = FilterVariantsByInterval.filterVariantsByInterval(hc, interval_PCSK9, gnomad_exomes_autosomes)
    filteredVariants.count().nVariants should be (1135)
  }

  "filterVariantsByInterval" should "return PCDH11Y varaints given an interval" in {
    val interval_PCDH11Y = "Y:4868267-5610265"
    val gnomad_exomes_y_chromosome = "/Users/msolomon/Data/gnomad/release-170228/gnomad.exomes.r2.0.1.sites.Y.vds"
    val filteredVariants = FilterVariantsByInterval.filterVariantsByInterval(hc, interval_PCDH11Y, gnomad_exomes_y_chromosome)
    filteredVariants.count().nVariants should be (461)
  }
}
