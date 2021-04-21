import os
import concurrent.futures as cf
import re

import imageio
from PIL import Image
import urllib
import numpy as np
import pandas as pd
import torch
import torchvision.transforms as transforms

from tqdm import tqdm, trange


# Function extracts the image from a GIF URL that is provided in the parameter
# This is done by extracting the first frame from the image
def extract_img_from_gif(img_url):
  pil_img = Image.open(urllib.request.urlopen(img_url))
  pil_img.seek(0)
  rgb_img = pil_img.convert("RGB")
  pil_img.close()
  
  return np.array(rgb_img)


# Function fetches both GIFs and other image typed from the links declared.
# After fetching, they are pre-processed accordingly with regard to image type.
def extract_img(img_url):
  img_np = None
  not_fetched = False
  try:
    img = imageio.imread(imageio.core.urlopen(img_url).read())
    if (img.shape[2] == 4):
      img_np = extract_img_from_gif(img_url)  # get the fist frame of GIF
    else:
      img_np = img
  except:
    # Image is probably now a dead link or requires authentication to access
    not_fetched = True
  
  if not_fetched:
    return None
  # Make sure that the image is set up in that, the width is always longer than
  # the height.
  # This means rotating the image is need be
  img_tensor = torch.from_numpy(img_np)
  img_tensor = img_tensor.permute(2, 0, 1)
  if img_tensor.shape[1] > img_tensor.shape[2]:
    img_tensor = img_tensor.permute(0, 2, 1)
  
  np_img = transforms.Resize((100, 100))(img_tensor).detach().numpy()

  return np_img



def populate_dataset(dataset_type):
  save_location = f"data/processed-data/{dataset_type}"
  train_csv = pd.read_csv(f"{save_location}/train.csv")
  test_csv = pd.read_csv(f"{save_location}/test.csv")

  training_dataset_imgs = []
  training_dataset_out = []
  evaluation_dataset_imgs = []
  evaluation_dataset_out = []

  for i in tqdm(range(len(train_csv))):
    np_img = extract_img(train_csv.iloc[i]["img_link"])
    eig_val = np.eye(2)[train_csv.iloc[i]["is_explicit"]]
    
    # make sure that the images are actually fetched and no error occurred while fetching images
    if np_img is not None:
      training_dataset_imgs.append(np.array(np_img))
      training_dataset_out.append(eig_val)

  for i in tqdm(range(len(test_csv))):
    np_img = extract_img(test_csv.iloc[i]["img_link"])
    eig_val = np.eye(2)[test_csv.iloc[i]["is_explicit"]]
    
    # make sure that the images are actually fetched and no error occurred while fetching images
    if np_img is not None:
      evaluation_dataset_imgs.append(np_img)
      evaluation_dataset_imgs.append(eig_val)
  
  # create dataset tuples
  training_set = (training_dataset_imgs, training_dataset_out)
  evaluation_set = (evaluation_dataset_imgs, evaluation_dataset_out)

  save_dataset(dataset_type, training_set, evaluation_set)


def save_arr(location, dataset):
  """
  Saves the dataset in the specified location
  location: specific absolute location the data will be saved
  dataset: np.array that is being saved
  """
  with open(location, "wb") as target_file:
    np.save(target_file, dataset)


def save_dataset(dataset_type, train, test):
  save_location = f"data/processed-data/{dataset_type}"
  train_imgs_location = f"{save_location}/train-imgs.npy"
  train_out_location = f"{save_location}/train-out.npy"
  test_imgs_location = f"{save_location}/test-imgs.npy"
  test_out_location = f"{save_location}/test-out.npy"

  
  # training set
  save_arr(train_imgs_location, train[0])  # save the training images
  save_arr(train_out_location, train[1])  # save the training images

  # training set
  save_arr(test_imgs_location, test[0])  # save the training images
  save_arr(test_out_location, test[1])  # save the training images


