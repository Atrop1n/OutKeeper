#include <SPI.h>
#include <WiFi.h>
#include <SPIFFS.h>
#include <RadioLib.h>
#include <ESPAsyncWebServer.h>
#include <EEPROM.h>
#include <time.h>
#include <ArduinoJson.h>
#include <HTTPClient.h>
#include "esp_camera.h"
//define camera pins
#define PWDN_GPIO_NUM 32
#define RESET_GPIO_NUM -1
#define XCLK_GPIO_NUM 0
#define SIOD_GPIO_NUM 26
#define SIOC_GPIO_NUM 27
#define Y9_GPIO_NUM 35
#define Y8_GPIO_NUM 34
#define Y7_GPIO_NUM 39
#define Y6_GPIO_NUM 36
#define Y5_GPIO_NUM 21
#define Y4_GPIO_NUM 19
#define Y3_GPIO_NUM 18
#define Y2_GPIO_NUM 5
#define VSYNC_GPIO_NUM 25
#define HREF_GPIO_NUM 23
#define PCLK_GPIO_NUM 22
#define PIR 1
#define FILE_PHOTO "/photo.jpg" //path to the image in SPIFFS
#define EEPROM_SIZE 1
#define BUTTON_PIN 15
#define LED_RED 12
#define LOCK_PIN 13

int EEPROMPosition = 0;

const char* ssid = "Telekom-792190";
const char* password = "8aubtf5d59r7tghx";
AsyncWebServer server(80);

int button_state = 1; //'0' means the button is pressed
int take_photo = 0;
int let_in = 0;
unsigned long startMillis;  //some global variables available anywhere in the program
unsigned long currentMillis;
File file;
File photo;
String path;
String timestamp;
const char index_html[] PROGMEM = R"rawliteral(
<!DOCTYPE HTML>
<html>
<head>
   <meta name="viewport" content="width=device-width, initial-scale=1">
   <style>
      body {
         text-align: center;
         background-color: rgb(29, 45, 189);
         font-family: Verdana, Geneva, Tahoma, sans-serif;
         color: rgb(44, 171, 191);
      }
      h2 {
         font-weight: 600;
         font-size: 32px;
         text-shadow: 1px 1px 1px rgb(0, 0, 0);
      }
      .p {
         padding: 0px;
         margin: 5px;
         /* background-color: blue; */
      }
      .title {
         margin: 5px;
      }
      img {
         max-width: 100%%;
         max-height: 100%%;
         width: 768px;
         height: 432px;
      }
      .picture-holder {
         margin-top: 5px;
         margin-bottom: 5px;
         display: inline-block;
         position: relative;
         width: 768px;
         height: 432px;
         max-width: 100%%;
         border-radius: 1px;
         box-shadow: 1px 1px 12px rgb(0, 0, 0);
      }
      @media (max-width: 767px) {
         .p {
            padding: 0px;
            margin: auto;
         }
         .picture-holder {
            height: auto;
            width: auto;
            border: none;
            box-shadow: none;
            margin-bottom: auto;
         }
         img {
            border: solid rgb(0, 0, 0);
            box-shadow: 1px 1px 6px rgb(0, 0, 0);
            max-width: 100%%;
            width: auto;
            height: auto;
         }
      }
   </style>
</head>
<body>
   <h2 class="title">Face detector</h2>

         <div class="picture-holder">
   <img src="saved-photo" id="photo">
        </div>
   <script>
       function newPhoto() {
         var xhttp = new XMLHttpRequest();
         var x = 1;
         xhttp.open("GET", "/update?takePhoto="+ x, true);
         xhttp.send();
      }
   </script>
</body>
</html>)rawliteral";

String processor(const String &
  var) {
  if (var == "PATH") {
    return String(path);
  } else if (var == "TIMESTAMP") {
    return timestamp;
  }
  return String();
}

void new_photo() {
   camera_fb_t* fb = NULL;
    fb = esp_camera_fb_get(); 
    if (!fb) {
      Serial.println("Camera capture failed");
      Serial.println("Restarting");
      ESP.restart();
      return;
    }
    
    // Photo file name
    Serial.printf("Picture file name: %s\n", FILE_PHOTO);
    File file = SPIFFS.open(FILE_PHOTO, FILE_WRITE);

    // Insert the data in the photo file
    if (!file) {
      Serial.println("Failed to open file in writing mode");
    }
    else {
      file.write(fb->buf, fb->len); // payload (image), payload length
      Serial.print("The picture has been saved in ");
      Serial.print(FILE_PHOTO);
      Serial.print(" - Size: ");
      Serial.print(file.size());
      Serial.println(" bytes");
    }
    // Close the file
    file.close();
    esp_camera_fb_return(fb);
    delay(1000);
}
//when a photo is fully transmitted
int send_photo() {
  int confidence = 0;
 // Make HTTP GET request
  HTTPClient http;
  http.begin("https://xuhqq2omx3.execute-api.eu-central-1.amazonaws.com/v1/recoginze_person"); // Replace with the URL of the server you want to make the request to
  int httpCode = http.GET();

  // Parse JSON response
  DynamicJsonDocument doc(1024);
  deserializeJson(doc, http.getString());

  // Extract value of "body"
  String body = doc["name"];
  confidence = doc["confidence"];

  // Print value of "body"
  Serial.print("Value of 'name': ");
  Serial.println(body);

  // Close connection
  http.end();
  return confidence;
}

