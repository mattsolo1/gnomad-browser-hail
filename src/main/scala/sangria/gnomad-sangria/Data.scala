package gnomadsangria

// import is.hail.HailContext
// import is.hail.annotations._
// import is.hail.expr._
// // import is.hail.utils._
import is.hail.variant.{VariantDataset, Genotype, Locus, Variant}

import gnomadutils.FilterByInterval.{getVariantsInGene}

class GnomadDatabase(vds: VariantDataset) {

  def getGene(gene_name: String): Option[GnomadGene] = {
    val geneData = FetchData.fetchGeneByName(gene_name)
    Some(geneData)
  }

  def getVariants(contig: String, start: Int, stop: String): List[GnomadVariant] = {
    val intervalString = s"${contig}:${start}-${stop}"
    val filteredVariants = getVariantsInGene(vds, intervalString)
    val variantCount = filteredVariants.count().nVariants
    val results = List(
      GnomadVariant(
        contig = "1",
        start = variantCount,
        ref = "G"
      ),
      GnomadVariant(
        contig = "1",
        start = variantCount,
        ref = "C"
      ),
      GnomadVariant(
        contig = "1",
        start = variantCount,
        ref = "G"
      )
    )
    results
  }
}