def save_segment(upper_bound, dataset_type, segment_type, segment_dataset):
  """
  upper_bound (int): This is the last number in the longer list of images that is extracted by this func
  dataset_type: "50-50, 70-30, 80-20, 90-10" based on the current type that is being downloaded
  segment_type: "train | test" based on the dataset type
  sample_imgs: these is a list of numoy arrays of the corresponding images of the segment
  sample_outs: thesea are the corresponding correct outputs of the images
  """
  img_save_location = f"data/processed-data/{dataset_type}/{segment_type}-{upper_bound}-img.npy"
  out_save_location = f"data/processed-data/{dataset_type}/{segment_type}-{upper_bound}-out.npy"

  save_arr(img_save_location, segment_dataset[0])
  save_arr(out_save_location, segment_dataset[1])


def populate_segments(upper_bound, dataset_type, segment_type, segment):
  """
  upper_bound (int): This is the last number in the longer list of images that is extracted by this func
  dataset_type: "50-50, 70-30, 80-20, 90-10" based on the current type that is being downloaded
  segment_type: "train | test" based on the dataset type
  segment: this is the sub segment of the list that is being operated on
  """
  imgs = []
  outs = []

  for i in trange(len(segment), desc=f"Segment from {upper_bound - 499} - {upper_bound}"):
    img_arr = extract_img(segment.iloc[i]["img_link"])

    if img_arr is not None:
      imgs.append(img_arr)
      outs.append(np.eye(2)[segment.iloc[i]["is_explicit"]])
  
  segment_dataset = (np.array(imgs), np.array(outs))
  save_segment(upper_bound, dataset_type, segment_type, segment_dataset)


def download_images(dataset_split_type, data_csv, dataset_type):
  # Download training images
  with cf.ThreadPoolExecutor() as download_executor:
    # train set
    i = 0
    while i + 500 <= len(data_csv):
      i += 500 # create the upperbound

      download_executor.submit(
        populate_segments,  # function
        i - 1,  # upperbound
        dataset_split_type,  # dataset split type
        dataset_type,  # type of the dataset being created
        data_csv.iloc[(i -500): i],  # segmenting to the section being worked on
      )
    
    # download the final segment that may not be in reach by the upper while loop
    if (len(data_csv) % 500) > 0:
      i += 500
      download_executor.submit(
        populate_segments,  # function
        i - 1,  # upperbound
        dataset_split_type,  # dataset split type
        dataset_type,  # type of the dataset being created
        data_csv.iloc[(i -500): len(data_csv)],  # segmenting to the section being worked on
      )


def assemble_dataset(dataset_split_type, train_csv_len, test_csv_len):
  assembly_folder = f"data/processed-data/{dataset_split_type}"
  
  def assemble(dataset_type, data_type, dataset_len):
    """
    This is is a helper function to assemble the data based on
    dataset_type: this is either train or test data
    data_type: this is either img data or out (expected output) data
    dataset_len: this is the lenght of the combined csv that contained the urls
    """
    floored_upper = int(dataset_len / 500) * 500  # get all the expected segment numbers
    upper = 0
    if floored_upper < dataset_len:
      upper = floored_upper + 500
    else:
      upper = floored_upper
    
    combined_dataset = []  # will contain all the np arrays combined
    segment_files = []  # append all the segment files here

    i = 0  # monitoring counter
    while i + 500 <= upper:  # fetch all the files part of the segmenting
      i += 500
      segment_files.append(f"{dataset_type}-{i - 1}-{data_type}.npy")
    
    # assemble
    for segment_file in segment_files:
      with open(f"{assembly_folder}/{segment_file}", "rb") as opened_file:
        np_data = np.load(opened_file)
        combined_dataset.extend(np_data[:])  # get all as an array
    
    with open(f"{assembly_folder}/{dataset_type}-{data_type}.npy") as target_file:
      np.save(target_file, np.array(combined_dataset))
  
  # train dataset
  with cf.ThreadPoolExecutor() as export_executor:
    export_executor.submit(assemble, "train", "img", train_csv_len)
    export_executor.submit(assemble,"train", "out", train_csv_len)

    export_executor.submit(assemble, "test", "img", test_csv_len)
    export_executor.submit(assemble, "test", "out", test_csv_len)


