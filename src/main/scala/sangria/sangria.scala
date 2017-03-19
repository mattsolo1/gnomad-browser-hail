package sangriaserver

import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import sangria.execution.deferred.DeferredResolver
import sangria.parser.QueryParser
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.marshalling.sprayJson._
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.schema.{Field, _}
import spray.json._

import is.hail.HailContext
import is.hail.annotations._
import is.hail.expr._
// import is.hail.utils._
import is.hail.variant.{VariantDataset, Genotype, Locus, Variant}

// case class SangriaVariant(variant_name: String, variant_id: String, rsid: String)
//
// class DataContext(data: List[(Variant, (Annotation, Iterable[Genotype]))]) {
//
//   val sangriaVariants: List[SangriaVariant] = data.map {
//     case (v, (va, gs)) => SangriaVariant(
//       variant_name = v.toString,
//       variant_id = v.toString,
//       rsid = "nothing"
//     )
//   }
//
//   def getAllVariants(): List[SangriaVariant] = { sangriaVariants }
//
//   def getRealVariant(variant_id: String): Option[SangriaVariant] =
//     sangriaVariants.find(variant => variant.variant_id == variant_id)
// }
//
// object GnomadSchema {
//   val SangriaVariantType = ObjectType("SangriaVariant", fields[Unit, SangriaVariant](
//     Field("variant_name",
//       StringType,
//       Some("The name of the variant"),
//       resolve = _.value.variant_name),
//     Field("rsid",
//       StringType,
//       Some("rsid"),
//       resolve = _.value.rsid),
//     Field("variant_id",
//       StringType,
//       Some("The variant id"),
//       resolve = _.value.variant_id)
//   ))
//
//   val Query = ObjectType("Query", fields[DataContext, Unit](
//     Field("variants", OptionType())
//   ))
//
//   val gSchema = Schema(SangriaVariantType)
// }

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

object GnomadSchema {
  val GeneType = ObjectType(
    "Gene", fields[Unit, Gene](
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

object Server extends App {

  def run(hc: HailContext, vds: VariantDataset, address: String, port: Int) = {

    // val data: List[(Variant, (Annotation, Iterable[Genotype]))] = vds.rdd.collect().toList

    implicit val system = ActorSystem("sangria-server")
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val route: Route =
      (post & path("graphql")) {
        entity(as[JsValue]) { requestJson ⇒
          val JsObject(fields) = requestJson
          val JsString(query) = fields("query")

          val operation = fields.get("operationName") collect {
            case JsString(op) ⇒ op
          }

          val vars = fields.get("variables") match {
            case Some(obj: JsObject) ⇒ obj
            case _ ⇒ JsObject.empty
          }

          QueryParser.parse(query) match {

            // query parsed successfully, time to execute it!
            case Success(queryAst) ⇒
              complete(Executor.execute(GnomadSchema.GnomadSchema, queryAst, new GnomadDatabase(vds),
                variables = vars,
                operationName = operation)
                // deferredResolver = DeferredResolver.fetchers(SchemaDefinition.characters))
                .map(OK → _)
                .recover {
                  case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
                  case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
                })
          }
        }
      } ~
        get {
          getFromResource("graphiql.html")
        }

    Http().bindAndHandle(route, address, sys.props.get("http.port").fold(port)(_.toInt))
  }
  val hc = HailContext()
  val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
  val vds = hc.read(vdsPath)
  println("Starting server")
  run(hc, vds, "0.0.0.0", 8004)
}
