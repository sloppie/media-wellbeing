import './styles/index.css'
import {useEffect, useState} from 'react';

import './App.css';


const testImages = [
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


function App() {
  const [activeTab, setActiveTab] = useState(0);
  const [activeMedia, setActiveMedia] = useState(testImages[0]);
  const [imageType, setImageType] = useState("neutral")

  useEffect(() => {
    fetchImageStatus(activeMedia, setImageType);
  }, [activeMedia]);

  return (
    <div
      className="App w-screen h-screen flex flex-col lg:flex-row items-stretch justify-stretch"
    >
      {/* Media Preview */}
      <div
        className="h-screen w-screen lg:w-1/2 bg-black flex flex-row items-center justify-center"
      >
        {
          activeMedia && (
            <div className="text-center">
              <div
                className="h-80 w-96 bg-white"
                style={{
                  background: `url(${activeMedia})`,
                  backgroundPosition: "center",
                  backgroundRepeat: "no-repeat",
                }}
              >
              </div>
              <div className="text-white">{`Image Type: ${imageType}`}</div>
            </div>
          )
        }
      </div>
      {/* Control Tab */}
      <div className="h-screen w-screen lg:w-1/2 bg-white overflow-y-scroll">
        {/* Tab Bar */}
        <div className="flex flex-row items-center justify-around pt-4">
          <div
            className={
              "border-b-4 w-26 px-3 py-2 text-center transition-all hover:shadow-lg cursor-pointer" +
              `${activeTab === 0? " bg-black text-white": " border-black bg-white text-black"}`
            }
            onClick={setActiveTab.bind(this, 0)}
          >
            Test Images
          </div>
          {/* <div
            className={
              "border-b-4 w-26 px-3 py-2 text-center transition-all hover:shadow-lg cursor-pointer" +
              `${activeTab === 1? "border-white bg-black text-white": " border-black bg-white text-black"}`
            }
            onClick={setActiveTab.bind(this, 1)}
          >
            Videos
          </div> */}
          {/* <div
            className={
              "border-b-4 w-26 px-3 py-2 text-center transition-all hover:shadow-lg cursor-pointer" +
              `${activeTab === 2? "border-white bg-black text-white": " border-black bg-white text-black"}`
            }
            onClick={setActiveTab.bind(this, 2)}
          >
            GIFs
          </div> */}
        </div>
        {/* Content Bar */}
        <div className="px-2 py-4 flex flex-row flex-wrap overflow-scroll items-center justify-around">
          {
            testImages.map((img) => {
              return (
                <Thumbnail
                  url={img}
                  setActive={setActiveMedia}
                />
              );
            })
          }
        </div>
      </div>
    </div>
  );
}


function Thumbnail({setActive, url}) {
  return (
    <div
      className="text-center w-40 hover:shadow-lg cursor-pointer"
      onClick={setActive.bind(this, url)}
    >
      <div
        className="h-40 w-40"
        style={{
          background: `url(${url})`,
          backgroundPosition: "center",
          backgroundRepeat: "no-repeat",
        }}
      >
      </div>
      <div className="py-2">
        Image
      </div>
    </div>
  );
}


async function fetchImageStatus(url) {
  const response = await fetch('http://localhost:5000/ecd/scan/uibvuiabvlaibv', {
    method: "POST",
  });
}

export default App;
