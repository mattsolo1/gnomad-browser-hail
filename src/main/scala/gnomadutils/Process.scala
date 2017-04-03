package gnomadutils

import is.hail.annotations.Annotation
import is.hail.variant.VariantDataset

import scala.collection.mutable.ArrayBuffer

object Process {
  def test(x: Any) = identity(x)

  def getMostSevereConsequence(vds: VariantDataset, va: Annotation) = {
    val (transcriptConsequenceSchema, transcriptConsequencesQuery) = vds.queryVA("va.vep.transcript_consequences")
    println(transcriptConsequenceSchema.schema)
    val alleles = transcriptConsequencesQuery(va).asInstanceOf[ArrayBuffer[AnyVal]]
    println(alleles)
    val consequencesByTranscript = alleles.map(va => {
      println(va.getClass)
//      val allele = transcriptConsequenceSchema.query("variant_allele")(va)
//      val impact = transcriptConsequenceSchema.query("impact")(va)
//      (allele, impact)
    })
    consequencesByTranscript
  }
}
