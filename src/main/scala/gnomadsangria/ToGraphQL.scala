package gnomadsangria

import org.apache.spark.sql.Row
import is.hail.expr.{Field => HailField, Type => HailType, _}
import is.hail.annotations.Annotation
import sangria.schema._

import scala.collection.mutable.ArrayBuffer
import org.json4s._
import gnomadutils.VdsVariant
import is.hail.variant.VariantDataset

object ToGraphQL {
  def toGraphQLDescription(attrs: Map[String, String]): Option[String] = attrs.get("Description")

  def getAnnotationValues(name: String, va: Annotation, hailType: HailType): Tuple2[String, Any] = {
    hailType match {
      case TBoolean => (name, va.asInstanceOf[Boolean])
      case TStruct(fields) =>
        val row = va.asInstanceOf[Row]
        (name , fields.map(f => getAnnotationValues(f.name, row.get(f.index), f.typ)).toMap)
      case _  => (name, "nothing yet")
    }
  }

  def hailToGraphQLField(hailField: HailField): Field[GnomadDatabase, VdsVariant] = {
    implicit val formats = DefaultFormats
    val HailField(name, typ, index, attrs) = hailField
    val gqlfield: Field[GnomadDatabase, VdsVariant] = typ match {
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
          val result = (c.value.annotations \\ name)
          result match {
            case _ => (c.value.annotations \\ name).extract[Int]
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
                (c.value.annotations \\ name).extract[ArrayBuffer[String]]
              }
            )
          case _ => Field(name, StringType , toGraphQLDescription(attrs), resolve = (ctx) => "nothing to see here")
        }
      }
      case TStruct(hailFields) => {
        Field(name, ObjectType(name, hailFields.map(f => hailToGraphQLField(f)).toList), toGraphQLDescription(attrs), resolve = (ctx) => ctx.value)
      }
      case _ => Field(name, StringType , toGraphQLDescription(attrs), resolve = (ctx) => "nothing to see here")
    }
    gqlfield
  }

  def makeGraphQLVariantSchema(vds: VariantDataset, a: String): List[Field[GnomadDatabase, VdsVariant]]  = {
    val Some(field) = vds.vaSignature.fieldOption(List(a))
    val fields = hailToGraphQLField(field)
    List(fields)
  }
}