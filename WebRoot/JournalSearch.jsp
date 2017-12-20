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
  	<div class="jumbotron">
	  	<form class="form-horizontal" role="form" >
		  <div class="form-group">
		  	<div class="col-sm-offset-1 col-sm-2">
		  		<img src=<%=path+"/image/journal_search.png" %> height="40" class="img-rounded"/>
		  	</div>
		    <div class="col-sm-7" >
		      <input type="text" class="form-control" id="firstname" placeholder="input something...."/>
		    </div>
		    <div class="col-sm-2">
		    	<button type="submit" class="btn btn-success">搜索</button>
		    </div>
		  </div>
		  <div class="form-group">
		  	<label for="inputEmail3" class="col-sm-offset-2 col-sm-1 control-label">多域检索</label>
		  	<div class="col-sm-1">
			  	<div class="checkbox">
		        	<label>
		          		<input type="checkbox">作者
		        	</label>
		      	</div>
		    </div>
		    <div class="col-sm-1">
		    	<input type="text" class="form-control" id="author" />
		    </div>
		    <div class="col-sm-1">
			  	<div class="checkbox">
		        	<label>
		          		<input type="checkbox">题名
		        	</label>
		      	</div>
		    </div>
		    <div class="col-sm-1">
		    	<input type="text" class="form-control" id="author" />
		    </div>
		    <div class="col-sm-1">
			  	<div class="checkbox">
		        	<label>
		          		<input type="checkbox">摘要
		        	</label>
		      	</div>
		    </div>
		    <div class="col-sm-1">
		    	<input type="text" class="form-control" id="author" />
		    </div>
		  </div>
		  <div class="form-group">
		  	<div class="col-sm-offset-3 col-sm-1">
			  	<div class="checkbox">
		        	<label>
		          		<input type="checkbox">关键词
		        	</label>
		      	</div>
		    </div>
		    <div class="col-sm-1">
		    	<input type="text" class="form-control" id="author" />
		    </div>
		    <div class="col-sm-1">
			  	<div class="checkbox">
		        	<label>
		          		<input type="checkbox">出版日期
		        	</label>
		      	</div>
		    </div>
		    <div class="col-sm-1">
		    	<input type="text" class="form-control" id="author" />
		    </div>
		    <div class="col-sm-1">
			  	<div class="checkbox">
		        	<label>
		          		<input type="checkbox">英文刊名
		        	</label>
		      	</div>
		    </div>
		    <div class="col-sm-1">
		    	<input type="text" class="form-control" id="author" />
		    </div>
		  </div>
		  
		  <div class="form-group">
		  	<label for="inputEmail3" class="col-sm-offset-2 col-sm-1 control-label">近义词检索</label>
		  	<div class="col-sm-2">
			  	<div class="checkbox">
		        	<label>
		          		<input type="checkbox" name="" id="" value="">请输入需要模糊查询的词：
		        	</label>
		      	</div>
		    </div>
		    <div class="col-sm-2">
		    	<input type="text" class="form-control" id="author" />
		    </div>
		  </div>
		</form>
	</div>
  	
  	
  </body>
</html>
