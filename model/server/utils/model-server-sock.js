/**
 * This class is used as a container to hold the socket instance started once the server is
 * spun up. It is essential to keep this socket variable as that will be the only way to write
 * and receive input from the Model client written in python
 */
class ModelServerSocket {
  /**
   * Takes in the scoket instance that will be used to communicate across the channel to the model.
   *
   * @param {import('net').Socket | null} socket this is the socket instance being run by
   * the net server spun up during startup.
   */
  constructor(socket) {
    this.socket = socket;
  }

  /**
   * Helper method to set the socket instance being used on the Socket endpoint to the model.
   *
   * @returns {import('net').Socket} the socket instance used for communication between the model
   * and the server.
   */
  getSocket() {
    return this.socket;
  }

  /**
   * Helper method to set the socket instance being used on the Socket endpoint to the model.
   *
   * @param {import('net').Socket} socket the socket instance opened for communication with the
   * model.
   */
  setSocket(socket) {
    this.socket = socket;
  }
  
  /**
   * Writes `ModelArgs` to the socket and sends the message to the model.
   *
   * @param {import('./sock-buffer').ModelArgs} modelArgs this is the ProtoBuffer to be sent
   * across the socket to the Model
   */
  writeToSocket(modelArgs) {
    this.socket.write(modelArgs.serializeBinary())
  }
}

module.exports = ModelServerSocket;