def attempt_recovery(dataset_split_type, dataset_type):
  """ Triggers recovery protocol to prevent image redownload

  Triggers recovery protocol to find the progress achieved by the previous download session.
  Top prevent redownload of already downloaded images. Scans through looking for upperbounds
  that have been acheived.

  Once the iundownloaded segments are found, redownload commences for only those segments

  Args:
    dataset_split_type: specifies which folder to look into based on how the dataset is split
  """
  dataset_folder = f"data/processed-data/{dataset_split_type}"
  dataset_csv = pd.read_csv(f"{dataset_folder}/{dataset_type}.csv")
  dataset_len = len(dataset_csv)
  # scan for remaining segments to download
  folder_children = os.scandir(dataset_folder)
  relevant_files = []  # will store the files that are of that category

  for child in folder_children:
    if re.search(f"{dataset_type}-\d+-img\.npy", child.name):
      relevant_files.append(child.name)
  
  floored_upper = int(dataset_len / 500) * 500  # get all the expected segment numbers
  upper = 0
  if floored_upper < dataset_len:
    upper = floored_upper + 500
  else:
    upper = floored_upper
  
  remaining_bounds = []  # stash the remaining bound after searching through

  i = 0  # monitor bounds
  while i + 500 <= upper:
    i += 500
    segment_file = f"{dataset_type}-{i - 1}-img.npy"
    try:
      relevant_files.index(segment_file)
    except:
      remaining_bounds.append(i - 1)
  
  with cf.ThreadPoolExecutor() as recovery_executor:
    for bound in remaining_bounds:
      if bound + 1 <= dataset_len:
        recovery_executor.submit(
          populate_segments,
          bound,
          dataset_split_type,
          dataset_type,
          dataset_csv.iloc[bound - 499: (bound + 1)],  # segment data by boundng hundreds
        )
      else:
        recovery_executor.submit(
          populate_segments,
          bound,
          dataset_split_type,
          dataset_type,
          dataset_csv.iloc[bound - 499: dataset_len],  # segment data by boundng hundreds
        )


def is_salvagable(dataset_split_type, dataset_type):
  """ Checks for salvagability of folder before forcing fresh download

  This includes looking for already downloaded segment files in the respetive folder
  This looks for folders with the segment file structure e.g.
    For training data, the segment would be `train-99-img.npy`
  
  Args:
    dataset_split_type: "50-50" | "70-30" | "80-20" | "909-10"
    dataset_type: whether it is `"train"` or `"test"` data
  
  Returns:
    bool: representing whether or not a segmented dataset was found
  """
  downloaded_segment_found = False
  dataset_folder = f"data/processed-data/{dataset_split_type}"

  # scan for remaining any previously downloaded segments
  folder_children = os.scandir(dataset_folder)

  # looking for the first file that fits the criteria. if any file is found that fits
  # the below regular expression, we break out of the loop and attempt recovery
  for child in folder_children:
    if re.search(f"{dataset_type}-\d+-img\.npy", child.name):
      downloaded_segment_found = True
      break
  
  return downloaded_segment_found



if __name__ == "__main__":
  datasets = ["50-50"]

  for dataset_split_type in datasets:
    train_csv = pd.read_csv(f"data/processed-data/{dataset_split_type}/train.csv")
    test_csv = pd.read_csv(f"data/processed-data/{dataset_split_type}/test.csv")

    # check for salvagability before committing to a fresh download for both train and test
    if is_salvagable(dataset_split_type, "train"):
      print(f"Attempting Recovery for training data in {dataset_split_type}...")
      attempt_recovery(dataset_split_type, "train")
    else:
      download_images(dataset_split_type, train_csv, "train")

    print(f"Train Dataset with split: {dataset_split_type} download complete")

    if is_salvagable(dataset_split_type, "test"):
      print(f"Attempting Recovery for training data in {dataset_split_type}...")
      attempt_recovery(dataset_split_type, "test")
    else:
      download_images(dataset_split_type, test_csv, "test")

    print(f"Test Dataset with split: {dataset_split_type} download complete")

    print("Assembling np arrays together...")
    assemble_dataset(dataset_split_type, len(train_csv), len(test_csv))
