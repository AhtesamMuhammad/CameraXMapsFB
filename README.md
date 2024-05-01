# CameraXMapsFB
This is a omprehensive Android mobile application developed using Java, Firebase, and XML files. It seamlessly integrates Firebase connectivity for backend operations and XML files for crafting the user interface. The application encompasses a robust authentication system, enabling both user login and registration. Additionally, it empowers users to capture and store images or videos with geotagging capabilities, seamlessly storing associated metadata in Firebase.

<br>

## The application is based on the following technologies:

- **Authentication:** An **Authentication** system is implemented to control access to the application.
- **Image Capture:** The application allows users to capture images using **CamaraX**.
- **Geolocation:** The **Google Maps API** is used to record the device's geographical location at the time the image is captured and store it in metadata.
- **Image Processing:** Captured images are processed and stored in **Firebase**.
- **Image Retrieval:** The application allows users to retrieve images stored in Firebase and display them in a fragment.

<br>

> [!IMPORTANT]
> ### Important Note:
> The application requires a minimum SDK of 31 and a target SDK of 33.
> Requires the YOUR_API_KEY in the file `AndroidManifest.xml`
> The `google-services` dependency must be installed for the application to function properly.

## Main Functionalities
1. **Login Screen: Allows** users to log in to the application using their credentials.
2. **Image Capture Screen:** Displays the camera view and allows users to capture an image.
3. **Image Display Screen:** Displays captured images stored in Firebase.
 
