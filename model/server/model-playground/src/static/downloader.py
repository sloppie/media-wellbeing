import imageio
import re
from tqdm import tqdm

test_images = [
  "https://l450v.alamy.com/450v/apykk4/rose-hips-rosa-rugosa-seedheads-apykk4.jpg",
  "http://www.wildfowl-photography.co.uk/wildfowl/swan-goose/swan-goose.jpg",
  "http://i1.cdn2b.image.pornhub.phncdn.com/m=eqgl9daaaa/videos/201311/07/19429901/original/2.jpg",
  "https://pics.davesgarden.com/pics/2007/11/23/gloria125/212fd8.jpg",
  "https://www.main.nc.us/naturenotebook/fungi/pix/coralfungus.JPG",
  "https://media.sciencephoto.com/image/b2501006/400wm/B2501006-Fly_agaric_mushrooms.jpg",
  "https://di.phncdn.com/videos/202011/09/368507722/original/(m=qXKQ66VbeafTGgaaaa)(mh=kq4ZGWAkGn9n1WKj)0.jpg",
  "http://cdn-d-img.pornhub.com/videos/201508/12/54947991/original/(m=eWdT8daaaa)12.jpg",
  "http://th3.dirtypornvids.com/th/M2n/48034098.jpg",
  "https://img-l3.xnxx-cdn.com/videos/thumbslll/dc/5b/e6/dc5be68abc6d1a8a00135e36149d18d5/dc5be68abc6d1a8a00135e36149d18d5.4.jpg",
  "https://thumb-v-cl2.xhcdn.com/a/yCFxkGn4fXaAtCXMSS3BKA/006/950/726/2000x2000.4.jpg",
  "https://ask-angels-swellpress.netdna-ssl.com/wp-content/uploads/2015/02/earthstarchakra-300x263.jpg",
  "https://l450v.alamy.com/450v/egdjar/hen-of-the-woods-hen-of-the-woods-rams-head-sheeps-head-grifola-frondosa-egdjar.jpg",
  "https://americanmushrooms.com/images/Boletus_bicolor_02.jpg",
  "https://i1.wp.com/raw.githubusercontent.com/dmlc/web-data/master/mxnet/Blog_mxnet_R/Blog_RealWorld_Switzerland.png?w=456&ssl=1",
  "https://i.ebayimg.com/images/g/aLwAAOSwuoZfdg5z/s-l300.png",
  "http://tn1.suitemovies.com/thumbs/320/846/8990846.jpg",
];


def download_image(img_url, idx):
  try:
    img = imageio.imread(img_url)
    if re.search("\.png", img_url):
      imageio.imsave(f"{idx}.png", img)
    else:
      imageio.imsave(f"{idx}.jpg", img)
  except:
    pass

if __name__ == "__main__":
  for i, image in tqdm(enumerate(test_images), desc="Downloading test images"):
    download_image(image, i)