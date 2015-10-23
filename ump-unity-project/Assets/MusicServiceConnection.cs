using UnityEngine;
using System.Collections;
using System.Collections.Generic;

public class MusicServiceConnection : MonoBehaviour
{
//#if !UNITY_EDITOR && UNITY_ANDROID

		enum E_MUSIC_PLAYER_STATE
		{
				EMPS_STOPPED,
				EMPS_PLAYING,
				EMPS_PAUSED
		};

		public GameObject m_InitialScreen;
		public GameObject m_PlayScreen;

		public GameObject m_InitializationText;
		public GameObject m_StartPlayerButton;
		public GameObject m_PlayButton;
		public GameObject m_PlayButtonText;
		public GameObject m_CurrentTrackNameLabel;

		AndroidJavaClass androidClass;
		AndroidJavaObject androidActivity;

		E_MUSIC_PLAYER_STATE m_PlayerState = E_MUSIC_PLAYER_STATE.EMPS_STOPPED;

		bool m_ServiceConnectionDone = false;
		bool m_PlayScreenActive = false;

		void Awake ()
		{
				androidClass = new AndroidJavaClass ("com.amslayer.musicplayer.MusicPlayerActivity");
				androidActivity = androidClass.CallStatic<AndroidJavaObject> ("getMusicPlayerActivityContext");
		}

		void OnDestroy()
		{
				androidClass.Dispose ();
				androidActivity.Dispose ();
		}

		// Use this for initialization
		void Start ()
		{
				m_InitializationText.GetComponent<UnityEngine.UI.Text>().text = "Waiting...";
		}

		// Update is called once per frame
		void Update ()
		{
				if (!m_ServiceConnectionDone) {
						AndroidJavaObject statusObj = androidActivity.Call<AndroidJavaObject> ("getServiceConnectionStatus");

						if (statusObj != null) {
								m_ServiceConnectionDone = true;

								m_InitializationText.GetComponent<UnityEngine.UI.Text> ().text = "Tap the button below to play music on the device";
								m_StartPlayerButton.SetActive (true);
						}
				}

				if (m_PlayScreenActive) {
						string currentTrackName = androidActivity.Call<string> ("getTrackName");

						if (!currentTrackName.Equals ("NULL", System.StringComparison.Ordinal)) {
								m_CurrentTrackNameLabel.GetComponent<UnityEngine.UI.Text>().text = currentTrackName;
						}
				}
		}

		public void ShowPlaylistScreen ()
		{
				androidActivity.Call ("Start");

				m_InitialScreen.SetActive (false);
				m_PlayScreen.SetActive (true);

				m_PlayScreenActive = true;
		}

		public void StartPlayingMusic ()
		{
				switch (m_PlayerState) {
						case E_MUSIC_PLAYER_STATE.EMPS_STOPPED:
								androidActivity.Call ("Play");
								m_PlayerState = E_MUSIC_PLAYER_STATE.EMPS_PLAYING;
								m_PlayButtonText.GetComponent<UnityEngine.UI.Text> ().text = "Pause";
								break;

						case E_MUSIC_PLAYER_STATE.EMPS_PLAYING:
								androidActivity.Call ("Pause");
								m_PlayerState = E_MUSIC_PLAYER_STATE.EMPS_PAUSED;
								m_PlayButtonText.GetComponent<UnityEngine.UI.Text> ().text = "Play";
								break;

						case E_MUSIC_PLAYER_STATE.EMPS_PAUSED:
								androidActivity.Call ("Play");
								m_PlayerState = E_MUSIC_PLAYER_STATE.EMPS_PLAYING;
								m_PlayButtonText.GetComponent<UnityEngine.UI.Text> ().text = "Pause";
								break;
				}
		}

		public void PlayNextTrack ()
		{
				if (m_PlayerState == E_MUSIC_PLAYER_STATE.EMPS_PLAYING) {
						androidActivity.Call ("PlayNextTrack");
				}
		}

		public void PlayPreviousTrack ()
		{
				if (m_PlayerState == E_MUSIC_PLAYER_STATE.EMPS_PLAYING) {
						androidActivity.Call ("PlayPreviousTrack");
				}
		}
//#endif
}
