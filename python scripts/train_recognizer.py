import json
import boto3
import cv2
import os
import numpy as np

s3 = boto3.resource('s3')
s3_client = boto3.client("s3")
BUCKET_PHOTOS = s3.Bucket('my-facerecognizerbucket-84808')
BUCKET_CASCADES = 'my-haarcascades-84808'
people = []
person_photos = []
features = []
features_np = []
labels = []
labels_np = []


def rescale_frame(frame,scale=0.5):
    width = int(frame.shape[1] * scale)
    height = int(frame.shape[0] * scale)
    dimensions = (width,height)
    return cv2.resize(frame,dimensions,interpolation=cv2.INTER_AREA)

def lambda_handler(event, context):
    # Remove previous instance of trained faces yml file from temp memory
    if os.path.exists('/tmp/face_trained.yml'):
        os.remove("/tmp/face_trained.yml")
        print('removed')
    else:
        print('not_removed')
    # Remove previous instance of trained faces yml file from S3 Bucket
    s3_client.delete_object(Bucket = BUCKET_CASCADES,Key="face_trained.yml")
    s3_client.download_file(BUCKET_CASCADES , 'haar_face.xml', '/tmp/haar_face.xml')
    # Create training set
    create_training_set()
    # Convert created set to numpy array
    features_np = np.array(features,dtype='object')
    labels_np = np.array(labels)
    face_recognizer = cv2.face.LBPHFaceRecognizer_create()
    # Train and save face recognizer as yml file
    face_recognizer.train(features_np,labels_np)
    face_recognizer.save('/tmp/face_trained.yml')
    # Upload yml file to S3 bucket
    s3_client.upload_file('/tmp/face_trained.yml', BUCKET_CASCADES, 'face_trained.yml')
    return {
    "statusCode": 200,
    "body": "OK"
    }
    
def create_training_set():
    haar_cascade = cv2.CascadeClassifier('/tmp/haar_face.xml')
    for photo in BUCKET_PHOTOS.objects.all():
        name = photo.key.split('/')[0]
        if name not in people and name != 'recent_visitor.jp':
            people.append(name)
        person_photos.append(photo.key)
    print(people)
    for person in people:
        label = people.index(person)
        for img in person_photos:
            if (img.split('/')[0] == person) and (img.split('/')[1] != ""):
                print(img)
                response = s3_client.get_object(Bucket='my-facerecognizerbucket-84808', Key=img)
                image_bytes = response['Body'].read()
                img = cv2.imdecode(np.frombuffer(image_bytes, np.uint8), cv2.IMREAD_COLOR)
                gray = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
                faces_rect = haar_cascade.detectMultiScale(gray,scaleFactor=1.3,minNeighbors=4)
                for (x,y,w,h) in faces_rect:
                    faces_roi = gray[y:y+h,x:x+w]
                    features.append(faces_roi)
                    labels.append(label)
   
