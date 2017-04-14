package gnomadutils

import is.hail.annotations._
import is.hail.expr.{Field, TDouble, TFloat, TInt, TString, TStruct, Type}
import is.hail.variant.VariantDataset

import scala.collection.mutable.ArrayBuffer

object Process {
  val countsMap = List(
    ("allele_count", List("info", "AC")),
    ("allele_number", List("info", "AN")),
    ("allele_frequency", List("info", "AF")),
    ("consequence", List("info", "CSQ"))
  )

  val qualityControlMetrics = Set(
    "BaseQRankSum",
    "ClippingRankSum",
    "DB",
    "DP",
    "FS",
    "InbreedingCoeff",
    "MQ",
    "MQRankSum",
    "QD",
    "ReadPosRankSum",
    "SOR",
    "VQSLOD",
    "VQSR_culprit"
  )

  val populationCodes = List(
    ("european_non_finnish", "NFE"),
    ("east_asian", "EAS"),
    ("other", "OTH"),
    ("african", "AFR"),
    ("latino", "AMR"),
//    ("south_asian", "SAS"),
    ("european_finnish", "FIN"),
    ("ashkenazi_jewish", "ASJ")
  )

  def populationMap(codes: List[(String, String)] = populationCodes): List[(String, List[String])] =
    codes.flatMap { case (key, value) =>
      List(
        (s"${key}_ac", List("info", s"AC_$value")),
        (s"${key}_an", List("info", s"AN_$value")),
        (s"${key}_af", List("info", s"AF_$value"))
      )
    }

  val multiAllelicIntegers = Set(
    "allele_count",
    "ashkenazi_jewish_ac"
  )

  val multiAllelicDouble = Set(
    "allele_frequency"
  )

  val schemaMap = countsMap ++ populationMap(populationCodes)

  def getSchemaLocation(key: String, vas: Type) =
    key match {
//      case (key) if qualityControlMetrics.contains(key) => Some("qualityMetrics")
      case _  => vas
    }

  def getGnomadTyp(key: String, typ: Type) = {
    key match {
      case (key) if multiAllelicIntegers.contains(key) => TInt
      case (key) if multiAllelicDouble.contains(key) => TDouble
      case "consequence" => TString
      case _  => typ
    }
  }

  def getConsequences(csq: ArrayBuffer[String]) = {
    csq(0).split("\\|")(1)
  }

  def processAnnotation(annotation: Annotation, querier: Querier, key: String) = {
    val value = querier(annotation)
    key match {
      case (key) if multiAllelicIntegers.contains(key) => value.asInstanceOf[ArrayBuffer[Int]](0)
      case (key) if multiAllelicDouble.contains(key) => value.asInstanceOf[ArrayBuffer[Double]](0)
      case "consequence" => getConsequences(value.asInstanceOf[ArrayBuffer[String]])
      case _ => value
    }
  }

  def addAnnotations(vds: VariantDataset, schemaMap: List[(String, List[String])] = schemaMap): VariantDataset = {
    println(schemaMap)
    val (vdsWithSchema, queriers, inserters) =
      schemaMap.foldLeft((vds, List[Querier](), List[Inserter]())){
        case ((vdsAcc, queryAcc, inserterAcc), schema) => {
          val (key, path) = schema
          val query = vdsAcc.vaSignature.query(path)
          val Some(Field(_, typ, _, _)) = vdsAcc.vaSignature.fieldOption(path)

          val schemaLoc = getSchemaLocation(key, vdsAcc.vaSignature)
          val gnomadTyp = getGnomadTyp(key, typ)

          val (signature, inserter) = schemaLoc.insert(gnomadTyp, key)
          (vdsAcc.copy(vaSignature = signature), queryAcc :+ query, inserterAcc :+ inserter)
        }
  }

    val fs = (queriers, inserters, schemaMap.map(_._1)).zipped
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
