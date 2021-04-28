import os
# iage processing
import cv2
import imageio
from PIL import Image
from imageio.core.functions import imread
# model pre-processing
import torch
import torchvision.transforms as transforms
import numpy as np
from tqdm import tqdm
import re


def preprocess_img(img_mat, to_tensor=False):
  img_tensor = torch.from_numpy(img_mat.copy())
  img_tensor = img_tensor.permute(2, 0, 1)
  if img_tensor.shape[1] > img_tensor.shape[2]:
    img_tensor = img_tensor.permute(0, 2, 1)
  
  resized_img = transforms.Resize((100, 100))(img_tensor)
  if to_tensor:
    return resized_img
  else:
    return resized_img.detach().numpy()


def to_PIL(img_mat):
  test_np = np.array([1])
  tensor_img = None
  if type(img_mat) == type(test_np):
    tensor_img = torch.from_numpy(img_mat)
  else:
    tensor_img = img_mat
  
  return tensor_img.permute(2, 0, 1)  # color channel comes first


def clean_up(img_location):
  try:
    os.remove(img_location)
  except:
    print(f"Unable to remove {img_location}")


def extract_gif(img_location):
  pil_img = Image.open(img_location)
  img_arr = []

  i = 0  # seek cursor
  while True:
    try:
      pil_img.seek(i)
      img_arr.append(preprocess_img(np.array(pil_img.convert("RGB"))))
    except:
      # means that the end of file has been reached.
      break 

    i += 1  # move cursor
  
  return np.array(img_arr)


def extract_img(img_location, toTensor=False):
  img_np = None
  not_fetched = False
  try:
    img_np = imageio.imread(img_location)
  except Exception as exc:
    # Image is probably now a dead link or requires authentication to access
    print(exc)
    # print(exc)
    not_fetched = True
  
  if not_fetched:
    return None
  # Make sure that the image is set up in that, the width is always longer than
  # the height.
  # This means rotating the image is need be
  img_tensor = torch.from_numpy(img_np.copy())
  img_tensor = img_tensor.permute(2, 0, 1)
  PIL_tensor = None
  
  if img_tensor.shape[1] > img_tensor.shape[2]:
    img_tensor = img_tensor.permute(0, 2, 1)
  
  np_img = transforms.Resize((100, 100))(img_tensor).detach().numpy()

  # break out instead of continuing
  if toTensor:
    PIL_tensor = torch.from_numpy(np_img)
    return PIL_tensor
  else:
    return np_img


def extract_from_vid(vid_location):
  frames = []  # store all the frames
  vid_cap = cv2.VideoCapture(vid_location)
  is_frame = True

  with tqdm(total=vid_cap.get(cv2.CAP_PROP_FRAME_COUNT), desc="Extracting Vdeo Frames") as extraction_progress:
    while is_frame:
      # get video frames
      is_frame, frame = vid_cap.read()
      
      if is_frame:
        # pre-process the tensor before appending the individual frame
        frame = preprocess_img(cv2.cvtColor(frame, cv2.COLOR_BGR2RGB))  # convert to rgb color space
        frames.append(frame)
        extraction_progress.update(1)

  return np.array(frames)


def parse_gif(output_matrix, file_name):
  PIL_image = Image.open(f"image-buffer/{file_name}")
  new_frames = []
  i = 0
  no_of_frames = 0
  duration = 0
  while True:
    try:
      PIL_image.seek(i)
      no_of_frames += 1
      frame = np.array(PIL_image.convert("RGB"))
      duration += PIL_image.info["duration"]  # gets the duration of the current frame
      if output_matrix[i] == 1:
        new_frames.append(np.zeros(frame.shape, dtype=np.int16))
      else:
        new_frames.append(frame)
    except:
      break  #EOF reached
  
  gif_fps = frame / duration * 1000  # calculate FPS
  
  imageio.mimsave(f"out-buffer/{file_name}", new_frames, format="GIF", fps=gif_fps)

  return f"out-buffer/{file_name}"  # return the file location of the saved image


def parse_vid(output_matrix, file_name):
  vid_cap = cv2.VideoCapture(f"image-buffer/{file_name}")
  fps = vid_cap.get(cv2.CAP_PROP_FPS)
  width = int(vid_cap.get(cv2.CAP_PROP_FRAME_WIDTH))
  height = int(vid_cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
  is_frame = True
  frame_count = 0
  new_frames = []

  with tqdm(total=vid_cap.get(cv2.CAP_PROP_FRAME_COUNT), desc="Iterating through Frames") as extraction_progress:
    while is_frame:
      # get video frames
      is_frame, frame = vid_cap.read()
      
      if is_frame:
        # pre-process the tensor before appending the individual frame
        # frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)  # convert to rgb color space
        if output_matrix[frame_count] == 1:
          new_frames.append(np.zeros(frame.shape))
        else:
          new_frames.append(frame)
        extraction_progress.update(1)
        frame_count += 1

  print(f"FPS: {fps} of screen resol: {height == new_frames[-1].shape[0]} x {width == new_frames[-1].shape[1]}")

  out_codec = cv2.VideoWriter_fourcc(*"mp4v")
  output = cv2.VideoWriter(
    f"out-buffer/{file_name}", out_codec, fps, (height, width))

  print(f"Number of frames: {len(new_frames)}")
  
  for frame in new_frames:
    output.write(frame.astype("uint8"))
  
  vid_cap.release()
  output.release()


def parse_img(output_matrix, file_name):
  img_arr = None
  if re.search("\.png", file_name):
    img = Image.open(f"image-buffer/{file_name}")
    img.seek(0)
    img_arr = np.array(img.convert("RGB"))
  else:
    img_arr = imageio.imread(f"image-buffer/{file_name}")

  # convert to BGR from RBG
  img_arr = cv2.cvtColor(img_arr, cv2.COLOR_RGB2BGR)
  if output_matrix[0] == 1:
    cv2.imwrite(f"out-buffer/{file_name}", np.zeros(img_arr.shape))
  else:
    cv2.imwrite(f"out-buffer/{file_name}", img_arr)


def parse_media(file_name):
  file_id, _ = file_name.split(".")
  output_matrix = None

  with open(f"out-buffer/{file_id}.npy", "rb") as file_npy:
    output_matrix = np.load(file_npy)
    print(f"output_matrix shape: {output_matrix.shape}")
  
  if re.search("\.jpg|\.png", file_name):
    print("processing image...")
    parse_img(output_matrix, file_name)
  elif re.search("\.gif", file_name):
    print("parsing gif...")
    parse_gif(output_matrix, file_name)
  elif re.search("\.mp4", file_name):
    print("parsing video...")
    parse_vid(output_matrix, file_name)


if __name__ == "__main__":
  # vid_arr = extract_from_vid("C:/Users/ADMIN/Downloads/Video/Soulmate - YouTube.mp4")
  parse_media(f"test-img.jpg")
  parse_media(f"test-vid.mp4")
  # print(torch.from_numpy(vid_arr).shape[0])
