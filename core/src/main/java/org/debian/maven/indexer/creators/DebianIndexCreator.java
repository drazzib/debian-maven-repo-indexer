package org.debian.maven.indexer.creators;

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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.nexus.index.ArtifactContext;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.IndexerField;
import org.sonatype.nexus.index.IndexerFieldVersion;
import org.sonatype.nexus.index.context.IndexCreator;
import org.sonatype.nexus.index.creator.AbstractIndexCreator;

/**
 * @author Damien Raude-Morvan
 */
@Component(role = IndexCreator.class, hint = "debian")
public class DebianIndexCreator extends AbstractIndexCreator {

    public static final IndexerField FLD_DEBIAN_PACKAGE = new IndexerField(
	    DEBIAN.DEBIAN_PACKAGE, IndexerFieldVersion.V1, "debp",
	    "Debian Package (tokenized, stored)", Store.YES, Index.TOKENIZED);

    public static final IndexerField FLD_DEBIAN_VERSION = new IndexerField(
	    DEBIAN.DEBIAN_VERSION, IndexerFieldVersion.V1, "debv",
	    "Debian Version (tokenized, stored)", Store.YES, Index.TOKENIZED);

    /**
     * Populate ArtifactInfo with data specific to your application. Note that
     * the artifactContext contains other useful objects, which may come in
     * handy.
     */
    public void populateArtifactInfo(ArtifactContext artifactContext)
	    throws IOException {
	String debianPackage = "";
	String debianVersion = "";

	// Add the data to the ArtifactInfo object, retrieved by whatever means
	// you see fit. you could get details from the artifact file in the
	// context, or the
	// pom or pretty much anything else
	if (artifactContext.getPom() != null) {
	    Xpp3Dom dom = readPomInputStream(new FileInputStream(
		    artifactContext.getPom()));
	    if (dom != null) {
		if (dom.getChild("properties") != null) {
		    Xpp3Dom child = dom.getChild("properties").getChild(
			    "debian.package");
		    if (child != null) {
			debianPackage = child.getValue();
		    }
		    child = dom.getChild("properties").getChild(
			    "debian.originalVersion");
		    if (child != null) {
			debianVersion = child.getValue();
		    }
		}
	    }
	}

	artifactContext.getArtifactInfo().getAttributes().put(
		FLD_DEBIAN_PACKAGE.getKey(), debianPackage);

	artifactContext.getArtifactInfo().getAttributes().put(
		FLD_DEBIAN_VERSION.getKey(), debianVersion);
    }

    /**
     * Popluate ArtifactInfo from exisiting lucene index document, will want to
     * populate the same fields that you populate in populateArtifactInfo
     */
    public boolean updateArtifactInfo(Document document,
	    ArtifactInfo artifactInfo) {
	// Add the data to the ArtifactInfo from the index document.
	String debianPackage = document.get(FLD_DEBIAN_PACKAGE.getKey());
	String debianVersion = document.get(FLD_DEBIAN_VERSION.getKey());

	if (debianPackage != null && debianVersion != null) {
	    artifactInfo.getAttributes().put(FLD_DEBIAN_PACKAGE.getKey(),
		    debianPackage);
	    artifactInfo.getAttributes().put(FLD_DEBIAN_VERSION.getKey(),
		    debianVersion);
	}

	// Note that returning false here will notify calling party of failure
	return true;
    }

    /**
     * Add data from the artifactInfo to the index
     */
    public void updateDocument(ArtifactInfo artifactInfo, Document document) {
	String debianPackage = artifactInfo.getAttributes().get(
		FLD_DEBIAN_PACKAGE.getKey());
	String debianVersion = artifactInfo.getAttributes().get(
		FLD_DEBIAN_VERSION.getKey());

	if (debianPackage != null && debianVersion != null) {
	    document.add(FLD_DEBIAN_PACKAGE.toField(debianPackage));
	    document.add(FLD_DEBIAN_VERSION.toField(debianVersion));
	}
    }

    public Collection<IndexerField> getIndexerFields() {
	return Arrays.asList(FLD_DEBIAN_PACKAGE, FLD_DEBIAN_VERSION);
    }

    /* toolbox to parse POM */
    private Xpp3Dom readPomInputStream(InputStream is) {
	Reader r = new InputStreamReader(is);
	try {
	    return Xpp3DomBuilder.build(r);
	} catch (XmlPullParserException e) {
	} catch (IOException e) {
	} finally {
	    try {
		r.close();
	    } catch (IOException e) {
	    }
	}

	return null;
    }
}
