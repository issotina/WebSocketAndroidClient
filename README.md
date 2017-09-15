# WebSocketAndroidClient
#### Android webSocket client for [Ratchet Server](http://socketo.me/) 
Credit : This android library use [Autobahn-java](https://github.com/crossbario/autobahn-java)

## Installation
 ### 1 - Add it in your root build.gradle at the end of repositories:
```groovy

  allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
 ### 2 - Add the dependency
 ```groovy
 dependencies {
	        compile 'com.github.geeckmc:WebSocketAndroidClient:0.0.4'
	}
```
 
 ### 3 - Add packaging options
 ```groovy
 android
 {
 ...
  packagingOptions {
        exclude 'META-INF/LICENSE'
        exclude 'META-INF/ASL2.0'
    }
}
```
  
## Usage 
### 1 - Create an Web Socket Instance and start connection

```java
  Ws ws = new Ws.Builder().from( "ws://server_address");
  ws.connect();
```

### 2 - Subscribe to channel

 Basically get raw data 
```java 
        ws.on("path/to/channel", new Ws.WsListner() {
            @Override
            public void onEvent(String eventUri, Object data) {
                if(data != null) //your logic here
            }
        });
```
#### OR

 Get parsed object from json response,
 for example to get User from channel do something like this

```java

        ws.on("path/to/channel", User.class, new Ws.WsListner<User>() {
            @Override
            public void onEvent(String eventUri, User user) {
                if(user != null) Log.e(TAG,user.name);
            }
        });
```
### 2 - Send data to server

 ```java 
 ws.send("Hello World");
 ```
 or send to specific channel
 
 ```java
 ws.send("path/to/channel","Hello Channel");
 ```
License
-------

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
