const net = require('net');
const path = require('path');
const express = require('express');
const multer = require('multer');
const bodyParser = require('body-parser');

// utils
const SockBuffer = require('./utils/sock-buffer');
const ModelServerSock = require('./utils/model-server-sock');

// proto-buffers
const modelio = require('./proto-buffers/modelio_pb');
const {getStorage} = require('./utils/cdn');

const HOST = '127.0.0.1';
const PORT = 9000;

// socket connection set up
const modelServerSock = new ModelServerSock(); // socket is set once the server is initialized.
const sockBuffer = new SockBuffer(modelServerSock);

// express server app.
const app = express();

// server middleware
app.use(bodyParser.json({limit: "50mb"}));
app.use(bodyParser.urlencoded({limit: "50mb", extended: false}));


// start socket connection on PORT and HOST
net.createServer((socket) => {
  console.log('CONNECTED: ' + socket.remoteAddress + ':' + socket.remotePort);
  modelServerSock.setSocket(socket);

  socket.on('data', function (data) {
    const modelResponse = modelio.ModelResponse.deserializeBinary(data);
    // based on the request type, resolve appropriately:
    if (modelResponse.getRequestType() == 1) {
      // send the resolved ModelResponse to allow response send
      sockBuffer.resolveRequest(modelResponse);
    } else {
      // send the parse request to the parse request buffer
      sockBuffer.resolveParseRequest(modelResponse);
    }
  });

  // Add a 'close' event handler to this instance of socket
  socket.on('close', function () {
    console.log('Socket closed. No further ECD tasks can be carried out.');
  });
}).listen(PORT, HOST);


console.log('Socket connection instantiated on PORT ' + PORT);

app.get("/", (request, response) => {
  response.send("<h1>Hello World</h1>")
});

app.post('/ecd/scan/:user_id', (request, response) => {
  const userId = request.params.user_id;
  let contentType = null;
  const setContentType = (mimetype) => {
    console.log(mimetype);
    if (/jpeg|jpg|png/g.test(mimetype)) {
      contentType = 1;
    } else if (/mp4/g.test(mimetype)) {
      contentType = 3;
    } else if (/gif/g.test(mimetype)) {
      contentType = 2;
    }
  };

  const fileName = `${userId}_${Date.now()}`;

  const upload = multer({
    storage: getStorage(path.join(__dirname, "..", "image-buffer"), fileName, setContentType),
  }).single("file");

  upload(request, response, (err) => {
    if (err) {
      console.log(err)
    } else {
      // send out request to the model
      /**@type {import('../utils/sock-buffer').ModelArgs} */
      const modelArgs = new modelio.ModelArgs();
      modelArgs.setFileName(request.file.filename);
      modelArgs.setMediaType(contentType? contentType: 1);
      // callback to be executed on successful response
      const onResponse = (modelResponse) => {
        console.log("Sending back response: " + modelResponse.getIsExplicit());
        response.status(200)
          .json({
            isExplicit: `${modelResponse.getIsExplicit()}`,
            file_name: request.file.filename,
          });
      };
      sockBuffer.addRequest(modelArgs, onResponse);
    }
  });
});


app.get("/ecd/parse/:file_name", (request, response) => {
  const parseArgs = new modelio.parseArgs();
  parseArgs.setFileName(request.params.file_name);

  const onResponse = () => {
    response.status(200).sendFile(
      path.join(__dirname, "..", "out-buffer", request.params.file_name));
  };

  sockBuffer.addParseRequest(onResponse);
});

app.listen(5000, () => {
  console.log("Server listening at port 5000");
});
