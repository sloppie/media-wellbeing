from selenium.common.exceptions import TimeoutException
from selenium.webdriver import ChromeOptions, Chrome
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.common.by import By
import selenium.webdriver.support.expected_conditions as EC
from pyvirtualdisplay import Display
import time
import re
from tqdm import tqdm
import platform

executable_path = "chrome-driver/chromedriver.exe" if re.search("Windows", platform.platform())\
    else "chrome-driver/chromedriver"


PATH = "C:\\Program Files (x86)\\web-drivers\\chrome\\chromedriver.exe"
display = None  # contains the display used on the server
chrome_options = ChromeOptions()

if re.search("Windows", platform.platform()):
    chrome_options.add_argument("--incognito")
else:
    display = Display(False, size=(1024, 768))
    display.start()
    chrome_options.add_argument("--headless")

driver = Chrome(executable_path=executable_path, options=chrome_options)


def search_for_item_from_homepage(web_driver, value):
    try:
        web_driver.get("https://duckduckgo.com")
        # get the search form of the duckduckgo search engine
        search_form = WebDriverWait(web_driver, timeout=5).until(
            lambda d: web_driver.find_element_by_id("search_form_input_homepage")
            # lambda d: EC.element_to_be_clickable(By.ID, "search_form_input_homepage")
        )
        search_form.click()
        search_form.send_keys(value)  # enter search
        search_form.send_keys(Keys.RETURN)

        return True
    except Exception as ex:
        print(ex)

        return False


def search_for_item(web_driver, value):
    try:
        # get the search form of the duckduckgo search engine
        search_form = WebDriverWait(web_driver, timeout=5).until(
            lambda d: web_driver.find_element_by_id("search_form_input")
            # lambda d: EC.element_to_be_clickable(By.ID, "search_form_input_homepage")
        )
        search_form.click()
        search_form.clear()
        search_form.send_keys(value)  # enter search
        search_form.send_keys(Keys.RETURN)

        return True
    except Exception as ex:
        print(ex)

        return False


