$(function(){
	$('.chats').on('click', function(){
		$('#chatBox').css('display','block');
		$('#chatInputBox').css('display','flex');
		$('#attendiesBox').css('display','none');
	});

	$('.attendies').on('click', function(){
		$('#chatBox').css('display','none');
		$('#chatInputBox').css('display','none');
		$('#attendiesBox').css('display','block');
	});
});

// Get the modal
var modal = document.getElementById("myModal");

// Get the button that opens the modal
var btn = document.getElementById("file-upload");

// Get the <span> element that closes the modal
var span = document.getElementsByClassName("close")[0];

// When the user clicks the button, open the modal 
btn.onclick = function() {
  modal.style.display = "block";
}

// When the user clicks on <span> (x), close the modal
span.onclick = function() {
  modal.style.display = "none";
}

// When the user clicks anywhere outside of the modal, close it
window.onclick = function(event) {
  if (event.target == modal) {
    modal.style.display = "none";
  }
}

const btnSendFile = document.querySelector('#btnSendFile');
const fileInputElement = document.querySelector('[name="fileUpload"]');
btnSendFile.addEventListener('click',()=>{
	var formData = new FormData();
	formData.append("userfile", fileInputElement.files[0]);
	// JavaScript file-like object
	var content = '<a id="a"><b id="b">hey!</b></a>'; // the body of the new file...
	var blob = new Blob([content], { type: "text/xml"});
	formData.append("webmasterfile", blob);
	var request = new XMLHttpRequest();
	request.open("POST", "http://localhost:8000/file");
	request.send(formData);
	modal.style.display = "none";

});
