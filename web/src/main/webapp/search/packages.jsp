<%--
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
--%>
<%@ include file="/common/header.jsp" %>

<%@page import="org.sonatype.nexus.index.MAVEN" %>
<%@page import="org.sonatype.nexus.index.ArtifactInfo" %>
<%@page import="org.debian.maven.indexer.creators.DEBIAN" %>
<%@page import="org.debian.maven.indexer.creators.DebianIndexCreator" %>

<c:if test="${not empty it}">
    <table border="1">
        <thead>
            <tr>
                <th>GroupId</th>
                <th>ArtifactId</th>
                <th>Version</th>
                <th>Debian Package</th>
            </tr>
        </thead>
        <tbody>
        <c:forEach var="ai" items="${it}">
            <c:set var="varAI" value="${ai}" />
            <%
            ArtifactInfo varAI = (ArtifactInfo) pageContext.getAttribute("varAI");
            %>
            <tr>
                <td><%=varAI.getFieldValue(MAVEN.GROUP_ID)%></td>
                <td><%=varAI.getFieldValue(MAVEN.ARTIFACT_ID)%></td>
                <td><%=varAI.getFieldValue(MAVEN.VERSION)%></td>
                <td><%=varAI.getAttributes().get(DebianIndexCreator.FLD_DEBIAN_PACKAGE.getKey())%></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>


<%@ include file="/common/footer.jsp" %>