import socket

import modelio_pb2 as modelio

model_args = modelio.ModelArgs()

model_args.media_type = 0
model_args.file_name = "my file.jpg"

print(model_args.SerializeToString())

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
  s.connect((socket.gethostname(), 9000))
  s.sendall(model_args.SerializeToString())
  data = s.recv(1024)

  while not data:
    break

  model_response = modelio.ModelResponse()
  model_response.ParseFromString(data)

  print(f"Received Model Output: {model_response.is_explicit}")
