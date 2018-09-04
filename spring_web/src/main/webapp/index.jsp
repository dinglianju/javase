<%@ page language="java" pageEncoding="UTF-8"%>
<!DOCTYPE HTML>
<html>
<head>
<title>文件上传</title>
</head>

<body>
	<form action="${pageContext.request.contextPath}/fileUplad"
		enctype="multipart/form-data" method="post">
		上传用户：<input type="text" name="username"> <br /> 上传文件：<input
			type="file" name="file"><br /> <input type="submit"
			value="单文件提交">
	</form>
	<form action="${pageContext.request.contextPath}/multiUpload"
		enctype="multipart/form-data" method="post">
		上传用户：<input type="text" name="username"> <br /> 上传文件1：<input
			type="file" name="file"><br /> 上传文件2：<input type="file"
			name="file2"><br /> <input type="submit" value="多文件提交1">
	</form>
	<form action="${pageContext.request.contextPath}/filesUpload"
		enctype="multipart/form-data" method="post">
		上传用户：<input type="text" name="username"> <br /> 上传文件1：<input
			type="file" name="files"><br /> 上传文件2：<input type="file"
			name="files"><br /> <input type="submit" value="多文件提交2">
	</form>
</body>
</html>