package gnomadsangria

import gnomadutils.{GnomadVariant, GnomadGene, FetchData}

import is.hail.variant.{VariantDataset}

import gnomadutils.FilterByInterval.{getVariantsInGene}
import gnomadutils.GnomadVariant.{toGnomadVariants}

class GnomadDatabase(vds: VariantDataset) {
  def getGene(gene_name: String): Option[GnomadGene] = {
    val geneData = FetchData.fetchGeneByName(gene_name)
    Some(geneData)
  }

  def getVariants(contig: String, start: Int, stop: String): List[GnomadVariant] = {
    val intervalString = s"${contig}:${start}-${stop}"
    val filteredVariants = getVariantsInGene(vds, intervalString)
    val gnomadVariants = toGnomadVariants(filteredVariants)
    gnomadVariants
  }
}
