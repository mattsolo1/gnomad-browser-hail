package gnomadsangria

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.{HttpOrigin, HttpOriginRange}
import akka.http.scaladsl.server._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import sangria.execution.deferred.DeferredResolver
import sangria.parser.QueryParser
import sangria.execution.{ErrorWithResolver, Executor, QueryAnalysisError}
import sangria.marshalling.sprayJson._
import spray.json._

import scala.util.{Failure, Success}

import is.hail.HailContext
import is.hail.variant.VariantDataset

object Server{

  def run(hc: HailContext, datasets: List[VariantDataset], address: String, port: Int) = {
    implicit val system = ActorSystem("sangria-server")
    implicit val materializer = ActorMaterializer()
    import system.dispatcher

    val corsSettings = CorsSettings.defaultSettings

    val route: Route =
      (cors(corsSettings) & post & path("graphql")) {
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
              val schemaDef = new SchemaDefinition(datasets)
              complete(Executor.execute(schemaDef.GnomadSchema, queryAst, new GnomadDatabase(datasets),
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

  def main(args: Array[String]) {
    val hc = HailContext()
    // val vdsPath = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
    // val vdsPath = "/Users/msolomon/Data/gnomad/release-170228/gnomad.exomes.r2.0.1.sites.Y.vds"

    val vdsPath1 = args(0)
    val vdsPath2 = args(1)

    val vds1 = hc.read(vdsPath1)
    val vds2 = hc.read(vdsPath2)
    val datasets = List(vds1, vds2)

    datasets.foreach(println)

    println("Starting server")
    run(hc, datasets, "0.0.0.0", 8004)
  }
}
