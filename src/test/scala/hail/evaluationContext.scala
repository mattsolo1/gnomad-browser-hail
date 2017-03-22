package expressionContext

import org.scalatest._
import is.hail.HailContext
import is.hail.expr.{TString, TDouble, TSet, TInt, TArray, TDict, TVariant, TGenotype, TStruct, TCall, Parser, EvalContext}
import is.hail.annotations.Annotation
import is.hail.variant.{Genotype, Call}

import scala.collection.mutable.ArrayBuffer

class EvaluationContextTest extends FlatSpec with Matchers {

  val hc = HailContext()

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

  "EvalContext" should "take more complex types" in {

    val symbolTable = Map(
      "name" -> (0, TString),
      "gs" -> (1, TStruct(
        ("noCall", TGenotype),
        ("homRef", TGenotype),
        ("het", TGenotype),
        ("homVar", TGenotype),
        ("hetNonRef35", TGenotype)
      )),
      "isaset" -> (2, TSet(TInt)),
      "genedict" -> (3, TDict(TString, TInt)),
      "structArray" -> (4, TArray(
        TStruct(
          ("f1", TInt),
          ("f2", TString),
          ("f3", TInt)
        )
      )),
      "calls" -> (5, TStruct(
        ("noCall", TCall),
        ("homRef", TCall),
        ("het", TCall),
        ("homVar", TCall)
      ))
    )

    val evaluationContext = EvalContext(symbolTable)
    val arrayBuffer = evaluationContext.a

    arrayBuffer(0) = "Matthew"
    arrayBuffer(1) = Annotation(
      Genotype(),
      Genotype(gt = Some(0)),
      Genotype(gt = Some(1)),
      Genotype(gt = Some(2)),
      Genotype(gt = Some(Genotype.gtIndex(3, 5)))
    )
    arrayBuffer(2) = Set(0, 1, 2)
    arrayBuffer(3) = Map("gene1" -> 2, "gene2" -> 10, "gene3" -> 14)
    arrayBuffer(4) = IndexedSeq(
      Annotation(1, "A", 2),
      Annotation(9, "B", 3),
      Annotation(2, "C", 7)
    )
    arrayBuffer(5) = Annotation(null, Call(0), Call(1), Call(2))
    // println(arrayBuffer)
    // ArrayBuffer(Matthew, [./.:.:.:.:PL=.,0/0:.:.:.:PL=.,0/1:.:.:.:PL=.,1/1:.:.:.:PL=.,3/5:.:.:.:PL=.], Set(0, 1, 2), Map(gene1 -> 2, gene2 -> 10, gene3 -> 14), Vector([1,A,2], [9,B,3], [2,C,7]), [null,0,1,2])
  }

  it should "Use vaSignature as in the symbol table" in {
    val gnomad_exomes_PCSK9 = "src/test/resources/gnomad.exomes.r2.0.1.sites.PCSK9.vds"
    val vds = hc.read(gnomad_exomes_PCSK9)
    val vas = vds.vaSignature
    val variantSymbolTable = Map(
      "v" -> (0, TVariant),
      "va" -> (1, vas)
    )
    val variantEvaluationContext = EvalContext(variantSymbolTable)
    println(variantEvaluationContext.st)
    println(variantEvaluationContext.a)
    // val (variantNames, variantTypes, variantFunction) = Parser.variantEvaluationContext()
  }

}
