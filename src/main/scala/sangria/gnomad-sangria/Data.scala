package gnomadsangria

// import is.hail.HailContext
// import is.hail.annotations._
// import is.hail.expr._
// // import is.hail.utils._
import is.hail.variant.{VariantDataset, Genotype, Locus, Variant}


case class Gene(gene_name: String, gene_id: String)

class GnomadDatabase(vds: VariantDataset) {
  import GnomadDatabase._

  def getGene(gene_name: String): Option[Gene] = {
    println(vds.count())
    genes.find(gene => gene.gene_name == gene_name)
  }
}

object GnomadDatabase {
  val genes = List(
    Gene(
      gene_id = "001",
      gene_name = "PCSK9"
    ),
    Gene(
      gene_id = "002",
      gene_name = "PPARA"
    )
  )
}