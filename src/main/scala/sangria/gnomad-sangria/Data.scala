package gnomadsangria

// import is.hail.HailContext
// import is.hail.annotations._
// import is.hail.expr._
// // import is.hail.utils._
import is.hail.variant.{VariantDataset, Genotype, Locus, Variant}

import gnomadutils.FilterByInterval.{filterVariantsByInterval}

class GnomadDatabase(vds: VariantDataset) {

  def getGene(gene_name: String): Option[GnomadGene] = {
    val geneData = FetchData.fetchGeneByName(gene_name)
    Some(geneData)
  }
}
