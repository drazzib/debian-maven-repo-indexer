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

<form action="<%=rootPath%>/search/packages/">
    <label for="packageName">Debian Package ?</label>
<input name="packageName" />
<input type="submit" name="Search"/>
</form>

<form action="<%=rootPath%>/search/classes/">
    <label for="className">Class Name ?</label>
<input name="className" />
<input type="submit" name="Search"/>
</form>

<%@ include file="/common/footer.jsp" %>
