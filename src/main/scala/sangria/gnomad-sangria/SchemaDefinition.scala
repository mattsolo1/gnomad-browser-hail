package gnomadsangria

import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema._

import scala.concurrent.Future

object SchemaDefinition {
  val VariantType = ObjectType(

    "Variant", fields[Unit, GnomadVariant](
      Field(
        "contig",
        StringType,
        Some("Chromosome"),
        resolve = _.value.contig
      ),
      Field(
        "start",
        LongType,
        Some("Start position"),
        resolve = _.value.start
      ),
      Field(
        "ref",
        StringType,
        Some("Reference allele"),
        resolve = _.value.ref
      ),
      Field(
        "allele_count",
        ListType(IntType),
        Some("Allele count"),
        resolve = _.value.allele_count
        )
    )
  )

  val GeneType = ObjectType(

    "Gene", fields[GnomadDatabase, GnomadGene](
      Field(
        "gene_name",
        StringType,
        Some("The name of the gene"),
        resolve = _.value.gene_name
      ),
      Field(
        "gene_id",
        StringType,
        Some("The gene id"),
        resolve = _.value.gene_id
      ),
      Field(
        "chrom",
        StringType,
        Some("The gene id"),
        resolve = _.value.chrom
      ),
      Field(
        "start",
        IntType,
        Some("Gene start position"),
        resolve = _.value.start
      ),
      Field(
        "stop",
        StringType,
        Some("Gene stop position"),
        resolve = _.value.stop
      ),
      Field(
        "exome_variants",
        ListType(VariantType),
        Some("Exome variants"),
        resolve = (context) => {
          val gene = context.value
          val variants = context.ctx.getVariants(gene.chrom, gene.start, gene.stop)
          variants
        }
      )
    )
  )

  val GeneNameArgument = Argument("gene_name", StringType, description = "Gene name")

  val Query = ObjectType("Query", fields[GnomadDatabase, Unit](
    Field("gene", OptionType(GeneType),
      arguments = GeneNameArgument :: Nil,
      resolve = (ctx) => ctx.ctx.getGene(ctx.arg(GeneNameArgument)))))

  val GnomadSchema = Schema(Query)
}
