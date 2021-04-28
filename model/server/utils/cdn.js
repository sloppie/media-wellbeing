const multer = require('multer');
const path = require('path');


/**
 * 
 * @param {string} fileName this is the generated filename based on user id and the timestamp when
 * the request is being handled.
 * @param {(contentType) => void} setContentType this callback helps the `ModelArgs` pace content type
 * based on what multer detects from the request body.
 */
function getStorage(fileLocation, fileName, setContentType) {
  return multer.diskStorage({
    destination: (req, file, cb) => {
      cb(null, fileLocation);
    },

    filename: (request, file, cb) => {
      let storedName = fileName;
      console.log(`Storing file name: ${fileName}`)
      if (file.mimetype) {
        setContentType(file.mimetype); // set the content type to allow full population of the ProtoBuff
        storedName += `.${file.mimetype.split("/")[1]}`;
      }

      cb(null, storedName);
    }
  });
}

module.exports = {
  getStorage,
};
