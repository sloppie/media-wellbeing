from torch.utils.data.dataset import Dataset


class FramesDataset(Dataset):
  """
  This is the class that is used to encapsulate the data that will be used to
  train the model.
  """
  def __init__(self, frames):
    self.data = frames
  
  def __getitem__(self, idx):
    return self.data[idx].float()
  
  def __len__(self):
    return self.data.shape[0]


class ImageDataset(Dataset):
  """
  This is the class that is used to encapsulate the data that will be used to
  train the model.
  """
  def __init__(self, frames):
    self.data = frames
  
  def __getitem__(self, idx):
    return self.data.float()
  
  def __len__(self):
    return 1
