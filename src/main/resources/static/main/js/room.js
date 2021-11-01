const myVideo = document.querySelector("#vd1");
const roomId = document.querySelector(".roomcode").innerText;
let username;
let myIp = document.querySelector('[name="ip"]').value;
let myId = document.querySelector('[name="userId"]').value;
let auth = document.querySelector('[name="auth"]').value;
const chatRoom = document.querySelector('.chat-cont');
const sendButton = document.querySelector('.chat-send');
const messageField = document.querySelector('.chat-input');
const videoContainer = document.querySelector('#vcont');

const videoButt = document.querySelector('.novideo');
const audioButt = document.querySelector('.audio');
const cutCall = document.querySelector('.cutcall');
const attendiesList = document.querySelector('#attendiesBox');

const screenShareButt = document.querySelector('.screenshare');
const whiteboardButt = document.querySelector('.board-icon')

//whiteboard js start
const whiteboardCont = document.querySelector('.whiteboard-cont');
const canvas = document.querySelector("#whiteboard");
const ctx = canvas.getContext('2d');

let boardVisisble = false;

whiteboardCont.style.visibility = 'hidden';

let isDrawing = 0;
let x = 0;
let y = 0;
let color = "black";
let drawsize = 3;
let colorRemote = "black";
let drawsizeRemote = 3;

function fitToContainer(canvas) {
    canvas.style.width = '100%';
    canvas.style.height = '100%';
    canvas.width = canvas.offsetWidth;
    canvas.height = canvas.offsetHeight;
}

fitToContainer(canvas);

function setColor(newcolor) {
    color = newcolor;
    drawsize = 3;
}

function setEraser() {
    color = "white";
    drawsize = 10;
}

//might remove this
function reportWindowSize() {
    fitToContainer(canvas);
}

window.onresize = reportWindowSize;


let videoAllowed = 1;
let audioAllowed = 1;

let micInfo = {};
let videoInfo = {};

let videoTrackReceived = {};

let mymuteicon = document.querySelector("#mymuteicon");
mymuteicon.style.visibility = 'hidden';

let myvideooff = document.querySelector("#myvideooff");
myvideooff.style.visibility = 'hidden';

const configuration = {
    iceServers: [{urls: 'stun:stun.l.google.com:19302'}]
    //iceServers: [{urls: ["stun:hk-turn1.xirsys.com"]}, {
    //    username: "3GEDCOyjRSCgUjFFoqhuT2aPxelcw4uAH4aEzK2f40YPdCiyUJD5c24yxZVSbZ0eAAAAAGFyncR0YW5pdXRhbg==",
    //    credential: "a2312990-3329-11ec-aeb7-0242ac120004",
    //    urls: ["turn:hk-turn1.xirsys.com:80?transport=udp", "turn:hk-turn1.xirsys.com:3478?transport=udp", "turn:hk-turn1.xirsys.com:80?transport=tcp", "turn:hk-turn1.xirsys.com:3478?transport=tcp", "turns:hk-turn1.xirsys.com:443?transport=tcp", "turns:hk-turn1.xirsys.com:5349?transport=tcp"]
    //}]
}

const mediaConstraints = {video: true, audio: true};

let connections = {};
let cName = {};
let audioTrackSent = {};
let videoTrackSent = {};

let mystream;
username = document.getElementById("myname").innerText
let peerConnection;


// const overlayContainer = document.querySelector('#overlay')
// overlayContainer.style.visibility = 'hidden';

function CopyClassText() {
    var textToCopy = document.querySelector('.roomcode');
    var currentRange;
    if (document.getSelection().rangeCount > 0) {
        currentRange = document.getSelection().getRangeAt(0);
        window.getSelection().removeRange(currentRange);
    } else {
        currentRange = false;
    }
    var CopyRange = document.createRange();
    CopyRange.selectNode(textToCopy);
    window.getSelection().addRange(CopyRange);
    document.execCommand("copy");

    window.getSelection().removeRange(CopyRange);

    if (currentRange) {
        window.getSelection().addRange(currentRange);
    }

    document.querySelector(".copycode-button").textContent = "Copied!"
    setTimeout(() => {
        document.querySelector(".copycode-button").textContent = "Copy Code";
    }, 5000);
}

//establish socket
const socket = new WebSocket("ws://" + window.location.host + "/signal");