void getTime() {
  time_t now = time(NULL);
  struct tm tm_now;
  localtime_r( & now, & tm_now);
  char buff[100];
  strftime(buff, sizeof(buff), "%Y-%m-%d  %H:%M:%S", & tm_now);
  timestamp = buff;
  Serial.println(timestamp);
}

void wifiConnect() {
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("Connecting to WiFi...");
  }
  Serial.print("IP Address: http://");
  Serial.println(WiFi.localIP());
}

void setup() {
  Serial.begin(115200);
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM;
  config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM;
  config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM;
  config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM;
  config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM;
  config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM;
  config.pin_href = HREF_GPIO_NUM;
  config.pin_sscb_sda = SIOD_GPIO_NUM;
  config.pin_sscb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM;
  config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 5000000;
  config.frame_size = FRAMESIZE_SVGA;
  config.pixel_format = PIXFORMAT_JPEG; // for streaming
  //config.pixel_format = PIXFORMAT_RGB565; // for face detection/recognition
  config.grab_mode = CAMERA_GRAB_WHEN_EMPTY;
  config.fb_location = CAMERA_FB_IN_PSRAM;
  config.jpeg_quality = 12;
  config.fb_count = 1;
  
  // if PSRAM IC present, init with UXGA resolution and higher JPEG quality
  //                      for larger pre-allocated frame buffer.
  if(config.pixel_format == PIXFORMAT_JPEG){
    if(psramFound()){
      config.jpeg_quality = 6;
      config.fb_count = 2;
      //config.brigthness = 4;
      config.grab_mode = CAMERA_GRAB_LATEST;
    } else {
      // Limit the frame size when PSRAM is not available
      config.frame_size = FRAMESIZE_SVGA;
      config.fb_location = CAMERA_FB_IN_DRAM;
    }
  } else {
    // Best option for face detection/recognition
    config.frame_size = FRAMESIZE_240X240;
  }


  // camera init
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed with error 0x%x", err);
    return;
  }

  sensor_t * s = esp_camera_sensor_get();
  s->set_brightness(s, 0); 
  s->set_contrast(s, 0); 
  s->set_saturation(s,2);
  s->set_special_effect(s,2);
  s->set_whitebal(s,1);
  s->set_awb_gain(s,1);
  s->set_wb_mode(s,0);
  s->set_exposure_ctrl(s,1);
  s->set_aec2(s,0);
  s->set_ae_level(s,0);
  //s->set_aec_value(s,1200);
  s->set_gain_ctrl(s,1);
  s->set_gainceiling(s,GAINCEILING_128X);
  //s->set_agc_gain(s,12);
  s->set_bpc(s,0);
  s->set_wpc(s,0);
  s->set_raw_gma(s,1);
  s->set_lenc(s,0);
  wifiConnect();
  server.begin();
  SPIFFS.begin(true);
  if (!SPIFFS.begin(true)) {
    Serial.println("An Error has occurred while mounting SPIFFS");
    ESP.restart();
  }

  server.on("/", HTTP_GET, [](AsyncWebServerRequest * request) {
    request -> send_P(200, "text/html", index_html, processor);
  });
  server.on("/update", HTTP_GET, [](AsyncWebServerRequest * request) {
    if (request -> hasParam("takePhoto")) {
     take_photo = (request -> getParam("takePhoto") -> value()).toInt();
     }
    if (request -> hasParam("let_in")) {
     let_in = (request -> getParam("let_in") -> value()).toInt();
     }
    //if (request -> hasParam("spreadFactor")) {
    //spreadFactor = (request -> getParam("spreadFactor") -> value()).toInt();
    //}
    request -> send(200, "text/plain", "OK");
  });
  server.on("/saved-photo", HTTP_GET, [](AsyncWebServerRequest * request) {
    request -> send(SPIFFS, FILE_PHOTO, "image/jpg", false);
  });
  SPIFFS.remove("/photo.jpg");
  configTime(3600, 0, "ntp.telekom.sk"); //choose correct ntp server for your region, first argument is time shift in seconds
  startMillis = millis();
  pinMode(BUTTON_PIN, INPUT);
  pinMode(LOCK_PIN,OUTPUT);
  pinMode(LED_RED,OUTPUT);
}



void loop() {
  button_state = digitalRead(BUTTON_PIN);
  digitalWrite(LOCK_PIN, LOW);
  digitalWrite(LED_RED, LOW);
  if (let_in == 1)
  {   
    digitalWrite(LOCK_PIN, HIGH); //unlock
  delay(5000);
  let_in = 0;
  }
  if (button_state == 0)
  {
    Serial.println("Button pressed");
    button_state = 1;
    new_photo();
    int confidence = send_photo();
    if(confidence>60 | confidence == 0)
    {
      digitalWrite(LED_RED, HIGH);
    }
    else  {
     digitalWrite(LOCK_PIN, HIGH); //unlock
    }
    Serial.println("Confidence is "+String(confidence));
    take_photo = 0;
    Serial.println("Photo taken and sent");
    delay(5000);
    
  }
  /*if(take_photo == 1)
  {
    new_photo();
    send_photo();
    take_photo = 0;
    Serial.println("Photo taken and sent");
  }
  */

}
