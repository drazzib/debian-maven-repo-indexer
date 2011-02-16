package org.debian.maven.indexer;

// Copyright 2009 Damian Bradicich
// Copyright 2010-2011 Damien Raude-Morvan
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.search.Query;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.Field;
import org.sonatype.nexus.index.Grouping;
import org.sonatype.nexus.index.SearchType;

/**
 * @author Damian Bradicich
 * @author Damien Raude-Morvan
 */
public interface MavenRepoIndexer {
    void index() throws IOException;

    Set<ArtifactInfo> searchIndexFlat(Field field, String query, SearchType type)
	    throws IOException;

    Set<ArtifactInfo> searchIndexFlat(Query query) throws IOException;

    Map<String, ArtifactInfoGroup> searchIndexGrouped(Field field,
	    String query, SearchType type) throws IOException;

    Map<String, ArtifactInfoGroup> searchIndexGrouped(Field field,
	    String query, SearchType type, Grouping grouping)
	    throws IOException;

    Map<String, ArtifactInfoGroup> searchIndexGrouped(Query q, Grouping grouping)
	    throws IOException;

    void publishIndex(File targetDirectory) throws IOException;
}
