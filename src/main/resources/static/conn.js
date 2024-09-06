const apiServer = "";
let iceServers = {
    iceServers: [
    {
        urls: 'stun:stun.l.google.com:19302',
    },
    {
        urls: "turn:turn.argnmp.com:3478",
        username: "hycord",
        credential: "hycord4321",
    },
],

}
let g_key = "";
let g_name = "";
let g_roomId = "";
let g_room_keys = [];
let stompClient = {};
stompClient.unsubscribeAll = async () => {};

// stream setting
let g_stream_local = undefined;
let g_conn = new Map(); // connection to remote key


const requestUserKey = async () => {
    g_name = prompt("enter your input");
    const res = await fetch(`${apiServer}/api/v1/user`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            "name": g_name,
        })
    });
    const data = await res.json();
    console.log(data);
    g_key = data.key;
    alert(g_key);
}
const initSocketConnection = () => {
    const socket = new SockJS(`${apiServer}/signal`);
    stompClient.current = Stomp.over(socket);
    stompClient.current.debug = null;
    stompClient.current.connect({}, function () {
        stompClient.current.subscribe(`/topic/common`, async data => {
            let message = JSON.parse(data.body);
            if(message.type==='ROOM_DELETE' && message.id === g_roomId){
                stompClient.current.send(`/app/room/${g_roomId}/disconnect`, {}, JSON.stringify(g_key))
                await resetAndEnterRoom();
                await subscribeRoom();
            }
            await refreshRoom();
            await renderRoom(async (id)=> {
                stompClient.current.send(`/app/room/${g_roomId}/disconnect`, {}, JSON.stringify(g_key))
                await resetAndEnterRoom(id);
            });
        })
    });
}

