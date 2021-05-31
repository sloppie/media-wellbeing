const crypto = require('crypto');

// models
const Guardian = require('../models/guardian');
const User = require('../models/user');

/**
 * @typedef {import('express').Request} Request 
 * @typedef {import('express').Response} Response 
 * @typedef {import('express').NextFunction} NextFunction 
 */

/**
 * This function generates the user_id based on the email input to it.
 *
 * @param {string} email this is the email of the user to be authenticated.
 *
 * @return {string} returns the id based on the email passed into the function.
 */
const generateUserId = (email) => {
  const emailHash = crypto.createHash("sha256");
  emailHash.update(email);

  const id = emailHash.digest("hex");

  return id;
};


/**
 * Extracts the user tokens attached to the request in the authorization header.
 * These tokens are:
 * 1. `guardianToken` - this is the guardian paired to the user of this application
 * 2. 'userToken' - this is the user that is currently in session using this application.
 *
 * @param {Request} request the request object.
 * @param {Response} response the response object.
 * @param {NextFunction} next callback to the next middleware
 */
const getUserTokens = (request, response, next) => {
  const authorizationToken = request.headers.authorization.replace(/bearer\s+/i, "");
  const [guardianToken, ] = authorizationToken.split(":");
  console.log(`Guardian Token: ${guardianToken}`);
  console.log(`user Token: ${authorizationToken}`);

  request.guardianToken = guardianToken;
  // the user token is a concat between guardian and child account
  request.userToken = authorizationToken;

  if (guardianToken) {
    next(); // move to next middleware
  } else {
    response.status(401).json({message: "Unable to authenticate guardian account"});
  }
};


/**
 * Aftet the middleware before extracts all the tokens, this token interacts with the DB to make
 * sure that are parties are authenticated before proceeding to offer services.
 *
 * @param {Request} request the request object.
 * @param {Response} response the response object.
 * @param {NextFunction} next callback to the next middleware
 */
const authenticateUserTokens = (request, response, next) => {
  const {userToken, guardianToken} = request;

  Guardian.findById(guardianToken, (err, res) => {
    if (err) {
      response.status(500).json({message: "an error occurred while handling the request"});
    } else {
      if (res) {
        // guardian counterpart authenticated, now authenticate paired user app
        User.findById(userToken, (userErr, userRes) => {
          if (userErr) {
            response
              .status(500)
              .json({message: "Internal server error occurred while handling the request"});
          } else {
            if (userRes) {
              next(); // there exists such a user, as such, the client can proceed.
            } else {
              response
                .status(401)
                .json({message: "Unauthorized. Unable to authenticate user"});
            }
          }
        })
      } else {
        response
          .status(401)
          .json({message: "Unauthorized. Unable to authenticate client"});
      }
    }
  });
};


const authenticateGuardianToken = (request, response, next) => {
  const {guardianToken} = request;

  Guardian.findById(guardianToken, (err, res) => {
    if (err) {
      response.status(500).json({message: "an error occurred while handling the request"});
    } else {
      if (res) {
        next();  // authenticated the guardian
      } else {
        response.status(401).json({message: "Unable to authenticate client"});
      }
    }
  });
};

module.exports = {
  generateUserId,
  getUserTokens,
  authenticateUserTokens,
  authenticateGuardianToken,
};
