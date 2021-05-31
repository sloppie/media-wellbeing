const express = require('express');

const app = express.Router();

// routing middleware
const activity = require('./activity');
const user = require('./user');
const guardian = require('./guardian');
const cdn = require('./cdn');

app.use("/activity", activity);
app.use("/user", user);
app.use("/guardian", guardian);
app.use("/cdn", cdn);

module.exports = app;
