# RescuWave - An Emergency Response Coordination App

RescuWave is an innovative app engineered to streamline and enhance the coordination of emergency responses. By connecting people with the right rescue agencies during emergency situations, it ensures a swift and coordinated rescue effort. Users can report emergencies with a few taps, while RescuWave automatically shares location and details with relevant agencies, and notifies their emergency contacts. Users can track the arrival of rescue agencies on a live map, communicate directly, and experience the peace of mind that comes with a community prioritizing their safety.

## Features

* Two-level System: Both individual users and rescue agencies are integrated within a single platform
* Authentication: Secure authentication via three methods: Email/password, Phone-OTP, and Google sign-in
* Report Emergencies: Users can report emergencies by specifying the type of emergency and their location
* Nearby Rescue Agencies: Fetch nearby agencies using the Google Places API for quick assistance
* Rescue Agency Notifications: Rescue agencies receive tailored notifications based on their specialization
* Emergency Contacts Notification: SMS alerts are sent to users' emergency contacts, which is especially helpful when there is no internet connection.
* User Map View: Users can see the location of rescue agencies that have accepted their emergency
* Agency Map View: Rescue agencies can view filtered emergencies based on their specialization
* Communication: Users can directly contact any rescue agencies involved in their emergency, and rescue agencies can also contact users and other involved agencies.

## Tech Stack

* Development: Kotlin, XML
* Firebase:
  - Authentication
  - Cloud Firestore
  - Cloud Storage
  - Cloud Messaging
* MongoDB Realm
* Google Places SDK
* Google Maps SDK

## Screenshots

![Authentication](https://github.com/mNik033/RescuWave/assets/69163737/d3f6ccfb-d7fd-4eec-998c-b1bc9b2a2206)
![User Screens](https://github.com/mNik033/RescuWave/assets/69163737/9493aabc-6adf-4bbd-8a39-b119e5c5ebe4)
![Uploading Emergency](https://github.com/mNik033/RescuWave/assets/69163737/2bea84f8-b4db-4c3a-b2cf-673bca665f8e)
![Responding to Emergency](https://github.com/mNik033/RescuWave/assets/69163737/5d87e905-70ed-4c5b-b9c2-720af6f181f7)

## Build Requirements

- [Android Studio](https://developer.android.com/studio)
- [Google Maps API Key](https://developers.google.com/maps/documentation/android-sdk/get-api-key)
- [Google Places API Key](https://developers.google.com/maps/documentation/places/android-sdk/get-api-key)
- [MongoDB Realm App](https://www.mongodb.com/docs/atlas/app-services/apps/create/#std-label-create-app)

## Build Instructions

1. Clone the repository:

```bash
git clone https://github.com/mNik033/RescuWave.git
```

2. Add the following in gradle.properties:

```bash
android.defaults.buildfeatures.buildconfig=true
MY_REALM_APP_ID=YOUR_REALM_APP_ID
GOOGLE_MAPS_API_KEY=YOUR_MAPS_API_KEY
MY_GOOGLE_PLACES_API_KEY=YOUR_GOOGLE_PLACES_API_KEY
```

3. Set up Firebase:
   - Create a Firebase project in the [Firebase Console](https://console.firebase.google.com/).
   - Add the app to your Firebase project.
   - Enable the required authentication methods, Cloud Firestore and Cloud Storage.
   - Add your SHA1 and SHA256 certificate fingerprints in Project Settings.
   - Download the `google-services.json` file and place it in the `app` directory.

4. Build and run

## Contributors

* [Akash Shaw](https://github.com/Sky051)
* [Bhuvi Singh](https://github.com/Bhuviii03)
* [Nikhil](https://github.com/mNik033)
* [Pratyush Singh](https://github.com/Scholar2219w)
* [Saransh Shivhare](https://github.com/saranshs17)
* [Sanika Kole](https://github.com/sanikakole123)