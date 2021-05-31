const mongoose = require('mongoose');

/**
 * @class Guardian
 */
const GuardianSchema = new mongoose.Schema({
  _id: {
    type: String,
    required: true,
  },
  email: {
    type: String,
    required: true,
  },
  password: {
    type: String,
    required: true,
  },
  name: {
    type: String,
    required: true,
  },
});

const Guardian = mongoose.model("guardian", GuardianSchema, "guardian");

module.exports = Guardian;
