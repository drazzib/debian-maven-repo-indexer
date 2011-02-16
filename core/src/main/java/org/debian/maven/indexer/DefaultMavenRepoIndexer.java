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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.lucene.search.Query;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Configuration;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.Field;
import org.sonatype.nexus.index.FlatSearchRequest;
import org.sonatype.nexus.index.FlatSearchResponse;
import org.sonatype.nexus.index.GroupedSearchRequest;
import org.sonatype.nexus.index.GroupedSearchResponse;
import org.sonatype.nexus.index.Grouping;
import org.sonatype.nexus.index.NexusIndexer;
import org.sonatype.nexus.index.SearchType;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.context.IndexingContext;
import org.sonatype.nexus.index.context.UnsupportedExistingLuceneIndexException;
import org.sonatype.nexus.index.packer.IndexPacker;
import org.sonatype.nexus.index.packer.IndexPackingRequest;
import org.sonatype.nexus.index.search.grouping.GAVGrouping;

/**
 * Sample app to show how to integrate with the nexus indexer. Note that this is
 * a simple plexus component extending the SampleApp interface
 * 
 * @author Damian Bradicich
 * @author Damien Raude-Morvan
 */
@Component(role = MavenRepoIndexer.class)
public class DefaultMavenRepoIndexer implements MavenRepoIndexer,
	Initializable, Disposable {
    // The nexus indexer
    @Requirement
    private NexusIndexer indexer;

    // The nexus index packer
    @Requirement
    private IndexPacker indexPacker;

    // The list of index creators we will be using (all of them)
    @Requirement(role = IndexCreator.class)
    private List<IndexCreator> indexCreators;

    // The indexing context
    private IndexingContext context = null;

    // The path to the repository to index, value will be pulled from
    // the plexus context
    @Configuration(value = "${repository.path}")
    private File repositoryDirectoryPath;

    // The path to store index files, value will be pulled from
    // the plexus context
    @Configuration(value = "${index.path}")
    private File indexDirectoryPath;

    // Initialize the index context
    @Override
    public void initialize() throws InitializationException {
	try {
	    // Add the indexing context
	    context = indexer.addIndexingContext(
	    // id of the context
		    "debian",
		    // id of the repository
		    "debianMavenRepo",
		    // directory containing repository
		    repositoryDirectoryPath,
		    // directory where index will be stored
		    indexDirectoryPath,
		    // remote repository url...not in this example
		    null,
		    // index update url...not in this example
		    null,
		    // list of index creators
		    indexCreators);
	} catch (UnsupportedExistingLuceneIndexException e) {
	    throw new InitializationException(
		    "Error initializing IndexingContext", e);
	} catch (IOException e) {
	    throw new InitializationException(
		    "Error initializing IndexingContext", e);
	}
    }

    // clean up the context
    @Override
    public void dispose() {
	if (context != null) {
	    // Remove the index files, typically would not want to remove the
	    // index files, so
	    // would pass in false, but this is just a test app...
	    try {
		indexer.removeIndexingContext(context, true);
	    } catch (IOException ex) {
		Logger.getLogger(DefaultMavenRepoIndexer.class.getName()).log(Level.SEVERE, null, ex);
	    }
	}
    }

    // index the repository
    @Override
    public void index() throws IOException {
	// Perform the scan, which will index all artifacts in the repository
	// directory
	// once this is done, searching will be available
	indexer.scan(context);
    }

    // search for artifacts
    @Override
    public Set<ArtifactInfo> searchIndexFlat(Field field, String value,
	    SearchType type) throws IOException {
	// Build a query that will search the documents for the field set to the
	// supplied value
	// This uses predefined logic to define the query
	// See
	// http://svn.sonatype.org/nexus/trunk/nexus-indexer/src/main/java/org/sonatype/nexus/index/DefaultQueryCreator.java
	// for details
	Query query = indexer.constructQuery(field, value, type);

	return searchIndexFlat(query);
    }

    // search for artifacts using pre-built query
    @Override
    public Set<ArtifactInfo> searchIndexFlat(Query query) throws IOException {
	// Build the request
	FlatSearchRequest request = new FlatSearchRequest(query);

	// Perform the search
	FlatSearchResponse response = indexer.searchFlat(request);

	// Return the artifact info objects
	return response.getResults();
    }

    @Override
    public Map<String, ArtifactInfoGroup> searchIndexGrouped(Field field,
	    String value, SearchType type) throws IOException {
	// We will simply use the GAV grouping, meaning that each
	// groupId/artifactId/version/classifier
	// will have its own entry in the returned map
	return searchIndexGrouped(field, value, type, new GAVGrouping());
    }

    @Override
    public Map<String, ArtifactInfoGroup> searchIndexGrouped(Field field,
	    String value, SearchType type, Grouping grouping)
	    throws IOException {
	// Build a query that will search the documents for the field set to the
	// supplied value This uses predefined logic to define the query
	// See
	// http://svn.sonatype.org/nexus/trunk/nexus-indexer/src/main/java/org/sonatype/nexus/index/DefaultQueryCreator.java
	// for details
	Query query = indexer.constructQuery(field, value, type);

	return searchIndexGrouped(query, grouping);
    }

    @Override
    public Map<String, ArtifactInfoGroup> searchIndexGrouped(Query q,
	    Grouping grouping) throws IOException {
	GroupedSearchRequest request = new GroupedSearchRequest(q, grouping);

	GroupedSearchResponse response = indexer.searchGrouped(request);

	return response.getResults();
    }

    @Override
    public void publishIndex(File targetDirectory) throws IOException {
	IndexPackingRequest packReq = new IndexPackingRequest(context,
		targetDirectory);
	packReq.setCreateChecksumFiles(true);
	packReq.setCreateIncrementalChunks(true);

	// NOTE: There are numerous other options you can set in the index pack
	// request

	indexPacker.packIndex(packReq);
    }
}
