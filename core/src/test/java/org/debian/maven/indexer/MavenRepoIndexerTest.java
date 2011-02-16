package org.debian.maven.indexer;

// Copyright 2009 Damian Bradicich
// Copyright 2010 Damien Raude-Morvan
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
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.context.Context;
import org.debian.maven.indexer.creators.DEBIAN;
import org.debian.maven.indexer.creators.DebianIndexCreator;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.ArtifactInfoGroup;
import org.sonatype.nexus.index.MAVEN;
import org.sonatype.nexus.index.SearchType;
import org.sonatype.nexus.index.search.grouping.AbstractGrouping;

public class MavenRepoIndexerTest extends PlexusTestCase {
    private MavenRepoIndexer app;

    @Override
    protected void customizeContext(Context context) {
	super.customizeContext(context);

	context.put("repository.path", "/usr/share/maven-repo/");
	context.put("index.path", "target/indexOutput");
    }

    @Override
    protected void setUp() throws Exception {
	super.setUp();

	app = lookup(MavenRepoIndexer.class);
    }

    public void testAddIndexContext() throws Exception {
	app.index();

	Set<ArtifactInfo> artifacts = app.searchIndexFlat(MAVEN.ARTIFACT_ID,
		"*", SearchType.EXACT);

	assertNotNull("returned artifacts is null", artifacts);
	assertFalse("returned artifacts is empty", artifacts.isEmpty());
    }

    public void testSearch() throws Exception {
	app.index();

	Set<ArtifactInfo> artifacts = app.searchIndexFlat(MAVEN.ARTIFACT_ID,
		"commons-pool", SearchType.EXACT);

	assertNotNull("returned artifacts is null", artifacts);
	assertFalse("returned artifacts is empty", artifacts.isEmpty());
	assertEquals("returned artifacts has more than 2 entry", 2, artifacts
		.size());
	assertEquals("returned artifact not correct item", "commons-pool",
		artifacts.iterator().next().artifactId);
    }

    public void testSampleSearch() throws Exception {
	app.index();

	Set<ArtifactInfo> artifacts = app.searchIndexFlat(
		DEBIAN.DEBIAN_PACKAGE, "libxalan2-java", SearchType.SCORED);

	assertNotNull("returned artifacts is null", artifacts);
	assertFalse("returned artifacts is empty", artifacts.isEmpty());

	for (ArtifactInfo ai : artifacts) {
	    assertEquals("returned artifact has invalid data",
		    "libxalan2-java", ai.getAttributes().get(
			    DebianIndexCreator.FLD_DEBIAN_PACKAGE.getKey()));
	}
    }

    public void testNegativeSampleSearch() throws Exception {
	app.index();

	Set<ArtifactInfo> artifacts = app.searchIndexFlat(
		DEBIAN.DEBIAN_PACKAGE, "invalid", SearchType.SCORED);

	assertNotNull("returned artifacts is null", artifacts);
	assertTrue("returned artifacts should be empty", artifacts.isEmpty());
    }

    public void testSampleSearchWithPrefixQuery() throws Exception {
	app.index();

	// This type of query will be totally built outside of nexus indexer,
	// and will not
	// be tied to constraints defined in
	// http://svn.sonatype.org/nexus/trunk/nexus-indexer/src/main/java/org/sonatype/nexus/index/DefaultQueryCreator.java

	// A PrefixQuery will look for any documents containing the MY_FIELD
	// term that starts with val
	Query q = new PrefixQuery(new Term(
		DebianIndexCreator.FLD_DEBIAN_PACKAGE.getKey(), "libuima"));

	Set<ArtifactInfo> artifacts = app.searchIndexFlat(q);

	assertNotNull("returned artifacts is null", artifacts);
	assertFalse("returned artifacts is empty", artifacts.isEmpty());

	for (ArtifactInfo ai : artifacts) {
	    assertTrue("returned artifact has invalid data", ai.getAttributes()
		    .get(DebianIndexCreator.FLD_DEBIAN_PACKAGE.getKey())
		    .startsWith("libuima"));
	}
    }

    public void testNegativeSampleSearchWithPrefixQuery() throws Exception {
	app.index();

	Query q = new PrefixQuery(new Term(
		DebianIndexCreator.FLD_DEBIAN_PACKAGE.getKey(), "vrz"));

	Set<ArtifactInfo> artifacts = app.searchIndexFlat(q);

	assertNotNull("returned artifacts is null", artifacts);
	assertTrue("returned artifacts should be empty", artifacts.isEmpty());
    }

    public void testSampleSearchGroup() throws Exception {
	app.index();

	Map<String, ArtifactInfoGroup> groupedArtifacts = app
		.searchIndexGrouped(DEBIAN.DEBIAN_PACKAGE, "libtiles-java",
			SearchType.SCORED);

	assertNotNull("returned groupedArtifacts is null", groupedArtifacts);
	assertFalse("returned groupedArtifacts should not be empty",
		groupedArtifacts.isEmpty());

	for (ArtifactInfoGroup artifactGroup : groupedArtifacts.values()) {
	    String[] parts = artifactGroup.getGroupKey().split(":");
	    // 1st part groupId
	    // 2nd part artifactId
	    // 3rd part version
	    // 4th part classifier
	    assertEquals("should be 4 parts to the group key", 4, parts.length);
	    assertFalse("each group should contain at least 1 artifact",
		    artifactGroup.getArtifactInfos().isEmpty());
	}
    }

    public void testSampleSearchGroupNewGrouping() throws Exception {
	app.index();

	// Search using my own grouping, which will group based upon the
	// MY_FIELD parameter
	Map<String, ArtifactInfoGroup> groupedArtifacts = app
		.searchIndexGrouped(DEBIAN.DEBIAN_PACKAGE, "libtiles-java",
			SearchType.SCORED, new AbstractGrouping() {
			    @Override
			    protected String getGroupKey(
				    ArtifactInfo artifactInfo) {
				return artifactInfo.getAttributes().get(
					DebianIndexCreator.FLD_DEBIAN_PACKAGE
						.getKey());
			    }
			});

	assertNotNull("returned groupedArtifacts is null", groupedArtifacts);
	assertEquals("returned groupedArtifacts should have 1 entry", 1,
		groupedArtifacts.size());
	assertEquals("group key should be value", "libtiles-java",
		groupedArtifacts.values().iterator().next().getGroupKey());
    }

    public void testIndexPacking() throws Exception {
	app.index();

	File publishDir = new File(getBasedir(), "target/publish/");

	app.publishIndex(publishDir);

	assertTrue(publishDir.exists());

	// Legacy index format
	assertTrue(new File(publishDir, "nexus-maven-repository-index.zip")
		.exists());
	assertTrue(new File(publishDir, "nexus-maven-repository-index.zip.sha1")
		.exists());
	assertTrue(new File(publishDir, "nexus-maven-repository-index.zip.md5")
		.exists());

	// Current index format
	assertTrue(new File(publishDir, "nexus-maven-repository-index.gz")
		.exists());
	assertTrue(new File(publishDir, "nexus-maven-repository-index.gz.sha1")
		.exists());
	assertTrue(new File(publishDir, "nexus-maven-repository-index.gz.md5")
		.exists());

	// properties file
	assertTrue(new File(publishDir,
		"nexus-maven-repository-index.properties").exists());
	assertTrue(new File(publishDir,
		"nexus-maven-repository-index.properties.sha1").exists());
	assertTrue(new File(publishDir,
		"nexus-maven-repository-index.properties.md5").exists());
    }
}