def turn_off_search_moderation(web_driver):
    try:
        filter_menu = WebDriverWait(web_driver, timeout=15).until(
            lambda d: web_driver.find_element_by_xpath(
                "/html/body/div[2]/div[5]/div[3]/div/div[1]/div[1]/div/div[2]/a"))
        filter_menu.click()
        off_option = WebDriverWait(web_driver, timeout=15).until(
            lambda d: web_driver.find_element_by_xpath("/html/body/div[6]/div[2]/div/div/ol/li[3]/a")
        )
        off_option.click()
        res = WebDriverWait(web_driver, timeout=15).until(
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
        duckbar = WebDriverWait(web_driver, timeout=15).until(
            lambda d: web_driver.find_element_by_id("duckbar_static")
        )
        items = WebDriverWait(duckbar, timeout=15).until(
            lambda d: duckbar.find_elements_by_class_name("zcm__link")
        )
        time.sleep(1)
        for list_item in items:
            if list_item.text == "Images":
                list_item
                list_item.click()
                break

        return True
    except Exception as ex:
        print(ex)

        return False


def set_image_size(web_driver):
    try:
        size_dropdown = WebDriverWait(web_driver, timeout=15).until(
            EC.visibility_of_element_located((By.CLASS_NAME, "dropdown--size"))
        )
        size_dropdown.click()  # open modal dropdown
        time.sleep(2)
        modal_dropdown = WebDriverWait(web_driver, timeout=15).until(
            lambda d: web_driver.find_element_by_class_name("modal__list")
        )
        anchor_elements = WebDriverWait(modal_dropdown, timeout=15).until(
            lambda d: modal_dropdown.find_elements_by_tag_name("a")
        )
        for anchor_element in anchor_elements:
            if anchor_element.text == "Medium":
                print("Size of photos set to medium")
                anchor_element.click()
                break
        # This sleep is necessary since the window needs to refresh to update images with the new
        # changes that come with changing the flag in question
        time.sleep(2)

        return True
    except Exception as ex:
        print(ex)

        return False


def set_image_type(web_driver):
    try:
        type_dropdown = WebDriverWait(web_driver, timeout=15).until(
            EC.visibility_of_element_located((By.CLASS_NAME, "dropdown--type"))
        )
        type_dropdown.click()  # open the modal
        time.sleep(1)
        anchor_elements = web_driver.find_elements_by_class_name("js-dropdown-items")

        for anchor_element in anchor_elements:
            if anchor_element.text == "Photograph":
                print("Type of image set to: Photograph")
                anchor_element.click()
                break
        # This sleep is necessary since the window needs to refresh to update images with the new
        # changes that come with changing the flag in question
        time.sleep(2)

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
        filter_dropdown = WebDriverWait(web_driver, 15).until(
            EC.visibility_of_element_located((By.CLASS_NAME, "dropdown--safe-search"))
        )
        filter_dropdown.click()
        modal_list = WebDriverWait(web_driver, timeout=15).until(
            lambda d: web_driver.find_element_by_class_name("modal__list")
        )
        anchor_elements = modal_list.find_elements_by_tag_name("a")
        for anchor_element in anchor_elements:
            if re.search("Off", anchor_element.text):
                anchor_element.click()
                print("SafeSearch set to: off")
                break
        # This sleep is necessary since the window needs to refresh to update images with the new
        # changes that come with changing the flag in question
        time.sleep(2)

        return True
    except Exception as ex:
        print(ex)

        return False


def select_first_image(web_driver):
    try:
        image_tile = WebDriverWait(web_driver, timeout=15).until(
            EC.element_to_be_clickable((By.CLASS_NAME, "tile--img"))
        )
        image_tile.click()  # clicks on the first image

        return True
    except Exception as ex:
        print(ex)

        return False  # unable to select the image


def get_selected_image_link(web_driver, is_first_image):
    time.sleep(1)
    if is_first_image:
        try:
            selected_image_desc = WebDriverWait(web_driver, timeout=15).until(
                EC.visibility_of_element_located((By.CLASS_NAME, "c-detail__desc"))
            )
            # scroll image into view
            web_driver.execute_script("arguments[0].scrollIntoView", selected_image_desc)
        except TimeoutException as te:
            print(f"{te.__class__.__name__}: Unable to scroll in the selected image into view")

        try:
            anchor_tag = WebDriverWait(web_driver, 15).until(
                EC.element_to_be_clickable((By.CLASS_NAME, "c-detail__btn")))
            image_href = anchor_tag.get_attribute("href")
            # print(f"href=\"{image_href}\"")

            return image_href
        except TimeoutException as ex:
            print(f"{ex.__class__.__name__}: Unable to fetch \"href\" attr of selected image")
            return ""
    else:
        try:
            anchor_tag = web_driver.find_element(By.CLASS_NAME, "c-detail__btn")
            image_href = anchor_tag.get_attribute("href")
            # print(f"href=\"{image_href}\"")

            return image_href
        except TimeoutException as ex:
            print(f"{ex.__class__.__name__}: Unable to fetch \"href\" attr of selected image")

            return ""  # shows that the image was not found


def move_to_next_image(web_driver):
    try:
        next_image = WebDriverWait(web_driver, 15).until(
            EC.element_to_be_clickable((By.CLASS_NAME, "js-detail-next"))
        )
        next_image.click()

        return True
    except Exception as ex:
        print(ex)

        return False


def dismiss_add_to_chrome_badge(web_driver):
    try:
        badge_link = WebDriverWait(web_driver, 15).until(
            lambda d: web_driver.find_element(By.CLASS_NAME, "badge-link")
        )

        if badge_link.is_displayed():
            badge_link_dismiss = WebDriverWait(web_driver, 15).until(
                EC.element_to_be_clickable((By.CLASS_NAME, "js-badge-link-dismiss"))
            )
            badge_link_dismiss.click()

        return True
    except Exception as ex:
        print(ex)
        return False


def download_images(web_driver, search_value, target_location):
    search_for_item(web_driver, search_value)

    time.sleep(3)

    select_first_image(driver)
    # tries to run the whole thing like a graph
    search_action_graph = [
        get_selected_image_link,
        move_to_next_image,
    ]

    link_list = []  # stores all the collected links in while scrapping the category

    for image_count in tqdm(range(600)):
        # fetching the first image requires a slightly different process so we have to confirm
        # whether it is the fist image or not
        is_first_searched_image = (image_count == 0)

        for action in search_action_graph:
            if action.__name__ == "get_selected_image_link":
                img_link = action(web_driver, is_first_searched_image)
                link_list.append(img_link)
            else:
                is_success = False
                while not is_success:
                    is_success = action(web_driver)

    # instead of maintaining a list which may increase the amount of memory needed to eun the program for excessively
    # large lists, it was opted to create a data dir and the search_keyword.txt file after the function continues
    # executing
    # dump all the links into a file before proceeding
    export_scrapped_links(link_list, target_location)


def export_scrapped_links(image_links, target_location):
    data = ""
    for image_link in image_links:
        data += f"{image_link}\n"

    link_file = open(f"data/{target_location}.txt", "w")
    link_file.write(data)
    link_file.close()


if __name__ == "__main__":
    search_for_item_from_homepage(driver, "Test Search")
    time.sleep(3)
    # set up the configuration to allow for explicit images to be shown
    config_graph = [
        dismiss_add_to_chrome_badge,
        select_image_tab,
        set_moderation_off,
        set_image_size,
        set_image_type,
    ]

    print("setting up image config")

    for i, func in enumerate(config_graph):
        print(f"Step: {i}, func: {func.__name__}")
        if func.__name__ == "get_selected_image_link":
            link = func(driver)
        else:
            success = False
            while not success:
                success = func(driver)

    with open("keylist.txt") as key_list:
        categories = key_list.readlines()
        for i, category in enumerate(categories):
            if category:
                search_category, new_line = category.split("\n")
                print(f"Current Category: {search_category}")
                search_term = f"pornhub {search_category} nude images"

                download_images(driver, search_term, search_category)

    driver.quit()

    # close the display for the server
    if not re.search("Windows", platform.platform()):
        display.stop()