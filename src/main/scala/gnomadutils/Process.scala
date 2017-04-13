package gnomadutils

import is.hail.annotations._
import is.hail.expr.Field
import is.hail.variant.VariantDataset

import scala.collection.mutable.ArrayBuffer

object Process {
  def test(x: Any) = identity(x)

  def addNewFieldstoSignature(schemaMap: Map[String, List[String]], vds: VariantDataset) =
    schemaMap.foldLeft((vds, List[Querier](), List[Inserter]()))((acc, schema) => {
      val (key, path) = schema
      val query = acc._1.vaSignature.query(path)
      val typ = acc._1.vaSignature.fieldOption(path) match {
        case Some(Field(name, typ, index, attrs)) => typ
      }
      val (signature, inserter) = acc._1.vaSignature.insert(typ, key)
      (acc._1.copy(vaSignature = signature), acc._2 :+ query, acc._3 :+ inserter)
    })

  def singlePass(schemaMap: Map[String, String], vds: VariantDataset) =
    schemaMap.foldLeft(vds)((acc, schema) => {
      val (key, path) = schema
      val (signature, querier) = acc.queryVA(path)
      val (typ, inserter) = acc.vaSignature.insert(signature, key)
      acc.mapAnnotations((v, va, gs) => inserter(va, querier(va))).copy(vaSignature = typ)
    })

  def addAnnotations(bundle: Tuple3[VariantDataset, List[Querier], List[Inserter]]): VariantDataset = {
    val fs = bundle._2.zip(bundle._3)
    val added = bundle._1.mapAnnotations((v, va, gs) =>
      fs.foldLeft(va){ case (acc, (querier, inserter)) => inserter(acc, querier(acc))})
    added
  }


  def processForBrowser(schemaMap: Map[String, List[String]], vds: VariantDataset) =
    addAnnotations(addNewFieldstoSignature(schemaMap, vds))

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

  def filterByExpression(selectPass: Option[Boolean], vds: VariantDataset) = selectPass match {
    case Some(pass: Boolean) => {
      val results = vds.filterVariants { case (v, va, gs ) =>
        v.ref == "G"
      }
      results
    }
    case None => vds
  }
}
