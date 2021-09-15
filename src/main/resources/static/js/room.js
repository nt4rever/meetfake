const myvideo = document.querySelector("#vd1");
const roomId = document.querySelector(".roomcode").innerText;
let username;
const chatRoom = document.querySelector('.chat-cont');
const sendButton = document.querySelector('.chat-send');
const messageField = document.querySelector('.chat-input');
const videoContainer = document.querySelector('#vcont');

const videoButt = document.querySelector('.novideo');
const audioButt = document.querySelector('.audio');
const cutCall = document.querySelector('.cutcall');


let videoAllowed = 1;
let audioAllowed = 1;

let micInfo = {};
let videoInfo = {};

let videoTrackReceived = {};

let mymuteicon = document.querySelector("#mymuteicon");
mymuteicon.style.visibility = 'hidden';

let myvideooff = document.querySelector("#myvideooff");
myvideooff.style.visibility = 'hidden';

const configuration = {iceServers: [{urls: "stun:stun.stunprotocol.org"}]}
const mediaConstraints = {video: true, audio: true};

let connections = {};
let cName = {};
let audioTrackSent = {};
let videoTrackSent = {};

let mystream;
username = document.getElementById("myname").innerText
let peerConnection;


const overlayContainer = document.querySelector('#overlay')
overlayContainer.style.visibility = 'hidden';

//establish socket
const socket = new WebSocket("ws://" + window.location.host + "/signal");

// send a message to the server to join selected room with Web Socket
socket.onopen = function () {
    log('WebSocket connection opened to Room: #' + roomId);
    sendToServer({
        from: username,
        type: 'join',
        data: roomId,
        candidate: null,
        sid: null,
        offer: null,
        micinf: null,
        vidinf: null,
        cname: null,
        answer: null,
    });
};

// a listener for the socket being closed event
socket.onclose = function (message) {
    log('Socket has been closed');
};

// an event listener to handle socket errors
socket.onerror = function (message) {
    handleErrorMessage("Error: " + message);
};

function startCall() {
    navigator.mediaDevices.getUserMedia(mediaConstraints)
        .then(localStream => {
            myvideo.srcObject = localStream;
            myvideo.muted = true;
            localStream.getTracks().forEach(track => {
                for (let key in connections) {
                    connections[key].addTrack(track, localStream);
                    if (track.kind === 'audio')
                        audioTrackSent[key] = track;
                    else
                        videoTrackSent[key] = track;
                }
            })

        })
        .catch(handleGetUserMediaError);
}

async function joinHandle(conc, cnames, micinfo, videoinfo) {
    if (cnames)
        cName = cnames;
    if (micinfo)
        micInfo = micinfo;
    if (videoinfo)
        videoInfo = videoinfo;
    console.log(cName);
    if (conc) {
        await conc.forEach(sid => {
            connections[sid] = new RTCPeerConnection(configuration);

            connections[sid].onicecandidate = function (event) {
                if (event.candidate) {
                    console.log('icecandidate fired');
                    sendToServer({
                        type: 'ice',
                        candidate: event.candidate,
                        sid: sid,
                    })
                    // socket.emit('new icecandidate', event.candidate, sid);
                }
            };

            connections[sid].ontrack = function (event) {

                if (!document.getElementById(sid)) {
                    console.log('track event fired')
                    let vidCont = document.createElement('div');
                    let newvideo = document.createElement('video');
                    let name = document.createElement('div');
                    let muteIcon = document.createElement('div');
                    let videoOff = document.createElement('div');
                    videoOff.classList.add('video-off');
                    muteIcon.classList.add('mute-icon');
                    name.classList.add('nametag');
                    name.innerHTML = `${cName[sid]}`;
                    vidCont.id = sid;
                    muteIcon.id = `mute${sid}`;
                    videoOff.id = `vidoff${sid}`;
                    muteIcon.innerHTML = `<i class="fas fa-microphone-slash"></i>`;
                    videoOff.innerHTML = 'Video Off'
                    vidCont.classList.add('video-box');
                    newvideo.classList.add('video-frame');
                    newvideo.autoplay = true;
                    newvideo.playsinline = true;
                    newvideo.id = `video${sid}`;
                    newvideo.srcObject = event.streams[0];

                    if (micInfo[sid] == 'on')
                        muteIcon.style.visibility = 'hidden';
                    else
                        muteIcon.style.visibility = 'visible';

                    if (videoInfo[sid] == 'on')
                        videoOff.style.visibility = 'hidden';
                    else
                        videoOff.style.visibility = 'visible';

                    vidCont.appendChild(newvideo);
                    vidCont.appendChild(name);
                    vidCont.appendChild(muteIcon);
                    vidCont.appendChild(videoOff);

                    videoContainer.appendChild(vidCont);

                }

            };

            connections[sid].onremovetrack = function (event) {
                if (document.getElementById(sid)) {
                    document.getElementById(sid).remove();
                }
            }

            connections[sid].onnegotiationneeded = function () {

                connections[sid].createOffer()
                    .then(function (offer) {
                        return connections[sid].setLocalDescription(offer);
                    })
                    .then(function () {
                        sendToServer({
                            type: 'offer',
                            offer: connections[sid].localDescription,
                            sid: sid,
                        })
                        // socket.emit('video-offer', connections[sid].localDescription, sid);

                    })
                    .catch(reportError);
            };

        });
        console.log('added all sockets to connections');
        startCall();
    } else {
        console.log('waiting for someone to join');
        navigator.mediaDevices.getUserMedia(mediaConstraints)
            .then(localStream => {
                myvideo.srcObject = localStream;
                myvideo.muted = true;
                mystream = localStream;
            })
            .catch(handleGetUserMediaError);
    }
}

