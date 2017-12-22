<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%
request.setCharacterEncoding("utf-8");
response.setCharacterEncoding("utf-8");
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  
  
  
    <base href="<%=basePath%>">
    
    <title>JournalSearch</title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">

	<link rel="stylesheet" type="text/css" href="bootstrap/css/bootstrap.min.css" />
	<script type="text/javascript" src="bootstrap/js/bootstrap.min.js"></script>
	<script src="http://cdn.bootcss.com/jquery/1.11.1/jquery.min.js"></script>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
  </head>
  <body>
  	<%
 		String[] checkinputName = {"author_input", "title_input", "press_input", "time_from_input", "time_to_input", "fuzz_input"};
		String currentInput[] = {"", "", "", "", "", ""};
		String s = "false";
		String currentQuery=(String) request.getAttribute("currentQuery");
		if (currentQuery == null) 
			currentQuery = "";
		String currentPage_str=(String) request.getAttribute("currentPage");
		if (currentPage_str == null) 
			currentPage_str = "1";
		int currentPage = Integer.parseInt(currentPage_str);
		for (int k = 0;k < checkinputName.length;k ++){
			if ((String)request.getAttribute(checkinputName[k]) != null)
				currentInput[k] = (String)request.getAttribute(checkinputName[k]);
		}
		String [] authorList = (String[])request.getAttribute("authorList");
		String [] abstractList = (String[])request.getAttribute("abstractList");
		String [] pressList = (String[])request.getAttribute("pressList");
		String [] titleList = (String[])request.getAttribute("titleList");
		String [] yearList = (String[])request.getAttribute("yearList");
		String totalLength_str = (String)request.getAttribute("totalLength");
		int totalLength = 0, nowPos = 0, minPage = 0, hitslength = 0;
		if (totalLength_str != null){
			totalLength = Integer.parseInt(totalLength_str);
			nowPos = (Integer) request.getAttribute("nowPos");
			minPage = (Integer) request.getAttribute("minPage");
			hitslength = (Integer)request.getAttribute("hitslength");
		}
		String isSearch=(String) request.getAttribute("isSearch");
	%>
  	<div class="jumbotron">
	  	<form class="form-horizontal" name="search_form" method="post" action="servlet/MainServer">
		  <div class="form-group">
		  	<div class="col-sm-offset-1 col-sm-2">
		  		<img src=<%=path+"/image/journal_search.png" %> height="40" class="img-rounded"/>
		  	</div>
		    <div class="col-sm-7" >
		      <input type="text" class="form-control" name="query" id="firstname" placeholder="input something...." value="<%=currentQuery%>"/>
		    </div>
		    <div class="col-sm-2">
		    	<button type="submit" class="btn btn-success">搜索</button>
		    </div>
		  </div>
		  <div class="form-group">
		  	<label class="col-sm-offset-2 col-sm-1 control-label">多域检索</label>
			<label class="col-sm-1 control-label">作者</label>
		    <div class="col-sm-1">
		    	<input type="text" class="form-control" id="author" name="author_input" value="<%=currentInput[0]%>"/>
		    </div>
			<label class="col-sm-1 control-label">题名</label>
		    <div class="col-sm-1">
		    	<input type="text" class="form-control" id="author" name="title_input" value="<%=currentInput[1]%>"/>
		    </div>
			<label class="col-sm-1 control-label">出版单位</label>
		    <div class="col-sm-1">
		    	<input type="text" class="form-control" id="author" name="press_input" value="<%=currentInput[2]%>"/>
		    </div>
		  </div>
		  
		   <div class="form-group">
		   		<label class="col-sm-offset-3 col-sm-1 control-label">出版年份</label>
		   		<label class="col-sm-1 control-label">from:</label>
		   		<div class="col-sm-1">
			    	<input type="text" class="form-control" id="author" name="time_from_input" value="<%=currentInput[3]%>"/>
			    </div>
			    <label class="col-sm-1 control-label">to:</label>
		   		<div class="col-sm-1">
			    	<input type="text" class="form-control" id="author" name="time_to_input" value="<%=currentInput[4]%>"/>
			    </div>
		   </div>
		  
		  <div class="form-group">
		  	<label for="inputEmail3" class="col-sm-offset-2 col-sm-1 control-label">近义词检索</label>
		  	<label class="col-sm-2 control-label">请输入需要模糊查询的词：</label>
		    <div class="col-sm-2">
		    	<input type="text" class="form-control" id="author" name="fuzz_input" value="<%=currentInput[5]%>"/>
		    </div>
		  </div>
		</form>
	</div>
  
	  <% 
	  	if(titleList!=null && titleList.length>0){
	  		for(int i=10*(currentPage - 1);i<Math.min(10*currentPage, hitslength);i++){
	  			int iter = i - 10*(currentPage - 1);%>
	  			<div class="col-sm-offset-1 col-sm-10">
		  			<div class="panel panel-success">
					  <div class="panel-heading">
					    <h3 class="panel-title"><%=titleList[iter] %></h3>
					  </div>
					  <div class="panel-body">
					    摘要&nbsp;<%=abstractList[iter] %>
					  </div>
					  <div class="panel-footer"><%=authorList[iter] %> - <%=pressList[iter] %> - <%=yearList[iter] %></div>
					</div>
				</div>
			<%}; %>
	  	<%}else{ 
	  		if (isSearch != null){%>
		  		<div class="col-sm-offset-1 col-sm-10">
				<ul class="list-group">
					<li class="list-group-item list-group-item-danger">找不到结果！</li>
				</ul>
		  		</div>
	  	<%	}
	  	 }; %>
  	
  	<br>
  	<% if (totalLength_str != null){%>
	  	<div class="text-center">
		  	<nav aria-label="Page navigation">
			  <ul class="pagination  pagination-lg">
			    <li>
			      <a href="servlet/MainServer?page=<%=(currentPage - 1) %>" aria-label="Previous">
			        <span aria-hidden="true">&laquo;</span>
			      </a>
			    </li>
			    	<% for(int i = minPage;i < minPage + totalLength;i ++){ %>
			    	<% if (i - minPage + 1 == nowPos){ %>
			    		<li class="active"><a href="servlet/MainServer?page=<%=i%>"><%=i%></a></li>
			    	<%} else{ %>
			    		<li><a href="servlet/MainServer?page=<%=i%>"><%=i%></a></li>
			    	<%}; %>
				    <%}; %>
			    <li>
			      <a href="servlet/MainServer?page=<%=(currentPage + 1) %>" aria-label="Next">
			        <span aria-hidden="true">&raquo;</span>
			      </a>
			    </li>
			  </ul>
			</nav>
	  	</div>
	<%}; %>
  </body>
</html>
