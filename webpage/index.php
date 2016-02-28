<html>
<head>
</head>
<body>
	<a href="./four.jar" download>Download The Four: Forgotten Age</a>
	<br><br>
	<fieldset>
		<legend>Subscribe/Unsubscribe for Monthly Updates</legend>
		<form action="index.php" method="post">
			<select name="action">
				<option value="subscribe">Subscribe</option>
				<option value="unsubscribe">Unsubscribe</option>
			</select>
			&nbsp; &nbsp; &nbsp;
			<label>Email:</label> 
			<input type="email" name="email" value="<?php echo $_POST["email"]; ?>"></input>
			<input type="submit"></input>
		</form>
		<?php
			$filename = "theFourMailingList.txt";
			$msg = wordwrap("\n\nVisit http://www.doubleastudios.net/theFour/ to edit your subscription status " . 
				"or download the latest version.");
			$email = $_POST["email"];
			$action = $_POST["action"];
			
			if (!isset($email) || !isset($action)) {
				die("");
			}
			
			$emailExists = (strpos(file_get_contents($filename), $email . "\r\n") !== false);
			if ($action == "subscribe") {
				if ($emailExists) {
					echo "That email is already subscribed to this mailing list.";
				} else {
					file_put_contents($filename, file_get_contents($filename) . $email . "\r\n");
					mail($email, "Subscribed to The Four: Forgotten Age", "You have been successfully subscribed." . $msg,
						"From: project@doubleastudios.net");
					echo "Subscribe complete.";
				}
			} else if ($action == "unsubscribe") {
				if ($emailExists) {
					file_put_contents($filename, str_replace($email . "\r\n", "", file_get_contents($filename)));
					mail($email, "Unsubscribed from The Four: Forgotten Age", "You have been successfully unsubscribed."
						. $msg, "From: project@doubleastudios.net");
					echo "Unsubscribe complete.";
				} else {
					echo "That email is not currently subscribed to this mailing list.";
				}
			}
		?>
	</fieldset>
</body>
</html>