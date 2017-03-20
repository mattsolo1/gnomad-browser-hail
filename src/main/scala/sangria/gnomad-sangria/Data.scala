package gnomadsangria

// import is.hail.HailContext
// import is.hail.annotations._
// import is.hail.expr._
// // import is.hail.utils._
import is.hail.variant.{VariantDataset, Genotype, Locus, Variant}

import gnomadutils.FilterByInterval.{filterVariantsByInterval}

case class Gene(gene_name: String, gene_id: String)

class GnomadDatabase(vds: VariantDataset) {
  import GnomadDatabase._

  def getGene(gene_name: String): Option[Gene] = {
    val geneData = FetchData.fetchGeneByName(gene_name)
    // println(vds.count())
    val result = Gene(gene_name = geneData.gene_name, gene_id = geneData.gene_id)
    Some(result)
  }
}
