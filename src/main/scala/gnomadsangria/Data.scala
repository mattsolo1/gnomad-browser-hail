package gnomadsangria

import gnomadutils.{VdsVariant, GnomadGene, FetchData}

import is.hail.variant.{VariantDataset}

import gnomadutils.FilterByInterval.{getVariantsInGene}
import gnomadutils.VdsVariant.{toVdsSchemaVariants}

class GnomadDatabase(datasets: List[VariantDataset]) {

  def getGene(gene_name: String): Option[GnomadGene] = {
    val geneData = FetchData.fetchGeneByName(gene_name)
    Some(geneData)
  }

  def getVariants(contig: String, start: Int, stop: String): List[VdsVariant] = {
    val intervalString = s"${contig}:${start}-${stop}"
    val filteredVariants = getVariantsInGene(datasets(0), intervalString)
    println(filteredVariants.count())
    val gnomadVariants = toVdsSchemaVariants(filteredVariants)
    gnomadVariants.take(10)
  }
}