const resetAndEnterRoom = async (roomId) => {
    alert(`enter room: ${roomId}`);
    // reset all states
    await stompClient.unsubscribeAll();
    g_roomId = null;
    g_room_keys = [];
    const chatBox = document.querySelector("#chatBox");
    while(chatBox.lastChild){
        chatBox.removeChild(chatBox.lastChild);
    }
    // close current connections
    for(let conn of g_conn.values()){
        conn.close();
    }
    g_conn = new Map();
    const remoteStreams = document.querySelector("#remoteStreams");
    while(remoteStreams.lastChild){
        remoteStreams.removeChild(remoteStreams.lastChild);
    }

    await stopLocalStream();

    if(roomId){
        g_roomId = roomId;
        // initialize cam
        await initLocalStream();
    }

}
const subscribeRoom = () => {
    // subscribe room socket endpoints
    const roomSubscribeList = [];
    let sub= stompClient.current.subscribe(`/topic/room/${g_roomId}/key/req`, async msg => {
        stompClient.current.send(`/app/room/${g_roomId}/key/res`, {}, JSON.stringify(g_key))
    })
    roomSubscribeList.push(sub);
    sub = stompClient.current.subscribe(`/topic/room/${g_roomId}/key/res`, async msg => {
        let key = JSON.parse(msg.body);
        if(key !== g_key && !g_room_keys.find((k)=>k===key)) {
            // 여기서 나타나는 이벤트로 새로운 유저가 방에 입장했는지를 알 수 있다.
            g_room_keys.push(key);
            //alert(`user connected: ${key}`);
        }
    })
    roomSubscribeList.push(sub);
    sub = stompClient.current.subscribe(`/topic/room/${g_roomId}/disconnect`, async msg => {
        let key = JSON.parse(msg.body);
        console.log(key);
        // 여기서 나타나는 이벤트로 어떤 유저가 room을 나갔는지 알 수 있다.
        // webrtc의 경우에는 이 이벤트가 아닌, webrtc 자체의 이벤트로 해당 peer가 나간것을 처리할 수 있다.
        if(key !== g_key) {
            let idx = g_room_keys.findIndex((k) => k === key);
            if(idx !== -1){
                g_room_keys.splice(idx, 1);
                alert(`user disconnected: ${key}`)
            }
        }
    })
    roomSubscribeList.push(sub);
    sub = stompClient.current.subscribe(`/topic/room/${g_roomId}/chat`, async msg => {
        let data = JSON.parse(msg.body);
        let chatBox = document.querySelector("#chatBox");
        const line = document.createElement("div");
        line.innerText = `${data.userId} : ${data.name} : ${data.message} : ${data.timestamp}`;
        chatBox.appendChild(line);
    })
    roomSubscribeList.push(sub);

    // webrtc signaling

    // remote host offered to connection
    sub = stompClient.current.subscribe(`/topic/room/${g_roomId}/offer/${g_key}`, msg => {
        let data = JSON.parse(msg.body);
        let remoteKey = data.key;
        let remoteDesc = data.desc;
        let conn = createConnection(remoteKey);
        conn.setRemoteDescription(new RTCSessionDescription({type: remoteDesc.type, sdp: remoteDesc.sdp}));
        g_conn.set(remoteKey, conn);

        console.log("topic offer");

        // send answer
        conn.createAnswer().then(answer => {
            console.log("send answer")
            stompClient.current.send(`/app/room/${g_roomId}/answer/${remoteKey}`, {}, JSON.stringify({
                key: g_key,
                desc: answer
            }))
            conn.setLocalDescription(answer);
        })
    });
    roomSubscribeList.push(sub);

    // local host offered to remote host, and remote host answered to it
    sub = stompClient.current.subscribe(`/topic/room/${g_roomId}/answer/${g_key}`, msg => {
        let data = JSON.parse(msg.body);
        let remoteKey = data.key;
        let remoteDesc = data.desc;

        console.log("topic answer");
        // because this is the answer from the remote host that local host offered
        let conn = g_conn.get(remoteKey)
        conn.setRemoteDescription(new RTCSessionDescription(remoteDesc));
    })
    roomSubscribeList.push(sub);

    // now exchanging descriptions are finished
    // endpoint for receiving ice candidate
    sub = stompClient.current.subscribe(`/topic/room/${g_roomId}/ice/${g_key}`, msg => {
        let data = JSON.parse(msg.body);
        let remoteKey = data.key;
        let remoteCand = data.data;

        let conn = g_conn.get(remoteKey);
        conn.addIceCandidate(new RTCIceCandidate({
            candidate: remoteCand.candidate,
            sdpMLineIndex: remoteCand.sdpMLineIndex,
            sdpMid: remoteCand.sdpMid,
        }))
    })
    roomSubscribeList.push(sub);

    stompClient.unsubscribeAll = async () => {
        for(sub of roomSubscribeList) {
            await sub.unsubscribe();
        }
    }

}
const initRemoteKeys = () => {
    stompClient.current.send(`/app/room/${g_roomId}/key/req`, {}, {})
}
const initLocalStream = async () => {
    if(navigator.mediaDevices !== undefined){
        await navigator.mediaDevices.getUserMedia({ audio: true, video : true })
            .then(async (stream) => {
                g_stream_local = stream;
                stream.getAudioTracks()[0].enabled = true;
                let localStream = document.querySelector("#localStream");
                const localStreamVideo = document.createElement('video');
                localStreamVideo.autoplay = true;
                localStreamVideo.controls = true;
                localStreamVideo.srcObject = stream;
                localStream.appendChild(localStreamVideo);
            }).catch(error => {
                console.error("streamInit failed", error);
            });
    }
}
const stopLocalStream = async () => {
    // stop local stream
    if(g_stream_local !== undefined){
        const localStream = document.querySelector("#localStream");
        while(localStream.lastChild){
            localStream.removeChild(localStream.lastChild);
        }
        g_stream_local.getTracks().forEach(track => {
            track.stop();
        })
        g_stream_local = undefined
    }
}

