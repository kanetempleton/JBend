//import axios from 'axios';

function trycommand() {

const URL='fuzz'

	const argz = document.getElementById("enterCommand").value;

	const sendme={
	    packet:420,
		inp:argz,
		end:0
	}
	document.getElementById("statusCode").innerHTML = "Sending command to server...";
$.ajax({
        url: URL,
        type: 'POST',
        contentType: 'application/x-www-form-urlencoded; charset=UTF-8',
        data: sendme,
        success: function(result) {
            // Do something with the result
            console.log(result)

            if (result == 'retry') {
                trycommand();
            }
            else if (result == 'gtfo') {
                document.getElementById("statusCode").innerHTML = "You are not an admin. GTFO";
            }
            else if (result == 'wyd') {
                document.getElementById("statusCode").innerHTML = "You must log in before attempting to use this feature.";
            }
            else if (result.startsWith('comresp[]+:+::+++:::')) {
                var gx = result.split('comresp[]+:+::+++:::');
                if (gx.length!=2) {
                    document.getElementById("statusCode").innerHTML = "BAD REPLY PLEASE REPORT THIS TO WEB ADMINS";
                } else {
                    document.getElementById("statusCode").innerHTML = "Completed.";
                    document.getElementById("output").innerHTML = "Response:<br>"+gx[1];
                }
            }
            else {
                document.getElementById("statusCode").innerHTML = "Unrecognized server response.";
            }
        }
    });
}

var submitButton = document.getElementById("submitButton");
submitButton.addEventListener ("click", function() {
    trycommand();
});