// send a message to the server to join selected room with Web Socket
socket.onopen = function () {
    log('WebSocket connection opened to Room: #' + roomId);
    sendToServer({
        from: username,
        type: 'join',
        data: {
            roomId: roomId,
            ip: myIp,
            id: myId,
            auth: auth,
            time: getTime(),
        },
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
            myVideo.srcObject = localStream;
            myVideo.muted = true;
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
};

async function joinHandle(conc, cnames, micinfo, videoinfo) {
    sendToServer({
        from: username,
        type: "getCanvas",
        data: {
            getCanvas: "yes",
        },
    });
    if (cnames)
        cName = cnames;
    if (micinfo)
        micInfo = micinfo;
    if (videoinfo)
        videoInfo = videoinfo;
    console.log(cName);
    if (conc.length > 0) {
        await conc.forEach(sid => {
            connections[sid] = new RTCPeerConnection(configuration);
            connections[sid].onicecandidate = function (event) {
                if (event.candidate) {
                    console.log('icecandidate fired');
                    sendToServer({
                        from: username,
                        type: 'ice',
                        data: {
                            candidate: {
                                sdpMLineIndex: event.candidate.sdpMLineIndex,
                                candidate: event.candidate.candidate,
                            },
                            sid: sid,
                        },
                    });
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
                            from: username,
                            type: 'offer',
                            data: {
                                offer: connections[sid].localDescription,
                                sid: sid,
                            },
                        });
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
                myVideo.srcObject = localStream;
                myVideo.muted = true;
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
                data: {
                    candidate: {
                        sdpMLineIndex: event.candidate.sdpMLineIndex,
                        candidate: event.candidate.candidate,
                    },
                    sid: sid,
                },
            });
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
                    data: {
                        offer: connections[sid].localDescription,
                        sid: sid,
                    },
                });

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
                data: {
                    answer: connections[sid].localDescription,
                    sid: sid,
                },
            });
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
            from: username,
            type: 'action',
            data: {
                action: 'videooff',
            },
        });
    } else {
        for (let key in videoTrackSent) {
            videoTrackSent[key].enabled = true;
        }
        videoButt.innerHTML = `<i class="fas fa-video"></i>`;
        videoAllowed = 1;
        videoButt.style.backgroundColor = "#1a73e8";
        if (mystream) {
            mystream.getTracks().forEach(track => {
                if (track.kind === 'video')
                    track.enabled = true;
            })
        }


        myvideooff.style.visibility = 'hidden';
        sendToServer({
            from: username,
            type: 'action',
            data: {
                action: 'videoon',
            },
        });
    }
});

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
        ;

        mymuteicon.style.visibility = 'visible';
        sendToServer({
            from: username,
            type: 'action',
            data: {
                action: 'mute',
            },
        });
    } else {
        for (let key in audioTrackSent) {
            audioTrackSent[key].enabled = true;
        }
        audioButt.innerHTML = `<i class="fas fa-microphone"></i>`;
        audioAllowed = 1;
        audioButt.style.backgroundColor = "#1a73e8";
        if (mystream) {
            mystream.getTracks().forEach(track => {
                if (track.kind === 'audio')
                    track.enabled = true;
            })
        }

        mymuteicon.style.visibility = 'hidden';
        sendToServer({
            from: username,
            type: 'action',
            data: {
                action: 'unmute',
            },
        });
    }
})

