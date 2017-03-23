package gnomadutils

import is.hail.HailContext
import is.hail.variant.VariantDataset
import is.hail.utils.IntervalTree
import is.hail.variant.Locus

object FilterByInterval {

  def filterVariantsByInterval(hc: HailContext, intervalString: String, vdsPath: String): VariantDataset = {

    val vds = hc.read(vdsPath)
    val interval = Locus.parseInterval(intervalString)
    val tree = IntervalTree(Array(interval))
    val subset = vds.filterIntervals(tree, keep = true)
    subset
  }

  def getVariantsInGene(vds: VariantDataset, intervalString: String): VariantDataset = {

    val interval = Locus.parseInterval(intervalString)
    val tree = IntervalTree(Array(interval))
    val subset = vds.filterIntervals(tree, keep = true)
    subset
  }
}