const initRemoteStream = (stream, remoteKey) => {
    const wrapper = document.createElement('div');
    wrapper.id = `wrapper-${remoteKey}`;
    wrapper.innerText = `${remoteKey}`;

    const state = document.createElement(`span`);
    state.id = `state-${remoteKey}`;
    state.classList.add('text-red-400');
    wrapper.appendChild(state);

    const remoteStreamVideo = document.createElement('video');
    remoteStreamVideo.id = `track-${remoteKey}`;
    remoteStreamVideo.autoplay = true;
    remoteStreamVideo.controls = true;
    remoteStreamVideo.srcObject = stream;
    wrapper.appendChild(remoteStreamVideo);

    const remoteStreams = document.getElementById('remoteStreams');
    remoteStreams.appendChild(wrapper);
}

const stopRemoteStream = (remoteKey) => {
    const remoteStreamVideo = document.querySelector(`#track-${remoteKey}`);
    remoteStreamVideo.srcObject.getTracks().forEach(track => {
        track.stop();
    })
    const wrapper = document.querySelector(`#wrapper-${remoteKey}`);
    const remoteStreams = document.querySelector('#remoteStreams');
    remoteStreams.removeChild(wrapper);
}
const createConnection = (remoteKey) => {
    const conn = new RTCPeerConnection(iceServers);
    // this event is fired when local description is set
    conn.addEventListener('icecandidate', (e) => {
        if (e.candidate !== null/* && conn.currentRemoteDescription*/) {
            // send ice candidate to remote host
            stompClient.current.send(`/app/room/${g_roomId}/ice/${remoteKey}`, {}, JSON.stringify({
                key: g_key,
                data: e.candidate,
            }))
        }
    })
    conn.addEventListener('connectionstatechange', (e) => {
        let state = document.getElementById(`state-${remoteKey}`);
        state.innerText = conn.connectionState;
        switch(conn.connectionState) {
            case "new":
            case "connecting":
            case "connected":
                break;
            case "disconnected":
            case "failed":
            case "closed":
                g_conn.get(remoteKey).close();
                stopRemoteStream(remoteKey);
                g_conn.delete(remoteKey);
                break;
            default:
                break;
        }
    })
    conn.addEventListener('track', (e) => {
        if(document.getElementById(`track-${remoteKey}`) === null ){
            initRemoteStream(e.streams[0], remoteKey);
        }
    })
    if(g_stream_local !== undefined) {
        g_stream_local.getTracks().forEach(track => {
            conn.addTrack(track, g_stream_local);
        })
    }
    return conn;
}

const addStreamEvents = async () => {
    document.querySelector("#connectBtn").addEventListener('click', async () => {
        await stompClient.unsubscribeAll();
        subscribeRoom();
        // request remote keys
        initRemoteKeys();

        setTimeout(() => {
            console.log(g_room_keys);
            g_room_keys.map((key) => {
                if(!g_conn.has(key)){
                    let conn = createConnection(key);
                    g_conn.set(key, conn);

                    // send offer to remote keys
                    conn.createOffer().then(offer => {
                        conn.setLocalDescription(offer);
                        console.log(`send offer to ${key} in ${g_roomId}`);
                        stompClient.current.send(`/app/room/${g_roomId}/offer/${key}`, {}, JSON.stringify({
                            key: g_key,
                            desc: offer
                        }));
                    })
                }
            })
        }, 1000);
    })
    document.querySelector("#disconnectBtn").addEventListener('click', async () => {
        stompClient.current.send(`/app/room/${g_roomId}/disconnect`, {}, JSON.stringify(g_key))
        await resetAndEnterRoom();
        await subscribeRoom();
    })
    document.querySelector("#sendChatBtn").addEventListener('click', async () => {
        let userName = document.querySelector("#userName").value;
        let chatMessage = document.querySelector("#chatMessage").value;
        stompClient.current.send(`/app/room/${g_roomId}/chat`, {}, JSON.stringify({
            userId: g_key,
            name: userName,
            message: chatMessage,
        }))
    })
}

window.onload = async () => {
    await requestUserKey();
    await addRoomEvents();
    await refreshRoom();
    await renderRoom(async (id) => {
        stompClient.current.send(`/app/room/${g_roomId}/disconnect`, {}, JSON.stringify(g_key))
        await resetAndEnterRoom(id);
    });
    initSocketConnection();
    await addStreamEvents();
}