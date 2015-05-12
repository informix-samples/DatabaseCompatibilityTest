<!DOCTYPE html>
<html>
<head>
	<title>PHP Starter Application</title>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
	<link rel="stylesheet" href="style.css" />
</head>
<body>
	<table>
		<tr>
			<td style='width: 30%;'><img src='images/newapp-icon.png'>
			</td>
			<td>
				<h1 id = "message"><?php echo "PHP compatibililty test"; ?>
</h1>
				<p class='description'></p> Links:
				<ul>
				<li><a href="src/main.php">Run compatibility test</a></li>
				<li><a href="utils/phpinfo.php">PHP Info</a></li>
				<li><a href="utils/testconnect.php">Test connection to SQLDB</a></li>
				</ul>
			</td>
		</tr>
	</table>
</body>
</html>