function handleVideoOffer(offer, sid, cname, micinf, vidinf) {
    cName[sid] = cname;
    console.log('video offered recevied');
    micInfo[sid] = micinf;
    videoInfo[sid] = vidinf;
    connections[sid] = new RTCPeerConnection(configuration);
    connections[sid].onicecandidate = function (event) {
        if (event.candidate) {
            console.log('icecandidate fired');
            sendToServer({
                from: username,
                type: 'ice',
                candidate: event.candidate,
                sid: sid,
            })
            // socket.emit('new icecandidate', event.candidate, sid);
        }
    };
    connections[sid].ontrack = function (event) {
        if (!document.getElementById(sid)) {
            console.log('track event fired')
            let vidCont = document.createElement('div');
            let newvideo = document.createElement('video');
            let name = document.createElement('div');
            let muteIcon = document.createElement('div');
            let videoOff = document.createElement('div');
            videoOff.classList.add('video-off');
            muteIcon.classList.add('mute-icon');
            name.classList.add('nametag');
            name.innerHTML = `${cName[sid]}`;
            vidCont.id = sid;
            muteIcon.id = `mute${sid}`;
            videoOff.id = `vidoff${sid}`;
            muteIcon.innerHTML = `<i class="fas fa-microphone-slash"></i>`;
            videoOff.innerHTML = 'Video Off'
            vidCont.classList.add('video-box');
            newvideo.classList.add('video-frame');
            newvideo.autoplay = true;
            newvideo.playsinline = true;
            newvideo.id = `video${sid}`;
            newvideo.srcObject = event.streams[0];

            if (micInfo[sid] == 'on')
                muteIcon.style.visibility = 'hidden';
            else
                muteIcon.style.visibility = 'visible';

            if (videoInfo[sid] == 'on')
                videoOff.style.visibility = 'hidden';
            else
                videoOff.style.visibility = 'visible';

            vidCont.appendChild(newvideo);
            vidCont.appendChild(name);
            vidCont.appendChild(muteIcon);
            vidCont.appendChild(videoOff);

            videoContainer.appendChild(vidCont);

        }


    };

    connections[sid].onremovetrack = function (event) {
        if (document.getElementById(sid)) {
            document.getElementById(sid).remove();
            console.log('removed a track');
        }
    };

    connections[sid].onnegotiationneeded = function () {

        connections[sid].createOffer()
            .then(function (offer) {
                return connections[sid].setLocalDescription(offer);
            })
            .then(function () {
                sendToServer({
                    from: username,
                    type: 'offer',
                    data: connections[sid].localDescription,
                    sid: sid,
                })
                // socket.emit('video-offer', connections[sid].localDescription, sid);

            })
            .catch(reportError);
    };

    let desc = new RTCSessionDescription(offer);

    connections[sid].setRemoteDescription(desc)
        .then(() => {
            return navigator.mediaDevices.getUserMedia(mediaConstraints)
        })
        .then((localStream) => {

            localStream.getTracks().forEach(track => {
                connections[sid].addTrack(track, localStream);
                console.log('added local stream to peer')
                if (track.kind === 'audio') {
                    audioTrackSent[sid] = track;
                    if (!audioAllowed)
                        audioTrackSent[sid].enabled = false;
                } else {
                    videoTrackSent[sid] = track;
                    if (!videoAllowed)
                        videoTrackSent[sid].enabled = false
                }
            })

        })
        .then(() => {
            return connections[sid].createAnswer();
        })
        .then(answer => {
            return connections[sid].setLocalDescription(answer);
        })
        .then(() => {
            sendToServer({
                from: username,
                type: 'answer',
                answer: connections[sid].localDescription,
                sid: sid,
            })
            // socket.emit('video-answer', connections[sid].localDescription, sid);
        })
        .catch(handleGetUserMediaError);
}

