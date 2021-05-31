const express = require('express');
const bodyParser = require('body-parser');
const mongoose = require('mongoose');

// utils
const authenticator = require('../../utils/authenticator');

// models
const Guardian = require('../../models/guardian');
const Activity = require('../../models/activity');

// connect db
mongoose.connect(
  "mongodb://localhost/ecd",
  {
    useUnifiedTopology: true,
    useNewUrlParser: true,
  },
  (err) => {
    if (err) {
      console.log("Err connecting to DB:");
      console.log(err);
    } else {
      console.log("Connected to database");
    }
  },
);

const app = express.Router()

app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));

// takes in the email and hands out an id and the user object created
app.post("/new", (request, response) => {
  const {email, password, name} = request.body;
  const _id = authenticator.generateUserId(email);
  const hashedPassword = authenticator.generateUserId(password);

  const newGuardian = new Guardian({_id, email, name, password: hashedPassword});
  console.log(`Signing up new user: \n\t${JSON.stringify(newGuardian.toObject(), null, 2)}`)

  // create new guardian
  newGuardian.save((err) => {
    if (err) {
      console.log(err);
      response.status(500).json({_id: null, email: null, profiles: null});
    } else {
      const {_id, email, name} = newGuardian.toObject();
      console.log(`response OK`);
      response.status(201).json({_id, email, name});
    }
  });
});


app.post("/login", (request, response) => {
  const {email} = request.body;
  const inputPassword = request.body.password;

  Guardian.findOne({email}, (err, result) => {
    if (err) {
      response
        .status(500)
        .json({
          message: "Internal Server Error occured while handling request",
        });
    } else {
      if (result) {
        const {password} = result;
        const hashedPassword = authenticator.generateUserId(inputPassword);
        if (hashedPassword == password) {
          response
            .status(200)
            .json(result.toObject());
        } else {
          response
            .status(401)
            .json({
              message: "Unauthorized. Unable to authenticate using given credentials",
            });
        }
      } else {
        response
          .status(401)
          .json({
            message: "Unauthorized. Unable to authenticate usig given credentials",
          });
      }
    }
  });
});


app.get(
  "/profiles",
  authenticator.authenticateUserTokens,
  authenticator.authenticateGuardianToken,
  (request, response) => {
    const guardian = request.guardianToken;

    Guardian.find({guardian}, (err, results) => {
      if (err) {
        response
          .status(500)
          .json({
            message: "Internal Server error occured while handling request",
          });
      } else {
        if (results) {
          response
            .status(200)
            .json(results.map(res => res.toObject()));
        } else {
          response
            .status(200)
            .json([]);
        }
      }
    });
  },
);


app.get(
  "/activity",
  authenticator.getUserTokens,
  authenticator.authenticateUserTokens,
  (request, response) => {
    const {userToken} = request;

    Activity.find({user: userToken}, (err, res) => {
      if (err) {
        response
          .status(500)
          .json({message: "Internal Server Error"});
      } else {
        if (res) {
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
)

module.exports = app;
