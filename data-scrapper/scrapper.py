from selenium.webdriver import Firefox, FirefoxOptions, ChromeOptions, Chrome
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.keys import Keys
import selenium.webdriver.support.expected_conditions as Conditions
from selenium.webdriver.common.by import By
import time
import re

PATH = "C:\\Program Files (x86)\\web-drivers\\chrome\\chromedriver.exe"
firefox_options = FirefoxOptions()
chrome_options = ChromeOptions()
chrome_options.add_argument("--incognito")
# firefox_options.add_argument("--private")  # turn on incognito to prevent interfering with the User's search history

driver = Chrome(executable_path=PATH, options=chrome_options)
# driver = Firefox(executable_path=PATH, options=firefox_options)


def search_for_item(web_driver, value):
    try:
        web_driver.get("https://duckduckgo.com")
        # get the search form of the duckduckgo search engine
        search_form = WebDriverWait(web_driver, timeout=5).until(
            lambda d: web_driver.find_element_by_id("search_form_input_homepage"))
        search_form.click()
        search_form.send_keys(value)  # enter search
        search_form.send_keys(Keys.RETURN)
        return True
    except Exception as ex:
        print(ex)
        return False


def turn_off_search_moderation(web_driver):
    try:
        filter_menu = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_element_by_xpath(
                "/html/body/div[2]/div[5]/div[3]/div/div[1]/div[1]/div/div[2]/a"))
        filter_menu.click()
        off_option = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_element_by_xpath("/html/body/div[6]/div[2]/div/div/ol/li[3]/a")
        )
        off_option.click()
        res = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_element_by_xpath(
                "/html/body/div[2]/div[5]/div[3]/div/div[1]/div[1]/div/div[2]/a").text == "Safe Search: Off"
        )
        if res:
            print("Safe search Moderation off")
        while not res:
            print("Retrying safe search turn off")
        return True
    except Exception as ex:
        print(ex)
        return False


def select_image_tab(web_driver):
    try:
        duckbar = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_element_by_id("duckbar_static")
        )
        items = WebDriverWait(duckbar, timeout=10).until(
            lambda d: duckbar.find_elements_by_class_name("zcm__link")
        )
        for list_item in items:
            if list_item.text == "Images":
                list_item.click()
                break
        return True
    except Exception as ex:
        print(ex)
        return False


def set_image_size(web_driver):
    try:
        size_dropdown = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_element_by_class_name("dropdown--size")
        )
        size_dropdown.click()  # open modal dropdown
        time.sleep(2)
        modal_dropdown = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_element_by_class_name("modal__list")
        )
        anchor_elements = WebDriverWait(modal_dropdown, timeout=10).until(
            lambda d: modal_dropdown.find_elements_by_tag_name("a")
        )
        for i, anchor_element in enumerate(anchor_elements):
            if anchor_element.text == "Medium":
                print("Size of photos set to medium")
                anchor_element.click()
                break
        return True
    except Exception as ex:
        print(ex)
        return False


def set_image_type(web_driver):
    try:
        type_dropdown = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_element_by_class_name("dropdown--type")
        )
        type_dropdown.click()  # open the modal
        time.sleep(2)
        modal_container = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_element_by_class_name("modal__list")
        )
        anchor_elements = web_driver.find_elements_by_class_name("js-dropdown-items")

        for anchor_element in anchor_elements:
            print(f"Media Type: {anchor_element.text}")
            if anchor_element.text == "Photograph":
                print("Type of image set to: Photograph")
                anchor_element.click()
                break
        return True
    except Exception as ex:
        print(ex)
        return False  # signal that it was unable to select the image type


# find all the drop-down tags that affect the type of images we receive


# since the search filter is already turned off, toggle the following filters:
#   - size: change to "Medium" -> dropdown--size
#   - type: Photograph (to avoid accidental GIFs along the automated search) -> dropdown--type


def set_moderation_off(web_driver):
    try:
        filter_dropdown = web_driver.find_element_by_class_name("dropdown--safe-search")
        filter_dropdown.click()
        modal_list = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_element_by_class_name("modal__list")
        )
        anchor_elements = modal_list.find_elements_by_tag_name("a")
        for anchor_element in anchor_elements:
            if re.search("Off", anchor_element.text):
                anchor_element.click()
                print("SafeSearch set to: off")
                break
        return True
    except Exception as ex:
        print(ex)
        return False


def select_first_image(web_driver):
    try:
        image_tiles = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_elements_by_class_name("tile--img")
        )
        image_tiles[0].click()  # clicks on the first image
        return True
    except Exception as ex:
        print(ex)
        return False  # unable to select the image


def get_selected_image_link(web_driver):
    try:
        selected_image_desc = WebDriverWait(web_driver, timeout=10).until(
            lambda d: web_driver.find_element_by_class_name("c-detail__desc"))
        anchor_tag = selected_image_desc.find_element_by_class_name("js-image-detail-link")
        image_href = anchor_tag.get_attribute("href")
        return image_href
    except Exception as ex:
        print(ex)
        return ""  # shows that the image was not found


def move_to_next_image(web_driver):
    try:
        next_image = web_driver.find_element_by_class_name("js-detail-next")
        next_image.click()
        print("next image ->")
        return True
    except Exception as ex:
        print(ex)
        return False


if __name__ == "__main__":
    print("Running __main__")
    search_for_item(driver, "Test Search")
    time.sleep(3)
    duckbar_static = WebDriverWait(driver, timeout=10).until(
        lambda d: driver.find_element_by_id("duckbar_static")
    )
    metabar = None
    try:
        metabar = WebDriverWait(driver, timeout=10).until(
            lambda d: driver.find_element_by_class_name("metabar__dropdowns")
        )
    except Exception as e:
        print(e)
        pass
    # turn_off_search_moderation(driver)
    # tries to run the whole thing like a graph
    action_graph = [
        select_image_tab,
        set_moderation_off,
        set_image_size,
        set_image_type,
        select_first_image,
        get_selected_image_link,
        move_to_next_image,
    ]

    for func in action_graph:
        if func.__name__ == "get_selected_image_link":
            link = func(driver)
            print(f"gotten link: {link}")
        else:
            success = False
            while not success:
                success = func(driver)

    time.sleep(10)
    driver.quit()
