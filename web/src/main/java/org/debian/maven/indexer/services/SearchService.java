package org.debian.maven.indexer.services;

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

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import org.debian.maven.indexer.MavenRepoApp;
import org.debian.maven.indexer.creators.DEBIAN;
import org.sonatype.nexus.index.ArtifactInfo;
import org.sonatype.nexus.index.MAVEN;
import org.sonatype.nexus.index.SearchType;

@Stateless
public class SearchService {

    private MavenRepoApp app;

    @PostConstruct
    public void startup() {
        this.app = new MavenRepoApp();
        this.app.bootstrap();
        try {
            // FIXME Maybe start this latter ?
            this.app.getApp().index();
        } catch (IOException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Set<ArtifactInfo> searchDebianPackage(String packageName) {
        try {
            return this.app.getApp().searchIndexFlat(DEBIAN.DEBIAN_PACKAGE, packageName, SearchType.SCORED);
        } catch (IOException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public Set<ArtifactInfo> searchClassName(String className) {
        try {
            return this.app.getApp().searchIndexFlat(MAVEN.CLASSNAMES, className, SearchType.SCORED);
        } catch (IOException ex) {
            Logger.getLogger(SearchService.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

}
