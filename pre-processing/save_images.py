import torch
import torchvision.transforms as transforms
import numpy as np
import pandas as pd
import imageio
from PIL import Image
import urllib
from tqdm import tqdm


# Function extracts the image from a GIF URL that is provided in the parameter
# This is done by extracting the first frame from the image
def extract_img_from_gif(img_url):
  pil_img = Image.open(urllib.request.urlopen(img_url))
  pil_img.seek(0)
  rgb_img = pil_img.convert("RGB")
  
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


def save_dataset(dataset_type, train, test):
  save_location = f"data/processed-data/{dataset_type}"
  train_imgs_location = f"{save_location}/train-imgs.npy"
  train_out_location = f"{save_location}/train-out.npy"
  test_imgs_location = f"{save_location}/test-imgs.npy"
  test_out_location = f"{save_location}/test-out.npy"

  def save_arr(location, dataset):
    with open(location, "wb") as target_file:
      np.save(target_file, dataset)
  
  # training set
  save_arr(train_imgs_location, train[0])  # save the training images
  save_arr(train_out_location, train[1])  # save the training images

  # training set
  save_arr(test_imgs_location, test[0])  # save the training images
  save_arr(test_out_location, test[1])  # save the training images


if __name__ == "__main__":
  datasets = ["50-50"]

  for dataset_type in datasets:
    populate_dataset(dataset_type)