function handleNewIceCandidate(candidate, sid) {
    console.log('new candidate recieved')
    var newcandidate = new RTCIceCandidate(candidate);
    connections[sid].addIceCandidate(newcandidate)
        .catch(reportError);
}

function handleVideoAnswer(answer, sid) {
    console.log('answered the offer')
    const ans = new RTCSessionDescription(answer);
    connections[sid].setRemoteDescription(ans);
}

videoButt.addEventListener('click', () => {

    if (videoAllowed) {
        for (let key in videoTrackSent) {
            videoTrackSent[key].enabled = false;
        }
        videoButt.innerHTML = `<i class="fas fa-video-slash"></i>`;
        videoAllowed = 0;
        videoButt.style.backgroundColor = "#b12c2c";

        if (mystream) {
            mystream.getTracks().forEach(track => {
                if (track.kind === 'video') {
                    track.enabled = false;
                }
            })
        }

        myvideooff.style.visibility = 'visible';
        sendToServer({
            type: 'action',
            data: 'videooff',
        })
        // socket.emit('action', 'videooff');
    } else {
        for (let key in videoTrackSent) {
            videoTrackSent[key].enabled = true;
        }
        videoButt.innerHTML = `<i class="fas fa-video"></i>`;
        videoAllowed = 1;
        videoButt.style.backgroundColor = "#4ECCA3";
        if (mystream) {
            mystream.getTracks().forEach(track => {
                if (track.kind === 'video')
                    track.enabled = true;
            })
        }


        myvideooff.style.visibility = 'hidden';
        sendToServer({
            type: 'action',
            data: 'videoon',
        })
        // socket.emit('action', 'videoon');
    }
})


audioButt.addEventListener('click', () => {
    if (audioAllowed) {
        for (let key in audioTrackSent) {
            audioTrackSent[key].enabled = false;
        }
        audioButt.innerHTML = `<i class="fas fa-microphone-slash"></i>`;
        audioAllowed = 0;
        audioButt.style.backgroundColor = "#b12c2c";
        if (mystream) {
            mystream.getTracks().forEach(track => {
                if (track.kind === 'audio')
                    track.enabled = false;
            })
        }

        mymuteicon.style.visibility = 'visible';
        sendToServer({
            type: 'action',
            data: 'mute',
        })
        // socket.emit('action', 'mute');
    } else {
        for (let key in audioTrackSent) {
            audioTrackSent[key].enabled = true;
        }
        audioButt.innerHTML = `<i class="fas fa-microphone"></i>`;
        audioAllowed = 1;
        audioButt.style.backgroundColor = "#4ECCA3";
        if (mystream) {
            mystream.getTracks().forEach(track => {
                if (track.kind === 'audio')
                    track.enabled = true;
            })
        }

        mymuteicon.style.visibility = 'hidden';
        sendToServer({
            type: 'action',
            data: 'unmute',
        })
        // socket.emit('action', 'unmute');
    }
})

