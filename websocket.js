const PACKET={
    ACCEPT:"{%ACPT%}",
    INFO:"{%INF%}",
    INFOPACKET:("{%INFPKT%}"),
    READY:"{%RDY%}",
    ERROR:"{%ERR%}"
}

const wsUrl="wss://YOUR.DOMAIN/yampb/dl";
var ws=null;

function log(msg) {
    const ta=document.getElementById('log');
    ta.value+=(ta.value?'\n':'')+msg;
    ta.selectionStart=ta.value.length;
    ta.selectionEnd=ta.value.length;
    ta.scrollTop=ta.scrollHeight;
}

document.getElementById("sendBtn").onclick=()=> {
    var content=document.getElementById("msg").value;
    if(!ws||ws.readyState===WebSocket.CLOSED) {
        ws=new WebSocket(wsUrl);
        ws.onopen=()=> {
            if(content) ws.send(content);
        }

        ws.onmessage=(event)=> {
            var answer=String(event.data).trim();

            if(answer.startsWith(PACKET.ACCEPT)) {
                log("Prepare download....");
                document.getElementById('sendBtn').style.display='none';
                document.getElementById('spinner').style.display='inline-block';
                document.getElementById('bigSpinner').style.display='flex';
            } else if(answer.startsWith(PACKET.INFOPACKET)) {
                var packet=answer.replace(PACKET.INFOPACKET,"");
                log(packet);
                try {
                    var data=JSON.parse(packet);
                    document.getElementById('title').textContent=data.title;
                    document.getElementById('thumbnail').src=data.thumbnail;
                    document.getElementById('description').textContent=data.description;
                    document.getElementById('bigSpinner').style.display='none';
                    document.getElementById('bigSpinner').style.setProperty('display','none','important');                    
                    document.getElementById('infoBox').style.display='block';
                } catch(e) {
                    log("⚠️ Error: "+e);
                }
            } else if(answer.startsWith(PACKET.READY)) {
                var link=answer.replace(PACKET.READY,"");
                document.getElementById('bigSpinner').style.display='none';
                document.getElementById('bigSpinner').style.setProperty('display','none','important');
                document.getElementById('downloadBtn').style.display='inline-block';
                document.getElementById('downloadBtn').onclick=function() {
                    window.location.href=link;
                };
                if(ws) ws.close();
            } else if(answer.startsWith(PACKET.INFO)) {
                answer=answer.replace(PACKET.INFO,"");
                log(answer);
            } else if(answer.startsWith(PACKET.ERROR)) {
                document.getElementById('sendBtn').style.display='inline-block';
                document.getElementById('spinner').style.display='none';
                document.getElementById('bigSpinner').style.display='none';
                document.getElementById('bigSpinner').style.setProperty('display','none','important');                    
                answer = answer.replace(PACKET.ERROR,"");
                log(answer);
                if(ws) ws.close();
            }

        }

        ws.onclose=(close)=> {
            document.getElementById('sendBtn').style.display='inline-block';
            document.getElementById('spinner').style.display='none';
            document.getElementById('bigSpinner').style.display='none';
            document.getElementById('bigSpinner').style.setProperty('display','none','important');                    
            log("❌ Closed: "+close.reason);
        }
        ws.onerror=(error)=> {
            document.getElementById('sendBtn').style.display='inline-block';
            document.getElementById('spinner').style.display='none';
            document.getElementById('bigSpinner').style.display='none';
            document.getElementById('bigSpinner').style.setProperty('display','none','important');                    
            log("⚠️ Error: "+error.type);
        }

    } else {
        log("⚠️ Websocket already in use.");
    }
};
