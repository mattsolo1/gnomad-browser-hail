package gnomadutils

import is.hail.annotations._
import is.hail.expr.{Field, TDouble, TFloat, TInt, Type}
import is.hail.variant.VariantDataset

import scala.collection.mutable.ArrayBuffer

case class gnomadAnnotation(gKey: String, gTyp: Type, gQuerier: Querier) {

}

object Process {

  val multiAllelicIntegers = Set(
    "allele_count"
  )

  val multiAllelicDouble = Set(
    "allele_frequency"
  )

  def getGnomadTyp(key: String, typ: Type) = {
    key match {
      case (key) if multiAllelicIntegers.contains(key) => TInt
      case (key) if multiAllelicDouble.contains(key) => TDouble
      case _  => typ
    }
  }

  def processAnnotation(annotation: Annotation, querier: Querier, key: String) = {
    val value = querier(annotation)
    key match {
      case (key) if multiAllelicIntegers.contains(key) => value.asInstanceOf[ArrayBuffer[Int]](0)
      case (key) if multiAllelicDouble.contains(key) => value.asInstanceOf[ArrayBuffer[Double]](0)
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

          val gnomadTyp = getGnomadTyp(key, typ)

          val (signature, inserter) = vdsAcc.vaSignature.insert(gnomadTyp, key)
          (vdsAcc.copy(vaSignature = signature), queryAcc :+ query, inserterAcc :+ inserter)
        }
  }

    val fs = (queriers, inserters, schemaMap.keys).zipped
    val vdsWithAddedAnnotations = vdsWithSchema.mapAnnotations((v, va, gs) =>
      fs.foldLeft(va){ case (acc, (querier, inserter, key)) => inserter(acc, processAnnotation(va, querier, key))})
    vdsWithAddedAnnotations
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
