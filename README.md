This is a door opening system with decisions based on opencv's face recognition. Upon a button push a photo is taken, analyzed and decision is made on whether door is opened. 
The system contains some functions of AWS, namely S3, Lambda and API gateway. 

## Components
- ESP32-CAM
- AWS Lambda
- Android application 'OutKeeper'

![image](https://github.com/Atrop1n/OutKeeper/assets/92330911/f30abf51-b715-472c-bd3f-df1725ae161a)

## Features
- Door opening based on whether the person captured on a photo is familiar.
- Add people to the 'familiar persons' database, edit their photos. These photos are basically a training set for the face recognizer.
- View the last captured photo. If the person is not in the database, it is possible to let them in manually via the app.

## Configuration
Because the system is using AWS features, at least some basic skills in the platform are needed. It is required to set up some S3 buckets. One will contain persons' photos, another one the recognizer files, and the last one will contain python libraries required to run opencv2 on Lambda. We will use 2 lambda functions: train_recognizer and recognize_person. Last but not least, we will need some APIs to be able to interact between S3 buckets, application and ESP32-CAM board. Change API URLs in application and microcontroller code for your API URLs. 
![image](https://github.com/Atrop1n/OutKeeper/assets/92330911/c8b1dca3-fb2b-4746-aa7a-ef0d3836edd6)

The hardware of the system is in above picture. You can see door lock, button and indicator LEDs connected to the microcontroller. To make the microcontroller reachable from the internet, we also need to configure Ngrok tunnel. I am using the paid version which lets you have your own custom URL, but you don't need to. It is important to note that in order to use the system, the Ngrok secure tunnel needs to be running in the local network, otherwise photos captured by the camera module won't be available for remote analyzing.
