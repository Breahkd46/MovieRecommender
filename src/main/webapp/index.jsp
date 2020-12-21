<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
 
pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>MovieRecomender</title>
</head>
<body>
 
	<center>
		<h2>Hello World</h2>
		<h3>
			<a href="hello?name=UT2">Click Here</a>
		</h3>

		<h4><a href="movies">Movies</a></h4>
		<form action="movies" style="width: 50%; margin-bottom: 30px">
			<fieldset style="display: flex; justify-content: space-between">
				<legend>Movies :</legend>
				<div>
					<label for="mU">Id user : </label>
					<input id="mU" name="user_id" type="number" required>
				</div>
				<button type="submit">Go</button>
			</fieldset>
		</form>

		<form action="movieratings" style="width: 50%; margin-bottom: 30px">
			<fieldset style="display: flex; justify-content: space-between">
				<legend>Movies rating :</legend>
				<div>
					<label for="mrU">Id user : </label>
					<input id="mrU" name="user_id" type="number" required>
				</div>
				<button type="submit">Go</button>
			</fieldset>
		</form>

		<form action="recommendations" style="width: 50%; margin-bottom: 30px">
			<fieldset style="display: flex; justify-content: space-between">
				<legend>Recommendations :</legend>
				<div>
					<div style="margin-bottom: 10px">
						<label for="rU">Id user : </label>
						<input id="rU" name="user_id" type="number" required>
					</div>
					<div style="display: flex; justify-content: space-between">
						<span>Processing Mode : </span>
						<div>
							<input id="p1" type="radio" name="processing_mode" value="1" checked><label for="p1">1</label>
						</div>
						<div>
							<input id="p2" type="radio" name="processing_mode" value="2"><label for="p2">2</label>
						</div>
						<div>
							<input id="p3" type="radio" name="processing_mode" value="3"><label for="p3">3</label>
						</div>

					</div>
				</div>
				<button type="submit">Go</button>
			</fieldset>
		</form>
	</center>
</body>
</html>
