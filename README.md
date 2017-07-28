# Flutter API

There is an artifact available in the Maven repository. These is not functional, however, and only provide stubbed implementations of the API. All methods in all classes throw a runtime exception. Because an Android app runs on a device, it will never use these libraries for execution, but the API compatibility allows an app to be compiled as if it were the real library.

## Repo
[![](https://jitpack.io/v/Scarabei/Flutter.svg)](https://jitpack.io/#Scarabei/Flutter)

## Usage

#### Step 1. Add the JitPack repository to your build file

Add it in your root build.gradle at the end of repositories:

```	
ext {
   flutterAPIversion = "5278588d80-2017-07-24"
}

allprojects {
   repositories {
      ...
      maven { url 'https://jitpack.io' }
   }
}
```

#### Step 2. Add the dependency

```
  dependencies {
      compile 'com.github.Scarabei.Flutter:flutter-api:$flutterAPIversion'
  }
```


