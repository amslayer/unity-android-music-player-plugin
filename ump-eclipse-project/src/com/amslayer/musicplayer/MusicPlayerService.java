package com.amslayer.musicplayer;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

// Music Player Service
public class MusicPlayerService extends Service {

	private static MusicPlayerService	m_ServiceInstance;

	public static final int				NOTIFY_ID				= 1;
	public static final int				MC_RUN					= 0;
	public static final int				MC_TERMINATE			= 1;
	public static final int				MC_PLAY_TRACK			= 2;
	public static final int				MC_PAUSE_TRACK			= 3;
	public static final int				MC_STOP_TRACK			= 4;
	public static final int				MC_PLAY_NEXT_TRACK		= 5;
	public static final int				MC_PLAY_PREVIOUS_TRACK	= 6;
	public static final int				MC_TRACK_NAME_CHANGED	= 7;

	private final Messenger				m_ServiceMessenger		= new Messenger(new RemoteMessageHandler());
	private Messenger					m_ActivityMessanger		= null;

	private Thread						m_MediaPlayerThread;
	private MediaPlayerRunnable			m_MediaPlayerRunnable;

	// Called when the service is created
	@Override
	public void onCreate() {
		Log.e("com.amslayer.musicplayer", "Service - onCreate");

		// create the service
		super.onCreate();

		m_ServiceInstance = this;

		m_ServiceInstance.m_MediaPlayerRunnable = new MediaPlayerRunnable(m_ServiceInstance.getApplicationContext());
		m_ServiceInstance.m_MediaPlayerThread = new Thread(m_ServiceInstance.m_MediaPlayerRunnable, "MediaPlayerThreadInstance");

		m_ServiceInstance.m_MediaPlayerRunnable.OnCurrentTrackNameChanged().observeOn(AndroidSchedulers.mainThread()).subscribe(onCurrentTrackNameChanged);
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.e("com.amslayer.musicplayer", "Service - onBind");

		// return m_MusicBinder;
		return m_ServiceMessenger.getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.e("com.amslayer.musicplayer", "Service - onUnbind");

		// m_MediaPlayerRunnable.setThreadMessage(MC_TERMINATE);
		return false;
	}

	@Override
	public void onRebind(Intent intent) {
		Log.e("com.amslayer.musicplayer", "Service - onRebind");

		// TODO Auto-generated method stub
		super.onRebind(intent);
	}

	@Override
	public void onDestroy() {
		Log.e("com.amslayer.musicplayer", "Service - onDestroy");

		stopForeground(true);

		try {
			m_MediaPlayerThread.join();
		} catch (InterruptedException exp) {
			exp.printStackTrace();
		}
	}

	static class RemoteMessageHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

				case MC_RUN:
					if (m_ServiceInstance.m_MediaPlayerRunnable.isRunning() == false) {
						m_ServiceInstance.m_MediaPlayerThread.start();
					}

					if (m_ServiceInstance.m_ActivityMessanger == null) {
						m_ServiceInstance.m_ActivityMessanger = msg.replyTo;
					}

					break;

				case MC_TERMINATE:
					m_ServiceInstance.m_MediaPlayerRunnable.setThreadMessage(MC_TERMINATE);
					break;

				case MC_PLAY_TRACK:
					m_ServiceInstance.m_MediaPlayerRunnable.setThreadMessage(MC_PLAY_TRACK);
					break;

				case MC_PAUSE_TRACK:
					m_ServiceInstance.m_MediaPlayerRunnable.setThreadMessage(MC_PAUSE_TRACK);
					break;

				case MC_STOP_TRACK:
					m_ServiceInstance.m_MediaPlayerRunnable.setThreadMessage(MC_STOP_TRACK);
					break;

				case MC_PLAY_NEXT_TRACK:
					m_ServiceInstance.m_MediaPlayerRunnable.setThreadMessage(MC_PLAY_NEXT_TRACK);
					break;

				case MC_PLAY_PREVIOUS_TRACK:
					m_ServiceInstance.m_MediaPlayerRunnable.setThreadMessage(MC_PLAY_PREVIOUS_TRACK);
					break;

				default:
					super.handleMessage(msg);
					break;
			}
		}
	}

	// Creates the status bar notification
	public void CreateNotification(String trackName) {
		int iconID = getResources().getIdentifier("play", "drawable", getPackageName());

		Notification.Builder musicPlayerNotificationBuilder = new Notification.Builder(this);
		musicPlayerNotificationBuilder.setSmallIcon(iconID).setTicker(trackName).setOngoing(true).setContentTitle("Playing").setContentText(trackName);

		Intent musicPlayerNotificationIntent = new Intent(this, MusicPlayerActivity.class);
		musicPlayerNotificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		PendingIntent musicPlayerPendingIntent = PendingIntent.getActivity(m_ServiceInstance, 0, musicPlayerNotificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		musicPlayerNotificationBuilder.setContentIntent(musicPlayerPendingIntent);

		Notification musicPlayerNotification = musicPlayerNotificationBuilder.build();
		this.startForeground(MusicPlayerService.NOTIFY_ID, musicPlayerNotification);
	}

	private void SendCurrentTrackName(String trackName) {
		try {
			Message msg = Message.obtain(null, MC_TRACK_NAME_CHANGED, 0, 0);
			Bundle bMsg = new Bundle();
			bMsg.putString("CurrentTrackName", trackName);
			msg.setData(bMsg);

			if (m_ActivityMessanger != null) {
				m_ActivityMessanger.send(msg);
			}
		} catch (RemoteException exp) {
			exp.printStackTrace();
		}
	}

	private Observer<String>	onCurrentTrackNameChanged	= new Observer<String>() {

																@Override
																public void onNext(String trackName) {
																	CreateNotification(trackName);

																	SendCurrentTrackName(trackName);
																}

																@Override
																public void onError(Throwable exp) {
																}

																@Override
																public void onCompleted() {
																}
															};
}
