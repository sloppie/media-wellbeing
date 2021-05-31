const express = require('express');
const bodyParser = require('body-parser');
const multer = require('multer');
const path = require('path');

// models
const Activity = require('../../models/activity');

// utils
const authenticator = require('../../utils/authenticator');
const cdn = require('../../utils/cdn');

const app = express.Router();

// middleware
app.use(bodyParser.json({limit: "50mb"}));
app.use(bodyParser.urlencoded({extended: false, limit: "50mb"}));


app.get(
  "/",
  authenticator.getUserTokens,
  authenticator.authenticateUserTokens,
  (request, response) => {
    const fileId = request.query.file_id;

    Activity.findById(fileId, (err, res) => {
      if (err) {
        response
          .status(500)
          .json({
            message: "Internal Server Error. An error occured while handling the request",
          });
      } else {
        if (res) {
          response
            .status(200)
            .json(res.toObject());
        } else {
          response
            .status(400)
            .json({
              message: `Couldnt access the file with id: ${fileId}. FileNotPresent`,
            });
        }
      }
    });
  },
)


app.post(
  "/",
  authenticator.getUserTokens,
  authenticator.authenticateUserTokens,
  (request, response) => {
    console.log("Uploading file for review");
    const {guardianToken, userToken} = request;

    const upload = multer({
      storage: cdn.getStorage(
        path.join(__dirname, "..", "..", "..", "activity-buffer"),
        request.query.file_id, // to avoid duplication
      ),
    }).single("file");

    upload(request, response, (err) => {
      if (err) {
        response
          .status(500)
          .json({message: "Internal Server Error. An error occured while uploading file"});
      } else {
        console.log(`Adding file ${request.file.filename}`);
        const activity = new Activity({
          _id: request.query.file_id,
          file_id: request.query.file_id,
          file_name: request.file.filename, // will help retreive the file in case of need
          date: new Date(),
          status: "PENDING_REVIEW",
          guardian: guardianToken,
          user: userToken,
          label: 0,  // implicit label from intent to still want to view image
        });
    
        activity.save((err) => {
          if (err) {
            response
              .status(500)
              .json({
                message: "Internal Server Error. An internal server error occurred."
              });
          } else {
            response.status(201).json(activity.toObject());
          }
        });
      }
    });
  },
);


app.put(
  "/review",
  authenticator.getUserTokens,
  authenticator.authenticateGuardianToken,
  (request, response) => {
    Activity.findByIdAndUpdate(
      request.body.file_id,
      {
        label: request.body.label,
        status: "REVIEWED",
      },
      (err, res) => {
        if (err) {
          response
            .status(500)
            .json({
              message: "Internal Server Error. A server error occured while handling request",
            });
        } else {
          if (res) {
            response
              .status(200)
              .json(res.toObject());
          } else {
            response
              .status(200)
              .json(request.body); // return what they sent as a query
          }
        }
      },
    );
  },
);

module.exports = app;
