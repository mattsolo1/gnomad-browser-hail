// https://github.com/scalaj/scalaj-http

import org.scalatest._

import scalaj.http.{HttpResponse, Http}

import org.json4s.jackson.JsonMethods
import org.json4s.{JObject, JArray, JInt, JString, JDecimal, JField}

class ScalaHttp extends FlatSpec with Matchers {

  "Simple HTTP get" should "fetch expected data" in {
    val response: HttpResponse[String] = Http("https://www.reddit.com/r/all.json").asString
    response.body.isInstanceOf[String] should be (true)
    response.headers("Status") should be (Vector("HTTP/1.1 200 OK"))
  }

  "HTTP post" should "fetch data from graphql endpoint" in {
    val url = "http://gnomad-api.broadinstitute.org"
    val query = """{
      gene(gene_name: "PCSK9") {
        _id
        stop
        gene_id
        chrom
        start
        xstop
        xstart
        gene_name
      }
    }"""

    val headers = Seq(("content-type", "application/graphql"), ("accept", "application/json"))
    val response = Http(url).postData(query).headers(headers).asString

    response.body.isInstanceOf[String] should be (true)
    response.headers("Status") should be (Vector("HTTP/1.1 200 OK"))
  }

  "Response" should "be parsed by json4s" in {
    val url = "http://gnomad-api.broadinstitute.org"
    val query = """{
      gene(gene_name: "PCSK9") {
        gene_id
        gene_name
        chrom
        start
        stop
      }
    }"""

    val headers = Seq(("content-type", "application/graphql"), ("accept", "application/json"))
    val response = Http(url).postData(query).headers(headers).asString
    val json = response.body
    val parsed = JsonMethods.parse(json, useBigDecimalForDouble = true)
    val expected = JObject(
      List(("data", JObject(("gene", JObject(List(
        ("gene_id", JString("ENSG00000169174")),
        ("gene_name", JString("PCSK9")),
        ("chrom", JString("1")),
        ("start", JInt(55505222)),
        ("stop", JString("55530526"))
      )))))))
    parsed should be (expected)
  }

  "Json response" should "be queried using json4 and for-comprehensions" in {
    val url = "http://gnomad-api.broadinstitute.org"
    val query = """{
      gene(gene_name: "PCSK9") {
        transcript {
          exons {
            _id
            start
            transcript_id
            feature_type
            strand
            stop
            xstart
            chrom
            gene_id
            xstop
          }
        }
      }
    }"""

    val headers = Seq(("content-type", "application/graphql"), ("accept", "application/json"))
    val response = Http(url).postData(query).headers(headers).asString
    val json = response.body
    val parsed = JsonMethods.parse(json, useBigDecimalForDouble = true)
    val features = for {
      JObject(data) <- parsed
      JField("feature_type", JString(feature)) <- data
    } yield feature
    features.size should be (26)
    val featureCounts = features.groupBy(identity).mapValues(_.size)
    val expected = Map("CDS" -> 12, "exon" -> 12, "UTR" -> 2)
    featureCounts should be (expected)
  }

  "json AST" should "be queried using XPath-like functions" in {
    val url = "http://gnomad-api.broadinstitute.org"
    val query = """{
      gene(gene_name: "PCSK9") {
        gene_name
        start
        stop
        transcript {
          exons {
            _id
            start
            feature_type
            strand
            stop
            chrom
          }
        }
      }
    }"""
    val headers = Seq(("content-type", "application/graphql"), ("accept", "application/json"))
    val response = Http(url).postData(query).headers(headers).asString
    val json = JsonMethods.parse(response.body, useBigDecimalForDouble = true)

    val geneName = json \\ "gene_name"
    geneName should be (JString("PCSK9"))

    val featureType = json \\ "feature_type"
    featureType.isInstanceOf[JObject] should be (true)
    featureType.children.size should be (26)
  }

  "json AST" should "have values extracted by case classes" in {
  }
}
