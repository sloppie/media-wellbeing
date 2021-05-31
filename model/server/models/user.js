const mongoose = require('mongoose');

const UserSchema = new mongoose.Schema({
  _id: {
    type: String,
    required: true,
  },
  guardian: {
    type: String,
    required: true,
  },
});

/**@class User */
const User = mongoose.model("user", UserSchema, "user");

module.exports = User;
