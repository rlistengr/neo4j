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
package org.neo4j.kernel.impl.util;

import org.neo4j.graphdb.factory.GraphDatabaseSettings;
import org.neo4j.kernel.configuration.Config;

public class CustomIOConfigValidator
{
    public static void assertCustomIOConfigNotUsed( Config config, String message )
    {
        if ( customIOConfigUsed( config ) )
        {
            throw new CustomIOConfigNotSupportedException( message );
        }
    }

    private static boolean customIOConfigUsed( Config config )
    {
        return config.get( GraphDatabaseSettings.pagecache_swapper ) != null;
    }

    private static class CustomIOConfigNotSupportedException extends RuntimeException
    {
        CustomIOConfigNotSupportedException( String message )
        {
            super( message );
        }
    }
}
