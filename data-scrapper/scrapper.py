from selenium.webdriver import Firefox, FirefoxOptions
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.keys import Keys
import time

PATH = "C:\\Program Files (x86)\\web-drivers\\firefox\\geckodriver.exe"
firefox_options = FirefoxOptions()
# firefox_options.add_argument("--private")  # turn on incognito to prevent interfering with the User's search history

driver = Firefox(executable_path=PATH, options=firefox_options)

driver.get("https://duckduckgo.com")
# get the search form of the duckduckgo search engine
search_form = WebDriverWait(driver, timeout=5).until(lambda d: driver.find_element_by_id("search_form_input_homepage"))
search_form.click()
search_form.send_keys("Test Search")  # enter search
search_form.send_keys(Keys.RETURN)
filter_menu = WebDriverWait(driver, timeout=10).until(
    lambda d: driver.find_element_by_xpath("/html/body/div[2]/div[5]/div[3]/div/div[1]/div[1]/div/div[2]/a"))
filter_menu.click()
off_option = WebDriverWait(driver, timeout=10).until(
    lambda d: driver.find_element_by_xpath("/html/body/div[6]/div[2]/div/div/ol/li[3]/a")
)
off_option.click()
res = WebDriverWait(driver, timeout=10).until(
    lambda d: driver.find_element_by_xpath(
        "/html/body/div[2]/div[5]/div[3]/div/div[1]/div[1]/div/div[2]/a").text == "Safe Search: Off"
)
print("Response:")
print(res)
time.sleep(2)
image_tab = WebDriverWait(driver, timeout=10).until(
    lambda d: driver.find_element_by_xpath("/html/body/div[2]/div[2]/div[1]/div[2]/div[1]/div/ul[1]/li[2]/a")
)
while not res:
    print("res not set")
    pass

image_tab.click()

# find all the drop-down tags that affect the type of images we receive
metabar_dropdowns = WebDriverWait(driver, timeout=10).until(
    lambda d: driver.find_element_by_class_name("metarbar_dropdowns")
)

# since the search filter is already turned off, toggle the following filters:
#   - size: change to "Medium" -> dropdown--size
#   - type: Photograph (to avoid accidental GIFs along the automated search) -> dropdown--type

# size metabar
size_dropdown = WebDriverWait(metabar_dropdowns, timeout=10).until(
    lambda d: metabar_dropdowns.find_element_by_class_name("dropdown--size")
)

# type metabar
type_dropdown = WebDriverWait(metabar_dropdowns, timeout=10).until(
    lambda d: metabar_dropdowns.find_element_by_class_name("dropdown--type")
)
