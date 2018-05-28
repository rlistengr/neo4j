/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) with the
 * Commons Clause, as found in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * Neo4j object code can be licensed independently from the source
 * under separate terms from the AGPL. Inquiries can be directed to:
 * licensing@neo4j.com
 *
 * More information is also available at:
 * https://neo4j.com/licensing/
 */
package org.neo4j.backup;

import java.io.File;

import org.neo4j.consistency.ConsistencyCheckService;
import org.neo4j.consistency.checking.full.CheckConsistencyConfig;
import org.neo4j.consistency.checking.full.ConsistencyCheckIncompleteException;
import org.neo4j.helpers.progress.ProgressMonitorFactory;
import org.neo4j.io.fs.FileSystemAbstraction;
import org.neo4j.io.pagecache.PageCache;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.logging.LogProvider;

interface ConsistencyCheck
{
    ConsistencyCheck NONE =
            new ConsistencyCheck()
            {
                @Override
                public String name()
                {
                    return "none";
                }

                @Override
                public boolean runFull( File storeDir, Config tuningConfiguration,
                        ProgressMonitorFactory progressFactory, LogProvider logProvider, FileSystemAbstraction fileSystem, PageCache pageCache, boolean verbose,
                        CheckConsistencyConfig checkConsistencyConfig )
                        throws ConsistencyCheckFailedException
                {
                    return true;
                }
            };

    ConsistencyCheck FULL =
            new ConsistencyCheck()
            {
                @Override
                public String name()
                {
                    return "full";
                }

                @Override
                public boolean runFull( File storeDir, Config tuningConfiguration,
                        ProgressMonitorFactory progressFactory, LogProvider logProvider,
                        FileSystemAbstraction fileSystem, PageCache pageCache, boolean verbose,
                        CheckConsistencyConfig checkConsistencyConfig ) throws ConsistencyCheckFailedException
                {
                    try
                    {
                        return new ConsistencyCheckService().runFullConsistencyCheck( storeDir, tuningConfiguration,
                                progressFactory, logProvider, fileSystem, pageCache, verbose, checkConsistencyConfig )
                                .isSuccessful();
                    }
                    catch ( ConsistencyCheckIncompleteException e )
                    {
                        throw new ConsistencyCheckFailedException( e );
                    }
                }
            };

    String name();

    boolean runFull( File storeDir, Config tuningConfiguration, ProgressMonitorFactory progressFactory,
            LogProvider logProvider, FileSystemAbstraction fileSystem, PageCache pageCache, boolean verbose,
            CheckConsistencyConfig checkConsistencyConfig )
            throws ConsistencyCheckFailedException;

    String toString();

    static ConsistencyCheck fromString( String name )
    {
        for ( ConsistencyCheck consistencyCheck : new ConsistencyCheck[]{NONE, FULL} )
        {
            if ( consistencyCheck.name().equalsIgnoreCase( name ) )
            {
                return consistencyCheck;
            }
        }
        throw new IllegalArgumentException( "Unknown consistency check name: " + name +
                ". Supported values: NONE, FULL" );
    }

    static ConsistencyCheck full( File reportDir, ConsistencyCheckService consistencyCheckService )
    {
        return new ConsistencyCheck()
        {
            @Override
            public String name()
            {
                return "full";
            }

            @Override
            public boolean runFull( File storeDir, Config tuningConfiguration, ProgressMonitorFactory progressFactory,
                    LogProvider logProvider, FileSystemAbstraction fileSystem, PageCache pageCache, boolean verbose,
                    CheckConsistencyConfig checkConsistencyConfig )
                    throws ConsistencyCheckFailedException
            {
                try
                {
                    return consistencyCheckService
                            .runFullConsistencyCheck( storeDir, tuningConfiguration, progressFactory, logProvider,
                                    fileSystem, pageCache, verbose, reportDir, checkConsistencyConfig ).isSuccessful();
                }
                catch ( ConsistencyCheckIncompleteException e )
                {
                    throw new ConsistencyCheckFailedException( e );
                }
            }
        };
    }
}