//receive message from server
socket.onmessage = async function (msg) {
    let message = JSON.parse(msg.data);
    let dataBody = JSON.parse(message.data);
    switch (message.type) {
        case "text":
            log('Text message received')
            chatRoom.scrollTop = chatRoom.scrollHeight;
            chatRoom.innerHTML += `<div class="message">
                    <div class="info">
                        <div class="username">${message.from}</div>
                        <div class="time">${getTime()}</div>
                    </div>
                    <div class="content">
                        ${dataBody.message}
                    </div>
                </div>`
            break;
        case "offer":
            log('Signal OFFER received');
            handleVideoOffer(dataBody.offer, dataBody.sid, dataBody.cname, dataBody.micinf, dataBody.vidinf)
            break;
        case "answer":
            log('Signal ANSWER received');
            handleVideoAnswer(dataBody.answer, dataBody.sid)
            break;
        case "ice":
            log('Signal ICE Candidate received');
            handleNewIceCandidate(dataBody.candidate, dataBody.sid)
            break;
        case "join":
            log("join #" + dataBody);
            let conc = dataBody.conc
            let socketName = dataBody.socketName
            let micInfoP = dataBody.micinf
            let videoInfoP = dataBody.vidinf
            await joinHandle(conc, socketName, micInfoP, videoInfoP)
            break;
        case "action":
            let msg = dataBody.action;
            let sid = dataBody.sid;
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
            let sidLeave = dataBody.sid;
            if (document.getElementById(sidLeave)) {
                document.getElementById(sidLeave).remove();
            }
            delete connections[sidLeave];
            break;

        case "user count":
            let sizeUser = dataBody.userCount;
            if (sizeUser > 1) {
                videoContainer.className = 'video-cont';
            } else {
                videoContainer.className = 'video-cont-single';
            }
            let obj = JSON.parse(dataBody.listAttendies);
            attendiesList.innerHTML = '';
            for (const key in obj) {
                console.log(key + ' - ' + obj[key])
                attendiesList.innerHTML += `<div class="attendies">
                    <div class="info">
                        <div class="avatar">
                            <span>${getFirstChar(obj[key])}</span>
                        </div>
                        <div class="username">${obj[key]}</div>
                    </div>
                    <input type="hidden" value="${key}">
                </div>`;
            }

            break;

        case "getCanvas":
            let url = dataBody.url;
            let img = new Image();
            img.onload = start;
            img.src = url;

        function start() {
            ctx.drawImage(img, 0, 0);
        }
            break;

        case "clearBoard":
            ctx.clearRect(0, 0, canvas.width, canvas.height);
            break;

        case "draw":
            colorRemote = dataBody.color;
            drawsizeRemote = dataBody.size;
            drawRemote(dataBody.newx, dataBody.newy, dataBody.prevx, dataBody.prevy);
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
        data: {
            message: msg,
            time: getTime(),
        },
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
                </div>`;
});

//event enter keyup on message field
messageField.addEventListener("keyup", function (event) {
    if (event.keyCode === 13) {
        event.preventDefault();
        sendButton.click();
    }
});

//

function clearBoard() {
    if (window.confirm('Are you sure you want to clear board? This cannot be undone')) {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        let url = canvas.toDataURL()
        sendToServer({
            from: username,
            type: 'store canvas',
            data: {
                url: url,
            },
        });
        sendToServer({
            from: username,
            type: 'clearBoard',
            data: {
                clear: 'yes',
            },
        })
        // socket.emit('store canvas', canvas.toDataURL());
        // socket.emit('clearBoard');
    } else return;
}

// socket.on('clearBoard', () => {
//     ctx.clearRect(0, 0, canvas.width, canvas.height);
// })

function draw(newx, newy, oldx, oldy) {
    ctx.strokeStyle = color;
    ctx.lineWidth = drawsize;
    ctx.beginPath();
    ctx.moveTo(oldx, oldy);
    ctx.lineTo(newx, newy);
    ctx.stroke();
    ctx.closePath();
    sendToServer({
        from: username,
        type: "store canvas",
        data: {
            url: canvas.toDataURL(),
        },
    });
    // socket.emit('store canvas', canvas.toDataURL());
}

function drawRemote(newx, newy, oldx, oldy) {
    ctx.strokeStyle = colorRemote;
    ctx.lineWidth = drawsizeRemote;
    ctx.beginPath();
    ctx.moveTo(oldx, oldy);
    ctx.lineTo(newx, newy);
    ctx.stroke();
    ctx.closePath();
}

canvas.addEventListener('mousedown', e => {
    x = e.offsetX;
    y = e.offsetY;
    isDrawing = 1;
})

canvas.addEventListener('mousemove', e => {
    if (isDrawing) {
        draw(e.offsetX, e.offsetY, x, y);
        // socket.emit('draw', e.offsetX, e.offsetY, x, y, color, drawsize);
        sendToServer({
            from: username,
            type: "draw",
            data: {
                newx: e.offsetX,
                newy: e.offsetY,
                prevx: x,
                prevy: y,
                color: color,
                size: drawsize,
            },
        });
        x = e.offsetX;
        y = e.offsetY;
    }
})

window.addEventListener('mouseup', e => {
    if (isDrawing) {
        isDrawing = 0;
    }
})

whiteboardButt.addEventListener('click', () => {
    if (boardVisisble) {
        whiteboardCont.style.visibility = 'hidden';
        boardVisisble = false;
    } else {
        whiteboardCont.style.visibility = 'visible';
        boardVisisble = true;
    }
})

// use JSON format to send WebSocket message
function sendToServer(msg) {
    let msgJSON = JSON.stringify(msg);
    socket.send(msgJSON);
}

//------------------util function------------------------------
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


function getTime() {
    return new Date().toLocaleTimeString();
}

function getFirstChar(str) {
    return str.charAt(0);
}