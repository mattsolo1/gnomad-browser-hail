package gnomadutils

import is.hail.annotations._
import is.hail.expr.{Field, TInt}
import is.hail.variant.VariantDataset

import scala.collection.mutable.ArrayBuffer

object Process {


  def processAnnotation(annotation: Annotation, querier: Querier, key: String) = {
    val value = querier(annotation)
    key match {
      case "allele_count" => value.asInstanceOf[ArrayBuffer[Int]](0)
      case _ => value
    }
  }

  def addAnnotations(schemaMap: Map[String, List[String]], vds: VariantDataset): VariantDataset = {
    val (vdsWithSchema, queriers, inserters) =
      schemaMap.foldLeft((vds, List[Querier](), List[Inserter]())){
        case ((vdsAcc, queryAcc, inserterAcc), schema) => {
          val (key, path) = schema
          val query = vdsAcc.vaSignature.query(path)
          val Some(Field(_, typ, _, _)) = vdsAcc.vaSignature.fieldOption(path)

          val multiAllelicTyp = key match {
            case "allele_count" => TInt
            case _  => typ
          }

          val (signature, inserter) = vdsAcc.vaSignature.insert(multiAllelicTyp, key)
          (vdsAcc.copy(vaSignature = signature), queryAcc :+ query, inserterAcc :+ inserter)
        }
  }

    val fs = (queriers, inserters, schemaMap.keys).zipped
    val vdsWithAddedAnnotations = vdsWithSchema.mapAnnotations((v, va, gs) =>
      fs.foldLeft(va){ case (acc, (querier, inserter, key)) => inserter(acc, processAnnotation(va, querier, key))})
    vdsWithAddedAnnotations
  }

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
