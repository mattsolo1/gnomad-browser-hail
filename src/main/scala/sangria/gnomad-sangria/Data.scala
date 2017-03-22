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

    val vas = filteredVariants.vaSignature

    val results = filteredVariants.rdd.map { case (v, (va, gs)) =>
      GnomadVariant(
        contig = v.contig,
        start = v.start,
        ref = v.ref
      )
    }

    val collected = results.collect().toList

    collected
  }
}
