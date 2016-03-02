<html>
<head>
	<style>
		img {
			border: inset #4E4 2px;
		}
		span.result {
			color: #080;
			font-weight: bold;
		}
		#header {
			text-align: center;
			width: 300px;
			margin-left: auto;
			margin-right: auto;
		}
		#footer {
			position: fixed;
			bottom: 0px;
			left: 0px;
			right: 0px;
			text-align: center;
		}
		#header, #footer {
			border: ridge #4E4 4px;
			background:#EEE;
		}
		#footer, #footer_spacer {
			height: 70px;
		}
		
	</style>
</head>
<body>
	<?php
		$filename = "theFourMailingList.txt";
		$msg = wordwrap("\n\nVisit http://www.doubleastudios.net/theFour/ to edit your subscription status " . 
			"or download the latest version.");
		$email = $_POST["email"];
		$action = $_POST["action"];
		
		if (isset($email) && isset($action) && $email != "") {
			$emailExists = (strpos(file_get_contents($filename), $email . "\r\n") !== false);
			if ($action == "subscribe") {
				if ($emailExists) {
					$result = "That email is already subscribed to this mailing list.";
				} else {
					file_put_contents($filename, file_get_contents($filename) . $email . "\r\n");
					mail($email, "Subscribed to The Four: Forgotten Age", "You have been successfully subscribed." . $msg,
						"From: project@doubleastudios.net");
					$result = "Subscribe complete.";
				}
			} else if ($action == "unsubscribe") {
				if ($emailExists) {
					file_put_contents($filename, str_replace($email . "\r\n", "", file_get_contents($filename)));
					mail($email, "Unsubscribed from The Four: Forgotten Age", "You have been successfully unsubscribed."
						. $msg, "From: project@doubleastudios.net");
					$result = "Unsubscribe complete.";
				} else {
					$result = "That email is not currently subscribed to this mailing list.";
				}
			}
		}
	?>
	<div>
	<div id="header">
		<h2 style="margin:3px">The Four: Forgotten Age</h2>
		<img src="sample.png"></img>
		<h5 style="margin:3px"><a href="./four.jar" download>Download the latest version (0.01)</a></h5>
	</div>
	</div>
	<p>
		This turn-based, 8-bit styled role playing game is still in early development. There are two short scenarios to play 
		through, and everything is controlled by clicking with the mouse. I encourage any and all comments, complaints, and 
		suggestions at project@doubleastudios.net. Please sign up for the mailing list in the footer if you'd like to receive
		emails as new versions are unveiled (monthly updates are planned).
	</p>
	
	<p>
		There are a number of planned improvements, including:
		<ul>
			<li>Character movement</li>
			<li>Equipment</li>
			<li>New Abilities (learn and master)</li>
			<li>Status Effects</li>
			<li>Keyboard/Controller Input</li>
			<li>Epic story quest</li>
			<li>Various side quests</li>
			<li>Multiplayer capability?</li>
		</ul>
	</p>
	
	<p>
		The source code is <a href="https://github.com/Zekzek/TheFour/tree/master">available on Github</a> and versioned archives 
		of the game will remain available here:
		<ul>
			<li><a href="./four_0_01.jar" download>0.01</a></li>
		</ul>
	</p>
	
	<div id="footer_spacer"></div>
	<div id="footer">
		<h4 style="margin:3px">Sign up for monthly updates!</h4>
		<form action="index.php" method="post">
			<input type="radio" name="action" value="subscribe" checked="checked"></input>Subscribe &nbsp;
			<input type="radio" name="action" value="unsubscribe"></input>Unsubscribe
			&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
			<label>Email:</label> 
			<input type="email" name="email" value="<?php echo $_POST["email"]; ?>"></input>
			&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;
			<input type="submit" value="Update Subscription"></input>
			<br>
			<span class="result"><?php echo $result; ?></span>
		</form>
	</div>
</body>
</html>