//receive message from server
socket.onmessage = async function (msg) {
    let message = JSON.parse(msg.data);
    switch (message.type) {
        case "text":
            log('Text message from ' + message.from + ' received: ' + message.data);
            chatRoom.scrollTop = chatRoom.scrollHeight;
            chatRoom.innerHTML += `<div class="message">
                    <div class="info">
                        <div class="username">${message.from}</div>
                        <div class="time">${getTime()}</div>
                    </div>
                    <div class="content">
                        ${message.data}
                    </div>
                </div>`
            break;
        case "offer":
            log('Signal OFFER received');
            log(message)
            handleVideoOffer(message.offer, message.sid, message.cname, message.micinf, message.vidinf)
            break;
        case "answer":
            log('Signal ANSWER received');
            handleVideoAnswer(message.answer, message.sid)
            break;
        case "ice":
            log('Signal ICE Candidate received');
            handleNewIceCandidate(message.candidate, message.sid)
            break;
        case "join":
            log("join #" + message.data);
            log(message)
            let conc = JSON.parse(message.conc)
            let socketName = JSON.parse(message.data)
            let micInfoP = JSON.parse(message.micinf)
            let videoInfoP = JSON.parse(message.vidinf)
            await joinHandle(conc, socketName, micInfoP, videoInfoP)
            break;
        case "action":
            let msg = message.data;
            let sid = message.sid;
            if (msg == 'mute') {
                console.log(sid + ' muted themself');
                document.querySelector(`#mute${sid}`).style.visibility = 'visible';
                micInfo[sid] = 'off';
            } else if (msg == 'unmute') {
                console.log(sid + ' unmuted themself');
                document.querySelector(`#mute${sid}`).style.visibility = 'hidden';
                micInfo[sid] = 'on';
            } else if (msg == 'videooff') {
                console.log(sid + 'turned video off');
                document.querySelector(`#vidoff${sid}`).style.visibility = 'visible';
                videoInfo[sid] = 'off';
            } else if (msg == 'videoon') {
                console.log(sid + 'turned video on');
                document.querySelector(`#vidoff${sid}`).style.visibility = 'hidden';
                videoInfo[sid] = 'on';
            }
            break;
        case "leave":
            log("leave #" + message.from);
            chatRoom.scrollTop = chatRoom.scrollHeight;
            chatRoom.innerHTML += `<div class="message">
                    <div class="info">
                        <div class="username" style="color: red">Bot</div>
                        <div class="time">${getTime()}</div>
                    </div>
                    <div class="content">
                        ${message.from} leave!
                    </div>
                </div>`
            let sidLeave = message.data;
            if (document.getElementById(sidLeave)) {
                document.getElementById(sidLeave).remove();
            }
            delete connections[sidLeave];
            break;

        case "user count":
            let sizeUser = message.data;
            log("user count" + sizeUser)
            if (sizeUser > 1) {
                videoContainer.className = 'video-cont';
            } else {
                videoContainer.className = 'video-cont-single';
            }
            break;
        default:
            handleErrorMessage('Wrong type message received from server');
    }
}

cutCall.addEventListener('click', () => {
    location.href = '/';
})

//event click send message
sendButton.addEventListener('click', () => {
    const msg = messageField.value;
    messageField.value = '';
    sendToServer({
        from: username,
        type: 'text',
        data: msg,
    });
    chatRoom.scrollTop = chatRoom.scrollHeight;
    chatRoom.innerHTML += `<div class="message">
                    <div class="info">
                        <div class="username">You</div>
                        <div class="time">${getTime()}</div>
                    </div>
                    <div class="content">
                        ${msg}
                    </div>
                </div>`

});

//event enter keyup on message field
messageField.addEventListener("keyup", function (event) {
    if (event.keyCode === 13) {
        event.preventDefault();
        sendButton.click();
    }
});

//------------------util function------------------------------
// use JSON format to send WebSocket message
function sendToServer(msg) {
    let msgJSON = JSON.stringify(msg);
    socket.send(msgJSON);
}

//print log
function log(message) {
    console.log(message);
}

function handleErrorMessage(message) {
    console.error(message);
}

function reportError(e) {
    console.log(e);
    return;
}

function handleGetUserMediaError(e) {
    switch (e.name) {
        case "NotFoundError":
            alert("Unable to open your call because no camera and/or microphone" +
                "were found.");
            break;
        case "SecurityError":
        case "PermissionDeniedError":
            break;
        default:
            alert("Error opening your camera and/or microphone: " + e.message);
            break;
    }

}

//get time js
function getTime() {
    return new Date().toLocaleTimeString();
}