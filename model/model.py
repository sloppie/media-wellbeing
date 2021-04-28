import socket
import re
import torch
from torch.utils import data
from torch.utils.data.dataset import Dataset
import numpy as np
from tqdm import tqdm

# proto-buffers
import modelio_pb2

import utils as utl

# dataloader
from torch.utils.data.dataloader import DataLoader

# datasets
from datasets import FramesDataset, ImageDataset


resnet18 = torch.load("resnet/resnet.pt")


def get_filename(media_location):
  directories = media_location.split("/")

  return directories[-1]  # return the last name as the filename


def export_file_outputs(media_Location, output_arr):
  file_id, _ = get_filename(media_Location).split(".")

  with open(f"out-buffer/{file_id}.npy", "wb") as out_buffer:
    np.save(out_buffer, output_arr)


def scan_gif(gif_location):
  gif_dataset = Dataset(torch.from_numpy(utl.extract_gif(gif_location)))
  gif_dataloader = DataLoader(
    dataset=gif_dataset,
    batch_size=gif_dataset.shape[0],
    shuffle=False,
  )
  frame_predictions = []
  is_explicit = 0
  for image in gif_dataloader:
    out = resnet18(image.view(gif_dataset.shape[0], 3, 100, 100))
    for frame in out:
      prediction = torch.argmax(frame).item()
      frame_predictions.append(prediction)
      if prediction == 1:
        is_explicit = 1
        break

  # save mode output in case the user wants the image to be parsed
  export_file_outputs(gif_location, np.array(frame_predictions))
  
  return is_explicit


def scan_vid(vid_location):
  vid_dataset = FramesDataset(torch.from_numpy(utl.extract_from_vid(vid_location)))
  vid_dataloader = DataLoader(
    dataset=vid_dataset,
    batch_size=30,
    shuffle=False,
  )
  is_explicit = 0
  frame_predictions = []
  # scan all available frames
  for image in tqdm(vid_dataloader, desc="Scannimg image frames"):
    out = resnet18(image.view(-1, 3, 100, 100)) # reshape to four dimesnions
    predictions = [torch.argmax(i).item() for i in out]
    frame_predictions.extend(predictions)
    # if no explicit content has been detected yet, scrap for whether the CNN has found explicit content
    # now
    if is_explicit == 0:
      try:
        frame_predictions.index(1)
        is_explicit = 1
      except:
        pass
  
  # save the model outputs in case the user might want the image parsed:
  export_file_outputs(vid_location, np.array(frame_predictions))

  return is_explicit


def scan_img(img_location):
  img_dataset = None
  # check for four-channel png
  if re.search("\.png", img_location):
    img_dataset = ImageDataset(torch.from_numpy(utl.extract_gif(img_location)[0]))
  else:
    img_dataset = ImageDataset(torch.from_numpy(utl.extract_img(img_location)))

  gif_dataloader = DataLoader(
    dataset=img_dataset,
    batch_size=1,
    shuffle=False,
  )
  is_explicit = 0
  for image in gif_dataloader:
    print(image.shape)
    out = resnet18(image)
    for frame in out:
      prediction = torch.argmax(frame).item()
      if prediction == 1:
        is_explicit = 1
        break
  
  export_file_outputs(img_location, np.array([is_explicit]))
  return is_explicit


def scan_image(img_type, media_location):
  if img_type == 1:
    return scan_img(media_location)
  elif img_type == 2:
    return scan_gif(media_location)
  elif img_type == 3:
    return scan_vid(media_location)


# game loop to keep the server running
def start_socket_connection():
  model_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

  # model_socket.bind((socket.gethostname(), 9000))

  model_socket.connect(("127.0.0.1", 9000))

  while True:
    data = model_socket.recv(1024)  # receive data from socket
    while not data:
      break

    model_args = modelio_pb2.ModelArgs()
    model_args.ParseFromString(data)
    # print(model_args.file_name)
    model_response = modelio_pb2.ModelResponse()
    model_response.file_name = model_args.file_name
    model_response.request_type = model_args.request_type

    if model_args.request_type == 1:
      print("Request to scan explicit content in file")
      model_response.is_explicit = scan_image(
        model_args.media_type,
        f"image-buffer/{model_args.file_name}"
      )
      print("sending output of model...")
    else:
      print("Parsing file to remove explicit content")
      model_response.is_explicit = 0  # output is irrelevant
      # wait for parsing to finish
      utl.parse_media(model_args.file_name)

    print("processing...")
    # get output from the program
    model_socket.send(model_response.SerializeToString())


if __name__ == "__main__":
  print(scan_image(1, f"image-buffer/test-img.jpg"))
  print(scan_image(3, f"image-buffer/test-vid.mp4"))
  # start_socket_connection()