import boto3
import json
import cv2
import os
import numpy as np
import urllib.request
#import requests

s3 = boto3.resource('s3')
s3_client = boto3.client("s3")
BUCKET_PHOTOS = s3.Bucket('my-facerecognizerbucket-84808')
BUCKET_CASCADES = 'my-haarcascades-84808'

people = []

def get_people():
    for my_bucket_object in BUCKET_PHOTOS.objects.all():
        name = my_bucket_object.key.split('/')[0]
        if name not in people and name != 'recent_visitor.jpg':
            people.append(name)
    print(people)
    
def rescale_frame(frame,scale=0.5):
    width = int(frame.shape[1] * scale)
    height = int(frame.shape[0] * scale)
    dimensions = (width,height)
    return cv2.resize(frame,dimensions,interpolation=cv2.INTER_AREA)

def lambda_handler(event, context):
    get_people();
    haar_cascade = cv2.CascadeClassifier('/tmp/haar_face.xml')
    face_recognizer = cv2.face.LBPHFaceRecognizer_create()
    s3_client.download_file(BUCKET_CASCADES , 'face_trained.yml', '/tmp/face_trained.yml')
    face_recognizer.read('/tmp/face_trained.yml')
    url = 'http://outkeeper.eu.ngrok.io/saved-photo'
    file_path = '/tmp/recent_visitor.jpg'
    urllib.request.urlretrieve(url, file_path)
    img = cv2.imread(file_path)
    cv2.imwrite('/tmp/recent_visitor.jpg', img, [int(cv2.IMWRITE_JPEG_QUALITY), 90])
    content_type = 'image/jpeg'
    with open('/tmp/recent_visitor.jpg', 'rb') as file:
        s3_client.upload_fileobj(file, 'my-haarcascades-84808', 'recent_visitor.jpg', ExtraArgs={'ContentType': 'image/jpeg'})
    with open('/tmp/recent_visitor.jpg', 'rb') as file:
        data = file.read()
        arr = bytearray(data)
    arr = np.asarray(arr, dtype=np.uint8)
    img = cv2.imdecode(arr,-1)
    img = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    faces_rect = haar_cascade.detectMultiScale(img,scaleFactor=1.3,minNeighbors=4)
    for (x,y,w,h) in faces_rect:
        # Extract areas with faces only
        faces_roi = img[y:y+h,x:x+w]
        # Do face recognition
        label,confidence = face_recognizer.predict(faces_roi)
        print(label)
        if(confidence<100):
                    cv2.putText(img,str(str(people[label])+str(confidence)),(x,y-10),cv2.FONT_HERSHEY_SIMPLEX,1.0,(255,0,0),1)

        cv2.rectangle(img,(x,y),(x+h,y+w),(255,0,0),thickness=2)
    img = rescale_frame(img,1)
    cv2.imwrite("/tmp/recent_visitor_detected.jpg", img)
    with open('/tmp/recent_visitor_detected.jpg', 'rb') as file:
        s3_client.upload_fileobj(file, 'my-haarcascades-84808', 'recent_visitor_detected.jpg', ExtraArgs={'ContentType': 'image/jpeg'})
    # TODO implement
    return {
    'statusCode': 200,
    'body': json.dumps({
        'name': people[label],
        'confidence': confidence
    })
    }

   
