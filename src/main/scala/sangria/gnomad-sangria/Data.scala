package gnomadsangria

// import is.hail.HailContext
// import is.hail.annotations._
// import is.hail.expr._
// // import is.hail.utils._
import is.hail.expr.{EvalContext, Parser, TArray, TBoolean, TDouble, TFloat, TGenotype, TInt, TLong, TSample, TSet, TString, TVariant, Type}
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

    val vas = filteredVariants.vaSignature

    // val data = vds.rdd.collect().toList
    val kt = vds.variantsKT
    

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
