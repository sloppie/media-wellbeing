const express = require('express')
const path = require('path')

const app = express()


app.get("/", (request, response) => {
  response.sendFile(path.join(__dirname, "..", "saved.npy"))
});


app.get("/:dataset_type/:type", (request, response) => {
  const filename = (/\.npy/.test(request.params.type))?
      request.params.type: `${request.params.type}.npy`;
  const fileLocation = path.join(
      __dirname, "..", "data", "processed-data",
      request.params.dataset_type, filename);
  
  response.status(200).sendFile(fileLocation);
});


app.listen(3000, () => console.log("listening at 3000"));
