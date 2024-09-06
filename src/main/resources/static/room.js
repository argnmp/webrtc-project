let g_roomList = [];
const addRoomEvents = async () => {
    document.querySelector("#createRoom").addEventListener('click', async () => {
        let roomName = document.querySelector('#roomName').value;
        const res = await fetch(`${apiServer}/api/v1/room`, {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                "name": roomName,
            })
        })
        console.log('Hello', await res.json());
    })
}
const refreshRoom = async () => {
    const res = await fetch(`${apiServer}/api/v1/room`);
    g_roomList = await res.json();
}
const renderRoom = async (roomClickEvent) => {
    const roomList = document.querySelector("#roomList");
    while(roomList.lastChild){
        roomList.removeChild(roomList.lastChild);
    }

    for(data of g_roomList) {
        let li = document.createElement("li");
        let id = data.id;

        let s1 = document.createElement("span");
        s1.innerText = data.id + " : " + data.name + " ";
        s1.classList.add("cursor-pointer");
        s1.classList.add("hover:text-green-400");
        s1.addEventListener("click", async () => {
            await roomClickEvent(id);
        })


        let s2 = document.createElement("span");
        s2.addEventListener("click", async () => {
            await fetch(`${apiServer}/api/v1/room/${id}`, {
                method: "DELETE",
            })
        })
        s2.classList.add("cursor-pointer");
        s2.classList.add("hover:text-red-400");
        s2.innerText = "delete";

        li.appendChild(s1)
        li.appendChild(s2)
        roomList.appendChild(li)
    }
}