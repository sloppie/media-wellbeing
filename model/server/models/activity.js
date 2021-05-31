const mongoose = require('mongoose');

const ActivitySchema = new mongoose.Schema({
  _id: {
    type: String,
    required: true,
  },
  file_id: {
    type: String,
    required: true,
  },
  file_name: {
    type: String,
    required: true,
  },
  user: {
    type: String,
    required: true,
  },
  guardian: {
    type: String,
    required: true,
  },
  date: Date,
  status: {
    type: String,
    required: true,
  },
  label: {
    type: Number,
    required: true,
  },
});

const Activity = mongoose.model("activity", ActivitySchema, "activity");

module.exports = Activity;
