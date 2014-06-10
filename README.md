nuance-phonegap-plugin
======================

This is a Cordova 3.x compatible plugin built to support nuance's mobile sdk for being able to do voice recognition and tts


Installation Instructions:

1. Sign up for a developer account at http://dragonmobile.nuancemobiledeveloper.com/public/index.php?task=home


2. Download the IOS SDK

3.  Add the IOS SDK .framework file as Nuance instructs you to in there installation directions.

4.  Install this plugin to your cordova project

cordova plugin add https://github.com/chalettu/nuance-phonegap-plugin.git

5.  Configure the plugin with your credentials in the Plugins/credentials.m file
![alt tag](https://raw.github.com/chalettu/nuance-phonegap-plugin/master/readme_resources/plugin_pic_1.png)
![alt tag](https://raw.github.com/chalettu/nuance-phonegap-plugin/master/readme_resources/plugin_step2.png)

6.	At this point you should be able to do a cordova build ios and have your project compile successfully

7.	I am working on getting more detailed documentation on some of the javascript calls but in the mean time if you have questions feel free to ask a question about how to use this plugin.