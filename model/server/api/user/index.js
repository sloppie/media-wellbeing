const express = require('express');
const bodyParser = require('body-parser');
const multer = require('multer');

// models
const User = require('../../models/user');
const Activity = require('../../models/activity');

// utils
const authenticator = require('../../utils/authenticator');
const cdn = require('../../utils/cdn');

const app = express.Router();

// middleware
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));


app.post(
  '/link',
  authenticator.getUserTokens,
  authenticator.authenticateGuardianToken,
  (request, response) => {
    const {userToken, guardianToken} = request;
    const _id = userToken;
    const guardian = guardianToken;


    User.find({_id}, (err, res) => {
      if (err) {
        saveNewUser();
      } else {
        if (res !== null) {
          if (res.length > 0) {
            console.log(`User already exists: ${_id}`);

            const foundUser = res[0].toObject();
            console.log(`Returning user details: 200 OK`)
            response
              .status(200)
              .json({_id: foundUser._id, guardian: foundUser.guardian});
          } else {
            saveNewUser();
          }
        } else {
          saveNewUser();
        }
      }
    });

    const saveNewUser = () => {
      console.log(`Creating new user profile: ${_id}`)
      const newUser = new User({_id, guardian});

      newUser.save((err) => {
        if (err) {
          console.log(err);
          response.status(500).json({_id: null, email: null, name: null});
        } else {
          console.log("Successfully created (201)")
          response.status(201).json(newUser.toObject());
        }
      });
    };
  },
);


app.get(
  "/activity/reviewed",
  authenticator.getUserTokens,
  authenticator.authenticateUserTokens,
  (request, response) => {
    Activity
      .find({_id: request.userToken, status: "REVIEWED"}, (err, res) => {
        if (err) {
          response
            .status(500)
            .json({message: "Internal Server Error"});
        } else {
          if (response) {
            response
              .status(200)
              .json(res.map(result => result.toObject()));
          } else {
            response
              .status(200)
              .json([]);
          }
        }
      });
  },
);

module.exports = app;
