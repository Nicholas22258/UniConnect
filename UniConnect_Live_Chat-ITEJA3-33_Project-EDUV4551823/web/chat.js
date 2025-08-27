//Nicholas Leong EDUV4551823

let socket;

function connect(){
    socket = new websocket("ws://localhost:8080/UniConnect_Live_Chat-ITEJA_Project-EDUV4551823-1.0-SNAPSHOT/chatEndpoint");
    
    socket.onopen = function(){
        document.getElementById("listOfConversations").style.display = "hidden";
        document.getElementById("chat-window").style.display = "block";
        document.getElementById("chat-window").style.overflowY = 'scroll'; 
    };
    
    socket.onmessage = function(event){
        let chatWindow = document.getElementById("chat-window");
        let message = document.createElement("div");
        message.textContent = event.data;
        chatWindow.appendChild(message);
        chatWindow.scrollTop = chatWindow.scrollHeight;
    };
    
    socket.onclose = function(){
        
    };
}

function onMessage(){
    let messageInput = document.getElementById("inputMessage");
    let message = messageInput.value;
    socket.send(message);
    messageInput.value = "";
}

window.onload = function(){
    connect();
};
