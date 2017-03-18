package expressionContext

import org.scalatest._
import is.hail.HailContext
import is.hail.expr.{TInt, TDouble, TString, EvalContext}
import scala.collection.mutable.ArrayBuffer

class ExpressionContextTest extends FlatSpec with Matchers {
  "EvalContext" should "take a symbol table, return an empty array buffer you can fill with values" in {

    val symbolTable = Map(
      "meaning"-> (0, TInt),
      "pi" -> (1, TDouble),
      "name" -> (2, TString)
    )

    val evaluationContext = EvalContext(symbolTable)

    evaluationContext.st should be (
      Map(
        "meaning" -> (0, TInt),
        "pi" -> (1, TDouble),
        "name" -> (2, TString)
      )
    )

    val arrayBuffer = evaluationContext.a

    arrayBuffer should be (ArrayBuffer(null, null, null))

    arrayBuffer(0) = 42
    arrayBuffer(1) = 3.14
    arrayBuffer(2) = "Matthew"

    arrayBuffer should be (ArrayBuffer(42, 3.14, "Matthew"))
  }
}
