package org.debian.maven.indexer.rest;

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

import com.sun.jersey.api.view.Viewable;
import java.util.Set;
import javax.ejb.EJB;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.debian.maven.indexer.ejb.SearchService;
import org.sonatype.nexus.index.ArtifactInfo;

@Singleton
@Path("/search")
public class SearchBean {

    @EJB
    private SearchService service;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Viewable index() {
        return new Viewable("/search/index.jsp", "FOO");
    }

    @GET @Path("/packages")
    @Produces(MediaType.TEXT_HTML)
    public Viewable packages(@QueryParam("packageName") String packageName) {
        Set<ArtifactInfo> set = service.searchDebianPackage(packageName);
        return new Viewable("/search/packages.jsp", set);
    }

    @GET @Path("/classes")
    @Produces(MediaType.TEXT_HTML)
    public Viewable classes(@QueryParam("className") String className) {
        Set<ArtifactInfo> set = service.searchClassName(className);
        return new Viewable("/search/classes.jsp", set);
    }
}
