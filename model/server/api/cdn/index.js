const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');

const app = express.Router();

// middleware
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));


// fetcht the image based on the image name offered to the client
// no authorization is required to make sure that the image is fetchable through
// plain urls
app.get(
  "/photo/:file_name",
  (request, response) => {
    const fileName = request.params.file_name;

    response
      .status(200)
      .sendFile(path.join(__dirname, "..", "..", "..", "activity-buffer", fileName));
  },
);

module.exports = app;
