----------------------------------------------------------------------------------------

Disclaimer: Given Unity's frequent updates, the method to develop android plugins may change significantly from one version to another. So please refer to the official documentation for making android plugins. The links given below did not prove to be complete in themselves for me to make this plugin, and I had to spend considerable amount of time in figuring out the method which worked for me.

(Official Unity) Building Plugins for Android - http://docs.unity3d.com/Manual/PluginsForAndroid.html

----------------------------------------------------------------------------------------

>> Unity-Android plugin for playing audio files present on device

The repository has two projects in these folders: 
1) ./ump-eclipse-project - Contains the Eclipse project which has code for the Android plugin
2) ./ump-unity-project - Small Unity project with the Android plugin incorporated

----------------------------------------------------------------------------------------

Description of Eclipse Project: 
 
>> IMPORTANT: 'MusicPlayerActivity' overrides 'UnityPlayerActivity' - Depending upon your project, you may need to change the name of this class.

----------------------------------------------------------------------------------------

Description of Unity Project:

>> IMPORTANT: <Assets/Plugins/Android/AndroidManifest.xml> will have to be manually written to be correctly used in the project
>> <Assets/Scene-1> is the main scene
>> <Assets/MusicServiceConnection.cs> is the script which connects with the Android plugin and calls the relevant functions. 

----------------------------------------------------------------------------------------

Reference: 
1. Tutorial to make a music player on Android: 
	>> http://code.tutsplus.com/tutorials/create-a-music-player-on-android-user-controls--mobile-22787
	This tutorial was used as the base for making the eclipse project. For further improving the plugin please study this tutorial and add functions to eclipse code.
2. Resources about making a Unity plugin: 
	>> Creating An Android Java Plugin For Unity3D - https://blog.nraboy.com/2014/06/creating-an-android-java-plugin-for-unity3d/
	>> Custom Resources in a Unity Android Project - http://www.twodee.org/blog/?p=4951
	>> How to create a Native Android Plugin for Unity - http://www.lorenzonuvoletta.com/how-to-create-a-native-android-plugin-for-unity/

----------------------------------------------------------------------------------------