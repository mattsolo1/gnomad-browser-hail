package gnomadsangria

import is.hail.variant.{VariantDataset}

import gnomadutils.{FetchData, GnomadGene, Process, VdsVariant}
import gnomadutils.FilterByInterval.getVariantsInGene
import gnomadutils.VdsVariant.toVdsSchemaVariants

class GnomadDatabase(datasets: Map[String, VariantDataset]) {

  def getGene(gene_name: String): Option[GnomadGene] = {
    val geneData = FetchData.fetchGeneByName(gene_name)
    Some(geneData)
  }

  def getVariants(dataSource: String, selectPass: Option[Boolean],
    contig: String,
    start: Int, stop:
    String
   ): List[VdsVariant] = {


    val intervalString = s"${contig}:${start}-${stop}"
    val filteredByGene = getVariantsInGene(datasets(dataSource), intervalString)
//    val addedFields = Process.processForBrowser(newSchemaMap, filteredByGene)

    val filteredByExpression = Process.filterByExpression(selectPass, filteredByGene)
    val gnomadVariants = toVdsSchemaVariants(filteredByExpression)

    gnomadVariants
  }
}
