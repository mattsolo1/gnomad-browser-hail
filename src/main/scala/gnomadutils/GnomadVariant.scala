package gnomadutils

import is.hail.variant.{AltAllele, VariantDataset}
import is.hail.expr.{Field => HailField, Type => HailType, _}
import is.hail.annotations.Annotation
import is.hail.expr.JSONAnnotationImpex
import org.apache.spark.sql.Row
import sangria.schema._

import scala.collection.mutable.ArrayBuffer
import gnomadsangria.GnomadDatabase
import org.json4s.JsonAST.{JInt, JObject}
import org.json4s._

import scala.collection.generic.SeqFactory

case class GnomadVariant(
  contig: String,
  start: Long,
  ref: String,
  altAlleles: Seq[AltAllele],
  rawAnnotations: Annotation,
  annotations: JValue,
//  allele_num:
  integer: Int
) {
}

object GnomadVariant {
  def toGnomadVariants(vds: VariantDataset) = {
    val vas = vds.vaSignature

    val results = vds.rdd.map { case (v, (va, gs)) =>
      GnomadVariant(
        contig = v.contig,
        start = v.start,
        ref = v.ref,
        altAlleles = v.altAlleles,
        rawAnnotations = va,
        annotations = JSONAnnotationImpex.exportAnnotation(va, vas),
        integer = 5
      )
    }
    val collected = results.collect().toList
    collected
  }

  def getAnnotationValues(name: String, va: Annotation, hailType: HailType): Tuple2[String, Any] = {
    hailType match {
      case TBoolean => (name, va.asInstanceOf[Boolean])
      case TStruct(fields) =>
        val row = va.asInstanceOf[Row]
        (name , fields.map(f => getAnnotationValues(f.name, row.get(f.index), f.typ)).toMap)
      case _  => (name, "nothing yet")
    }
  }

  def toGraphQLField(hailField: HailField): Field[GnomadDatabase, GnomadVariant] = {
    implicit val formats = DefaultFormats
    val HailField(name, typ, index, attrs) = hailField
    val gqlfield: Field[GnomadDatabase, GnomadVariant] = typ match {
      case TBoolean => Field(
        name,
        BooleanType,
        toGraphQLDescription(attrs),
       resolve = c => {
         val result = (c.value.annotations \\ name).extract[Boolean]
         if (result == null) false else result
       }
      )
      case TString => Field(
        name,
        StringType,
        toGraphQLDescription(attrs),
        resolve = c => {
          val result = (c.value.annotations \\ name).extract[String]
          if (result == null) "Nothing to see" else result
        }
      )
      case TInt => Field(
        name,
        IntType,
        toGraphQLDescription(attrs),
        resolve = c => {
//          println(name, typ)
          val result = (c.value.annotations \\ name)
          result match {
//            case JObject(List(("allele_num",JInt(_)), ("allele_num",JInt(_)), ("allele_num",JInt(_)), ("allele_num",JInt(_)), ("allele_num",JInt(d)))) => 5
            case _ => (c.value.annotations \\ name).extract[Int]
//            case _ => 5
          }
        }
      )
      case TDouble => Field(
        name,
        FloatType,
        toGraphQLDescription(attrs),
        resolve = c => {
          val result = (c.value.annotations \\ name).extract[Double]
          if (result == null) 0.0 else result
        }
      )
      case TArray(elementType) => {
        elementType match {
          case TInt =>
            Field(
              name,
              ListType(IntType),
              toGraphQLDescription(attrs),
              resolve = c => (c.value.annotations \\ name).extract[ArrayBuffer[Int]]
            )
          case TDouble =>
            Field(
              name,
              ListType(FloatType),
              toGraphQLDescription(attrs),
              resolve = c => (c.value.annotations \\ name).extract[ArrayBuffer[Double]]
            )
          case TString =>
            Field(
              name,
              ListType(StringType),
              toGraphQLDescription(attrs),
              resolve = c => {
                // println(c)
                (c.value.annotations \\ name).extract[ArrayBuffer[String]]
              }
            )
//         case TStruct(hailFields) =>
//           Field(
//             name,
//             ListType(
//               ObjectType(
//                 name,
//                 hailFields.map(f => {
//                   Field(
//                     f.name,
//                     StringType,
//                     toGraphQLDescription(f.attrs),
//                     resolve = ctx[Unit, JObject] => Seq(ctx.extract)
//                   )
//                 }).toList
//               )
//             ),
//             toGraphQLDescription(attrs),
//             resolve = ctx => Seq(ctx.value)
//           )
//           case TStruct(hailFields) => {
////             hailFields.foreach(println)
//             val fields = hailFields.map(f => toGraphQLField(f)).toList
////             println(fields)
//             Field(
//               name,
//               ListType(ObjectType(name, fields)),
//               toGraphQLDescription(attrs),
//               resolve = (c) => {
// //                println(c)
//                 Seq(c.value)
//               }
//             )
//           }
          case _ => Field(name, StringType , toGraphQLDescription(attrs), resolve = (ctx) => "nothing to see here")
        }
      }
      case TStruct(hailFields) => {
        Field(name, ObjectType(name, hailFields.map(f => toGraphQLField(f)).toList), toGraphQLDescription(attrs), resolve = (ctx) => ctx.value)
      }
      case _ => Field(name, StringType , toGraphQLDescription(attrs), resolve = (ctx) => "nothing to see here")
    }
    gqlfield
  }

  def toGraphQLDescription(attrs: Map[String, String]): Option[String] = attrs.get("Description")

  def makeGraphQLVariantSchema(vaSignature: HailType, a: String): List[Field[GnomadDatabase, GnomadVariant]]  = {
    val Some(field) = vaSignature.fieldOption(List(a))
    val fields = toGraphQLField(field)
    List(fields)
  }
}
