# mirror-notify-android

An Android application that forwards notifications to Google Glass via [service](https://github.com/tikurahul/mirror-notify-python-server).

# Setup the client

* Go to [Glass Notification Service](http://glass-notify.appspot.com) and click on __Basic OAuth setup__ to complete OAuth setup.
* Once you complete the setup, click __Basic OAuth setup again__, to __verify__ if the setup was successful.
* Build and Install the APK (or get a copy of the [app](https://play.google.com/store/apps/details?id=com.rahulrav.glassnotify) from Google Play).
* Enable the Glass Notify Accessiblity Service (under Settings -> Accessibility).
* Launch the Glass Notify App, and make sure you see a your primary Gmail account listed.
* Whitelist applications whose notifications you want to forward by clicking the __Setup Whitelist__ button.
* Click on the __Test Glass Notifier__ button.
* Your Android notification should have been forwarded to Google Glass.