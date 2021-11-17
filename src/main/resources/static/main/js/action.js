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
