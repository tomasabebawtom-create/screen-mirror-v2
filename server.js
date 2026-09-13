const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 8080 });
console.log('Signaling server running on port 8080');

wss.on('connection', function connection(ws) {
  console.log('Client connected');
  ws.on('message', function incoming(data) {
    wss.clients.forEach(function each(client) {
      if (client !== ws && client.readyState === WebSocket.OPEN) {
        client.send(data);
      }
    });
  });
  ws.on('close', function() {
    console.log('Client disconnected');
  });
});
