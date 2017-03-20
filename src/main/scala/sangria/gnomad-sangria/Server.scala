package gnomadsangria

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
import spray.json._

import scala.util.{Failure, Success}

import is.hail.HailContext
import is.hail.variant.{VariantDataset}

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
              complete(Executor.execute(SchemaDefinition.GnomadSchema, queryAst, new GnomadDatabase(vds),
                variables = vars,
                operationName = operation)
                // deferredResolver = DeferredResolver.fetchers(SchemaDefinition.characters))
                .map(OK → _)
                .recover {
                  case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
                  case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
                })
            case Failure(error) ⇒
              complete(BadRequest, JsObject("error" → JsString(error.getMessage)))
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
