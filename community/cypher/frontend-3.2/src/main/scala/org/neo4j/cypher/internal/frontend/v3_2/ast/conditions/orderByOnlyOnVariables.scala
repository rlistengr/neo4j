/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.frontend.v3_2.ast.conditions

import org.neo4j.cypher.internal.frontend.v3_2.ast.{OrderBy, SortItem, Variable}
import org.neo4j.cypher.internal.frontend.v3_2.helpers.rewriting.Condition

case object orderByOnlyOnVariables extends Condition {
  def apply(that: Any): Seq[String] = {
    val orderBys = collectNodesOfType[OrderBy].apply(that)
    orderBys.flatMap { orderBy =>
      orderBy.sortItems.collect {
        case item: SortItem if !item.expression.isInstanceOf[Variable] =>
          s"OrderBy at ${orderBy.position} is ordering on an expression (${item.expression}) instead of a variable"
      }
    }
  }

  override def name: String = productPrefix
}
