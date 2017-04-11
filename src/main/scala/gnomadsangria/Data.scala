package gnomadsangria

import gnomadutils.{VdsVariant, GnomadGene, FetchData}

import is.hail.variant.{VariantDataset}

import gnomadutils.FilterByInterval.{getVariantsInGene}
import gnomadutils.VdsVariant.{toVdsSchemaVariants}

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
    val filteredByExpression = selectPass match {
      case Some(pass: Boolean) => {
        val results = filteredByGene.filterVariants { case (v, va, gs ) =>
            v.ref == "G"
        }
        results
      }
      case None => filteredByGene
    }
    val gnomadVariants = toVdsSchemaVariants(filteredByExpression)

    gnomadVariants.take(10)
  }
}
