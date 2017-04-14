
# PhotoPicker

---

# Example
![](http://ww2.sinaimg.cn/large/5e9a81dbgw1etra61rnr9j206z0ce3yu.jpg)
![](http://ww3.sinaimg.cn/large/5e9a81dbgw1etra6q2edzj206z0cedgg.jpg)
![](http://ogkb67oc8.bkt.clouddn.com/F028B942CF5978D900B15033941478B7.jpg?imageView2/2/w/250/)
<img src="https://github.com/Qiu800820/PhotoPicker/raw/master/img/device-2017-04-14-154555.png" width = "251" height = "446"/>
<img src="https://github.com/Qiu800820/PhotoPicker/raw/master/img/screenshot2017-04-14_001.png" width = "251" height = "446"/>
<img src="https://github.com/Qiu800820/PhotoPicker/raw/master/img/screenshot2017-04-14_002.png" width = "251" height = "446"/>
<img src="https://github.com/Qiu800820/PhotoPicker/raw/master/img/device-2017-04-14-151354.png" width = "251" height = "446"/>


---

# Usage

### Gradle

```groovy
dependencies {
    compile 'me.iwf.photopicker:PhotoPicker:0.9.5@aar'
    
    compile 'com.android.support:appcompat-v7:23.4.0'
    compile 'com.android.support:recyclerview-v7:23.4.0'
    compile 'com.android.support:design:23.4.0'
    compile 'com.nineoldandroids:library:2.4.0'
    compile 'com.github.bumptech.glide:glide:3.7.0'
}
```
* ```appcompat-v7```version >= 23.0.0

### eclipse
[![GO HOME](http://ww4.sinaimg.cn/large/5e9a81dbgw1eu90m08v86j20dw09a3yu.jpg)

### Pick Photo
```java
PhotoPicker.builder()
    .setPhotoCount(9)
    .setShowCamera(true)
    .setShowGif(true)
    .setPreviewEnabled(false)
    .start(this, PhotoPicker.REQUEST_CODE);
```

### Pick Photo And Video
```java
PhotoPicker.builder()
    .setPhotoCount(9)
    .setShowCamera(true)
    .setShowGif(true)
    .setShowVideo(true)
    .setPreviewEnabled(false)
    .start(this, PhotoPicker.REQUEST_CODE);
```

### Preview Photo

```java
ArrayList<String> photoPaths = ...;

PhotoPreview.builder()
    .setPhotos(selectedPhotos)
    .setCurrentItem(position)
    .setShowDeleteButton(false)
    .start(MainActivity.this);
```

### onActivityResult
```java
@Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK && requestCode == PhotoPicker.REQUEST_CODE) {
        ArrayList<Media> photos = null;
            if (data != null) {
                photos = data.getParcelableArrayListExtra(PhotoPicker.KEY_SELECTED_PHOTOS);
            }
    }
}
```

### manifest
```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    >
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
    <uses-permission android:name="android.permission.CAMERA" />
  <application
    ...
    >
    ...
    
    <activity android:name="me.iwf.photopicker.PhotoPickerActivity"
      android:theme="@style/Theme.AppCompat.NoActionBar" 
       />

    <activity android:name="me.iwf.photopicker.PhotoPagerActivity"
      android:theme="@style/Theme.AppCompat.NoActionBar"/>
    
  </application>
</manifest>
```
### Custom style
```xml
<style name="actionBarTheme" parent="ThemeOverlay.AppCompat.Dark.ActionBar">
  <item name="android:textColorPrimary">@android:color/primary_text_light</item>
  <item name="actionBarSize">@dimen/actionBarSize</item>
</style>

<style name="customTheme" parent="Theme.AppCompat.Light.NoActionBar">
  <item name="actionBarTheme">@style/actionBarTheme</item>
  <item name="colorPrimary">#FFA500</item>
  <item name="actionBarSize">@dimen/actionBarSize</item>
  <item name="colorPrimaryDark">#CCa500</item>
</style>
```

### Proguard

```
# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
# nineoldandroids
-keep interface com.nineoldandroids.view.** { *; }
-dontwarn com.nineoldandroids.**
-keep class com.nineoldandroids.** { *; }
# support-v7-appcompat
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }
-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}
# support-design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }
```

---


# License

    Copyright 2015 Huang Donglu

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



