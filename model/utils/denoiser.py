import os
from os import path
import glob
import re

data_path = path.abspath("../nsfw_data_source_urls-master/raw_data")

all_files = glob.glob(f"{data_path}\\*\\*\\*.txt")

# print(len(all_files))

viable_urls = ""

# This loop goes through all the files looking for duplicates and only appending
# .jpg | .jpeg | .png files to the list of viable urls
for i, file in enumerate(all_files):
  try:
    with open(file, "r") as f:
      for line in f.readlines():
        try:
          if re.search(".jpg|.jpeg|.png", line):
            viable_urls += line
        except:
          pass
    f.close()
  except:
    pass
  # if i == 10:
  #   break

all_urls = open("pic-urls.txt", "w")
all_urls.write(viable_urls)
all_urls.close()
