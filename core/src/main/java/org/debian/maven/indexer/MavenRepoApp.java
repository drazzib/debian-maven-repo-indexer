package org.debian.maven.indexer;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.DefaultContext;

public class MavenRepoApp {

    private PlexusContainer container;
    private MavenRepoIndexer app;

    public MavenRepoIndexer getApp() {
        return app;
    }

    public void bootstrap() {
        try {
            setupContainer();

            app = container.lookup(MavenRepoIndexer.class);

        } catch (ComponentLookupException ex) {
            Logger.getLogger(MavenRepoApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void setupContainer() {
        // ----------------------------------------------------------------------------
        // Context Setup
        // ----------------------------------------------------------------------------
        final DefaultContext context = new DefaultContext();
        context.put("repository.path", "/usr/share/maven-repo/");
        context.put("index.path", "target/indexOutput");

        // ----------------------------------------------------------------------------
        // Configuration
        // ----------------------------------------------------------------------------
        final ContainerConfiguration containerConfiguration = new DefaultContainerConfiguration().setName("debian").setContext(context.getContextData());

        try {
            container = new DefaultPlexusContainer(containerConfiguration);
        } catch (final PlexusContainerException ex) {
           Logger.getLogger(MavenRepoApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
