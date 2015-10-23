package com.amslayer.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

public class MusicPlayerActivity extends UnityPlayerActivity {

	private static MusicPlayerActivity	m_ActivityInstance;

	private Intent						m_PlayIntent;
	private boolean						m_MusicBound			= false;
	private boolean						m_ActivityPaused		= false;
	private boolean						m_MusicPlaybackPaused	= false;
	private Messenger					m_ServiceMessenger		= null;
	private final Messenger				m_ActivityMessenger		= new Messenger(new RemoteMessageHandler());

	private String						m_TrackName				= "NULL";

	public String getTrackName() {
		return m_TrackName;
	}

	public static Context getMusicPlayerActivityContext() {
		return m_ActivityInstance;
	}

	public Context getServiceConnectionStatus() {
		if (m_MusicBound) {
			return m_ActivityInstance;
		} else {
			return null;
		}
	}

	// Service Connection which is passed at the time of binding
	private ServiceConnection	m_MusicConnection	= new ServiceConnection() {

														@Override
														public void onServiceConnected(ComponentName name, IBinder service) {
															/*
															MusicBinder binder = (MusicBinder) service;
															m_MusicPlayerMessenger = binder.getService();
															*/

															m_ServiceMessenger = new Messenger(service);
															m_MusicBound = true;
														}

														@Override
														public void onServiceDisconnected(ComponentName name) {
															m_ServiceMessenger = null;
															m_MusicBound = false;
														}
													};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("com.amslayer.musicplayer", "Activity - onCreate");

		// DON'T forget to call the super class method
		super.onCreate(savedInstanceState);

		m_ActivityInstance = this;
	}

	@Override
	protected void onStart() {
		Log.e("com.amslayer.musicplayer", "Activity - onStart");

		// DON'T forget to call the super class method
		super.onStart();

		if (m_PlayIntent == null) {
			m_PlayIntent = new Intent(this, MusicPlayerService.class);
			bindService(m_PlayIntent, m_MusicConnection, Context.BIND_AUTO_CREATE);
			startService(m_PlayIntent);
		}
	}

	@Override
	protected void onResume() {
		Log.e("com.amslayer.musicplayer", "Activity - onResume");

		super.onResume();
		if (m_ActivityPaused) {
			m_ActivityPaused = false;
		}
	}

	@Override
	protected void onPause() {
		Log.e("com.amslayer.musicplayer", "Activity - onPause");

		super.onPause();
		m_ActivityPaused = true;
	}

	@Override
	protected void onStop() {
		Log.e("com.amslayer.musicplayer", "Activity - onStop");

		super.onStop();

		if (m_MusicBound) {
			unbindService(m_MusicConnection);
			m_MusicBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		Log.e("com.amslayer.musicplayer", "Activity - onDestroy");

		// stopService(m_PlayIntent);

		m_ServiceMessenger = null;

		// DON'T forget to call the super class method
		super.onDestroy();
	}

	public void Start() {
		Message msg = Message.obtain(null, MusicPlayerService.MC_RUN, 0, 0);
		msg.replyTo = m_ActivityMessenger;

		try {
			m_ServiceMessenger.send(msg);
		} catch (RemoteException exp) {
			exp.printStackTrace();
		}

		// m_MusicPlayerMessenger.Go();
	}

	public void Play() {
		Message msg = Message.obtain(null, MusicPlayerService.MC_PLAY_TRACK, 0, 0);
		msg.replyTo = m_ActivityMessenger;

		try {
			m_ServiceMessenger.send(msg);
		} catch (RemoteException exp) {
			exp.printStackTrace();
		}
	}

	public void Pause() {
		m_MusicPlaybackPaused = true;

		Message msg = Message.obtain(null, MusicPlayerService.MC_PAUSE_TRACK, 0, 0);
		msg.replyTo = m_ActivityMessenger;

		try {
			m_ServiceMessenger.send(msg);
		} catch (RemoteException exp) {
			exp.printStackTrace();
		}

		// m_MusicPlayerMessenger.PausePlayer();
	}

	// play next
	public void PlayNextTrack() {

		Message msg = Message.obtain(null, MusicPlayerService.MC_PLAY_NEXT_TRACK, 0, 0);
		msg.replyTo = m_ActivityMessenger;

		try {
			m_ServiceMessenger.send(msg);
		} catch (RemoteException exp) {
			exp.printStackTrace();
		}

		// m_MusicPlayerMessenger.PlayNext();

		if (m_MusicPlaybackPaused) {
			m_MusicPlaybackPaused = false;
		}
	}

	// play previous
	public void PlayPreviousTrack() {

		Message msg = Message.obtain(null, MusicPlayerService.MC_PLAY_PREVIOUS_TRACK, 0, 0);
		msg.replyTo = m_ActivityMessenger;

		try {
			m_ServiceMessenger.send(msg);
		} catch (RemoteException exp) {
			exp.printStackTrace();
		}

		// m_MusicPlayerMessenger.PlayPrevious();

		if (m_MusicPlaybackPaused) {
			m_MusicPlaybackPaused = false;
		}
	}

	/*
	public void TrackSelected(int trackIndex) {
		m_MusicPlayerMessenger.SetTrack(trackIndex);
		m_MusicPlayerMessenger.PlayTrack();

		if (m_MusicPlaybackPaused) {
			m_MusicPlaybackPaused = false;
		}
	}
	*/

	/*
	public int GetDuration() {
		if (m_MusicPlayerMessenger != null && m_MusicBound && m_MusicPlayerMessenger.isPlaying())
			return m_MusicPlayerMessenger.getDuration();
		else
			return 0;
	}
	*/

	/*
	public int GetCurrentPosition() {
		if (m_MusicPlayerMessenger != null && m_MusicBound && m_MusicPlayerMessenger.isPlaying())
			return m_MusicPlayerMessenger.getPosition();
		else
			return 0;
	}
	*/

	/*
	public void SeekTo(int pos) {
		m_MusicPlayerMessenger.SeekTo(pos);
	}
	*/

	/*
	public boolean IsPlaying() {
		if (m_MusicPlayerMessenger != null && m_MusicBound)
			return m_MusicPlayerMessenger.isPlaying();
		return false;
	}
	*/

	static class RemoteMessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case MusicPlayerService.MC_TRACK_NAME_CHANGED:
					m_ActivityInstance.m_TrackName = msg.getData().getString("CurrentTrackName");
					break;

				default:
					super.handleMessage(msg);
					break;
			}
		}
	}
}
