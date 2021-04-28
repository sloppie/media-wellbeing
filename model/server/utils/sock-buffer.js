/**
 * @typedef {{
 * setMediaType: (type: 0 | 1 | 2) => void,
 * getMediaType: () => 0 | 1 | 2,
 * setFileName: (fileName: string) => void,
 * getFileName: () => string,
 * setRequestType: (requestType: 0 | 1) => void,
 * getRequestType: () => 0 | 1,
 * deserializeBinary: () => void,
 * serializeBinary: () => Buffer,
 * }} ModelArgs
 * 
 * @typedef {{
 * setIsExplicit: () => void,
 * getIsExplicit: () => 0 | 1,
 * getRequestType: () => 0 | 1,
 * deserializeBinary: () => void,
 * setFileName: (filename: string) => void,
 * getFileName: () => string,
 * }} ModelResponse
 */

 /**
  * This class holds a buffer of all the pending model responses that are currently being
  * handled by the `model.py` model.
  * 
  * Each buffer entry holds the `FileArgs` and the callback to be executed on resolve of
  * the `FileArgs` sent to the model. After the mode resolves, it sends the `ModelResponse`
  * through the open communication socket. Once the `ModelServerSocket` receives the
  * ModelResonse, it sends it to the `SockBuffer` instance which looks for the `ModelArgs`
  * with the coresponding arguments executes the associated callback.
  * 
  * @async callbacks thus detaches from the main thread.
  */
class SockBuffer {
  /**
   * 
   * @param {import('./model-server-sock')} modelServerSocket this is the class container
   * wrapping the socket instance used to communicate to the model.
   */
  constructor(modelServerSocket) {
    /**
     * houses all active pending request to `model.py`
     * @type {Array<{
     * modelRequest: ModelArgs,
     * onResponse: (modelResponse: ModelResponse) => void,
     * }>}
     */
    this.socketBuffer = [];
    /**
     * Houses all the pending requests to parse files of explicit content.
     * @type {Array<{
     * parseRequest: ModelArgs,
     * onResponse: () => void,
     * }>}
     */
    this.parseRequestSocketBuffer = [];
    this.modelServerSocket = modelServerSocket;
  }

  /**
   * This method sends the `ModelArgs` to the server. Afterwards, it adds those `ModelArgs` sent to
   * the server and the corresponding callbackthat is to be executed upon successful
   * resolution by the `model.py` running instance.
   *
   * @param {ModelArgs} request this is the `ModelArgs` protocolbuffer that was sent to the Model
   * to resolve
   * @param {(modelResponse: ModelResponse) => void} onResponse this is the callback to b executed
   * once the Model sends back a `ModelResponse` to the user.
   */
  addRequest(request, onResponse) {
    this.socketBuffer.push({modelRequest: request, onResponse});
    this.modelServerSocket.writeToSocket(request);
  }

  /**
   * This method sends the `ModelArgs` to the server. Afterwards, it adds those `ModelArgs` sent to
   * the server and the corresponding callbackthat is to be executed upon successful
   * resolution by the `model.py` running instance.
   *
   * @param {ModelArgs} parseRequest this is the `ModelArgs` protocolbuffer that was sent to the
   * Model to resolve
   * @param {() => void} onResponse this is the callback to b executed once the Model sends back
   * a `ModelResponse` to the user.
   */
  addParseRequest(parseRequest, onResponse) {
    this.parseRequestSocketBuffer.push({parseRequest, onResponse});
    this.modelServerSocket.writeToSocket(request);
  }

  /**
   * This methods sends back a response from the socket instance running. This happens after successful
   * resolution of the `ModelArgs` sent to the server.
   * Once called, the model goes through the `socketBuffer` and gets the index of the associate model
   * args that led to that response, executes the callback and splices off that instance since it is
   * no longer of use in the socketBuffer.
   * @param {ModelResponse} modelResponse 
   */
  resolveRequest(modelResponse) {
    let requestIndex = -1; // set at negative one to help know if the request was not found
    // search linearly through the socketBuffer, once found, execute and then splice out.
    for (let i in this.socketBuffer) {
      if (modelResponse.getFileName() == this.socketBuffer[i]["modelRequest"].getFileName()) {
        requestIndex = 1;
        // return the output of model
        this.socketBuffer[i].onResponse(modelResponse);
        break; // search complete
      }
    }

    // clean up buffer if the request was found.
    if (requestIndex > -1) {
      this.socketBuffer.splice(requestIndex, 1);
    }
  }

  resolveParseRequest(modelResponse) {
    let requestIndex = -1; // set at negative one to help know if the request was not found
    // search linearly through the socketBuffer, once found, execute and then splice out.
    for (let i in this.parseRequestSocketBuffer) {
      const requestFileName = modelResponse.getFileName();
      const currentRequestFileName = this.parseRequestSocketBuffer[i]["modelRequest"].getFileName();
      if (requestFileName == currentRequestFileName) {
        requestIndex = 1;
        // return the procesed media file
        this.parseRequestSocketBuffer[i].onResponse();
        break; // search complete
      }
    }

    // clean up buffer if the request was found.
    if (requestIndex > -1) {
      this.parseRequestSocketBuffer.splice(requestIndex, 1);
    }
  }
}

module.exports = SockBuffer;
