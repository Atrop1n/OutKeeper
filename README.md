This is a door opening system with decisions based on opencv's face recognition. Upon a button push a photo is taken, analyzed and decision is made on whether door is opened. It is intended to make door unlocking more convenient.
The system's functionality is based on libary opencv and some AWS features, namely S3, Lambda and API gateway. 

## Components
- ESP32-CAM. This is a microcontroller board with camera module. Its job is taking a photo and providing it for analysis.
- AWS (Amazon Web Service). This platform provides remote cloud storage for training sets (S3) , as well as remote computing platform (Labmda) and API service (API Gateway).
- Android application 'OutKeeper'. This application allows user to directly configure the system.

![image](https://github.com/Atrop1n/OutKeeper/assets/92330911/f30abf51-b715-472c-bd3f-df1725ae161a)

## Prerequisities
Skills regarding setting up above AWS services are needed. 

## Configuration

![image](https://github.com/Atrop1n/OutKeeper/assets/92330911/c8b1dca3-fb2b-4746-aa7a-ef0d3836edd6)

The hardware of the system is depicted in above picture. You can see door lock, button and indicator LEDs connected to the microcontroller. To make the microcontroller reachable from the internet, we also need to configure Ngrok tunnel. I am using the paid version which lets you have your own custom URL, but you don't need to. It is important to note that in order to use the system, the Ngrok secure tunnel needs to be running in the local network, otherwise photos captured by the camera module won't be available for remote analyzing.

<img width="576" alt="scheme_door_lock" src="https://github.com/Atrop1n/OutKeeper/assets/92330911/9fdef351-293e-43e5-8c73-011476fd7e5f">


Because the system is using AWS features, at least some basic skills on the platform are needed. First we need to set up some S3 buckets. One will contain persons' photos, another one the recognizer files (Haar cascades), and the last one will contain python libraries required to run opencv2 on Lambda. You can name your buckets similarly to mine:
- persons-photos (contains photos)
- haar-cascades (contains face recognizer files)
- python-libraries (contains python libraries)

Keep in mind that bucket names need to be globally unique. If you're not able to use these exact names, edit them but make their purpose clear from the name.

We will use 2 Lambda functions: train_recognizer and recognize_person. 
Last but not least, we will need some APIs to be able to interact between S3 buckets, application and ESP32-CAM board. Change API URLs in application and microcontroller code for your API URLs. 

Please note that I have disabled all the original AWS APIs and Lambda functions. To make use of the program, you need to make your own AWS instance and adjust code appropriately.
