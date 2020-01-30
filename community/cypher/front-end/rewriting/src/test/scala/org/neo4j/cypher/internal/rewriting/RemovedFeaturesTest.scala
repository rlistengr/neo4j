/*
 * Copyright (c) 2002-2020 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.cypher.internal.rewriting

import org.neo4j.cypher.internal.ast.AstConstructionTestSupport
import org.neo4j.cypher.internal.expressions._
import org.neo4j.cypher.internal.rewriting.rewriters.replaceAliasedFunctionInvocations
import org.neo4j.cypher.internal.util.symbols
import org.neo4j.cypher.internal.util.test_helpers.CypherFunSuite

class RemovedFeaturesTest extends CypherFunSuite with AstConstructionTestSupport {

  private val rewriter4_0 = replaceAliasedFunctionInvocations(Deprecations.removedFeaturesIn4_0)
  private val deprecatedNameMap4_0 = Deprecations.removedFeaturesIn4_0.removedFunctionsRenames

  private val rewriter4_1 = replaceAliasedFunctionInvocations(Deprecations.removedFeaturesIn4_1)
  private val deprecatedNameMap4_1 = Deprecations.removedFeaturesIn4_1.removedFunctionsRenames

  test("should rewrite removed function names regardless of casing") {
    for (deprecatedMap <- Seq(deprecatedNameMap4_0, deprecatedNameMap4_1)) {
      for ((oldName, newName) <- deprecatedMap) {
        rewriter4_0(function(oldName, varFor("arg"))) should equal(function(oldName, varFor("arg")).copy(functionName = FunctionName(newName)(pos))(pos))
        rewriter4_0(function(oldName.toLowerCase(), varFor("arg"))) should equal(function(newName, varFor("arg")))
        rewriter4_0(function(oldName.toUpperCase(), varFor("arg"))) should equal(function(newName, varFor("arg")))
      }
    }
  }

  test("should not touch new function names of regardless of casing") {
    for (deprecatedMap <- Seq(deprecatedNameMap4_0, deprecatedNameMap4_1)) {
      for (newName <- deprecatedMap.values) {
        rewriter4_0(function(newName, varFor("arg"))) should equal(function(newName, varFor("arg")))
        rewriter4_0(function(newName.toLowerCase(), varFor("arg"))) should equal(function(newName, varFor("arg")))
        rewriter4_0(function(newName.toUpperCase(), varFor("arg"))) should equal(function(newName, varFor("arg")))
      }
    }
  }

  test("should rewrite length of strings and collections to size regardless of casing") {
    val str = literalString("a string")
    val list = listOfInt(1, 2, 3)

    for (lengthFunc <- Seq("length", "LENGTH", "leNgTh")) {
      rewriter4_0(function(lengthFunc, str)) should equal(function("size", str))
      rewriter4_0(function(lengthFunc, list)) should equal(function("size", list))
    }
  }

  test("should rewrite filter to list comprehension") {
    val x = varFor("x")
    val list = listOfString("a", "aa", "aaa")
    val predicate = startsWith(x, literalString("aa"))

    // filter(x IN ["a", "aa", "aaa"] WHERE x STARTS WITH "aa") -> [x IN ["a", "aa", "aaa"] WHERE x STARTS WITH "aa"]
    val before = FilterExpression(x, list, Some(predicate))(pos)
    val after = listComprehension(x, list, Some(predicate), None)
    rewriter4_0(before) should equal(after)
  }

  test("should rewrite extract to list comprehension") {
    val x = varFor("x")
    val list = listOfString("a", "aa", "aaa")
    val extractExpression = function("size", x)

    // extract(x IN ["a", "aa", "aaa"] | size(x)) -> [x IN ["a", "aa", "aaa"] | size(x)]
    val before = ExtractExpression(x, list, None, Some(extractExpression))(pos)
    val after = listComprehension(x, list, None, Some(extractExpression))
    rewriter4_0(before) should equal(after)
  }

  test("should rewrite old parameter syntax") {
    val before = ParameterWithOldSyntax("param", symbols.CTString)(pos)

    val after = parameter("param", symbols.CTString)
    rewriter4_0(before) should equal(after)
  }

  test("4.1 rewriter should not rewrite things removed in 4.0") {
    val oldParam = ParameterWithOldSyntax("param", symbols.CTString)(pos)
    rewriter4_1(oldParam) should equal(oldParam)
  }
}