package com.amslayer.musicplayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import rx.Observable;
import rx.subjects.PublishSubject;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.PowerManager;
import android.util.Log;

class MediaPlayerRunnable implements Runnable, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

	private MediaPlayer				m_MediaPlayer;
	private int						m_CurrentTrackIndex;
	private String					m_CurrentTrackTitle	= "";
	private ArrayList<MusicTrack>	m_TrackList			= new ArrayList<MusicTrack>();
	private boolean					m_ShuffleTracks		= false;
	private Random					m_RandomTrack;
	private Context					m_ApplicationContext;
	private ThreadMessage			m_PlayerStateMessage;

	private boolean					m_isRunning			= false;

	public boolean isRunning() {
		return m_isRunning;
	}

	private PublishSubject<String>	onCurrentTrackNameChanged	= PublishSubject.create();

	public Observable<String> OnCurrentTrackNameChanged() {
		return onCurrentTrackNameChanged;
	}

	public void setThreadMessage(int msg) {
		m_PlayerStateMessage.setMessage(msg);
	}

	public MediaPlayerRunnable(Context applicationContext) {
		m_ApplicationContext = applicationContext;

		m_PlayerStateMessage = new ThreadMessage();
		setThreadMessage(MusicPlayerService.MC_RUN);

		m_CurrentTrackIndex = 0;
		m_RandomTrack = new Random();

		// Saves list of tracks to a local variable
		getTrackListFromDevice();

		// Create the instance of the media player and initializes callbacks
		InitializeMusicPlayer();
	}

	@Override
	public void run() {

		m_isRunning = true;

		while (m_PlayerStateMessage.getMessageNumeric() != MusicPlayerService.MC_TERMINATE) {

			if (m_PlayerStateMessage.isChanged()) {
				m_PlayerStateMessage.setChanged(false);

				switch (m_PlayerStateMessage.getMessageNumeric()) {
					case MusicPlayerService.MC_RUN:
						break;

					case MusicPlayerService.MC_TERMINATE:
						break;

					case MusicPlayerService.MC_PLAY_TRACK:
						Play();
						break;

					case MusicPlayerService.MC_PAUSE_TRACK:
						PausePlayer();
						break;

					case MusicPlayerService.MC_STOP_TRACK:
						StopPlayer();
						break;

					case MusicPlayerService.MC_PLAY_NEXT_TRACK:
						PlayNext();
						break;

					case MusicPlayerService.MC_PLAY_PREVIOUS_TRACK:
						PlayPrevious();
						break;

					default:
						break;
				}
			}
		}

		TerminateMediaPlayer();

		m_isRunning = false;

		Log.e("com.amslayer.musicplayer", "Runnable - Exiting run()");
	}

	// Initializes the Music Player
	private void InitializeMusicPlayer() {
		m_MediaPlayer = new MediaPlayer();

		// set player properties
		m_MediaPlayer.setWakeMode(m_ApplicationContext, PowerManager.PARTIAL_WAKE_LOCK);
		m_MediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

		m_MediaPlayer.setOnPreparedListener(this);
		m_MediaPlayer.setOnCompletionListener(this);
		m_MediaPlayer.setOnErrorListener(this);
	}

	private void TerminateMediaPlayer() {
		m_MediaPlayer.stop();
		m_MediaPlayer.release();
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {
		if (m_MediaPlayer.getCurrentPosition() > 0) {
			mediaPlayer.reset();
			PlayNext();
		}
	}

	@Override
	public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
		mediaPlayer.reset();
		return false;
	}

	@Override
	public void onPrepared(MediaPlayer mediaPlayer) {

		// start playback
		mediaPlayer.start();

		// Rx pattern
		onCurrentTrackNameChanged.onNext(m_CurrentTrackTitle);
	}

	// Plays the current track
	private void PlayTrack() {
		// play a track
		m_MediaPlayer.reset();
		// get track
		MusicTrack playTrack = m_TrackList.get(m_CurrentTrackIndex);
		m_CurrentTrackTitle = playTrack.getTitle();
		// get id
		long currTrack = playTrack.getID();
		// set uri
		Uri trackUri = ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, currTrack);

		try {
			m_MediaPlayer.setDataSource(m_ApplicationContext, trackUri);
		} catch (Exception e) {
			Log.e("MUSIC SERVICE", "Error setting data source", e);
		}

		m_MediaPlayer.prepareAsync();
	}

	/*
	// Changes the current track index
	private void SetTrack(int trackIndex) {
		m_CurrentTrackIndex = trackIndex;
	}

	// Sets the track list to be palyed
	private void SetList(ArrayList<MusicTrack> theTracks) {
		m_TrackList = theTracks;
	}

	// Returns the current position of the track being played
	private int getPosition() {
		return m_MediaPlayer.getCurrentPosition();
	}

	// Gets the duration of current track being played
	private int getDuration() {
		return m_MediaPlayer.getDuration();
	}

	// Returns whether the track is playing
	private boolean isPlaying() {
		return m_MediaPlayer.isPlaying();
	}
	
		// Moves to "posn" in milli-seconds of the current track
	private void SeekTo(int posn) {
		m_MediaPlayer.seekTo(posn);
	}
	
	// Sets whether to shuffle the tracks
	private void SetShuffle() {
		if (m_ShuffleTracks)
			m_ShuffleTracks = false;
		else
			m_ShuffleTracks = true;
	}
	*/

	// Pauses the media player
	private void PausePlayer() {
		m_MediaPlayer.pause();
	}

	// Stops the media player
	private void StopPlayer() {
		m_MediaPlayer.stop();
	}

	// Start or Resume the playback
	private void Play() {
		m_MediaPlayer.start();
	}

	// Move to previous track
	private void PlayPrevious() {

		m_CurrentTrackIndex--;
		if (m_CurrentTrackIndex < 0)
			m_CurrentTrackIndex = m_TrackList.size() - 1;

		PlayTrack();
	}

	// Move to next track
	private void PlayNext() {

		if (m_ShuffleTracks) {
			int newTrack = m_CurrentTrackIndex;
			while (newTrack == m_CurrentTrackIndex) {
				newTrack = m_RandomTrack.nextInt(m_TrackList.size());
			}
			m_CurrentTrackIndex = newTrack;
		} else {
			m_CurrentTrackIndex++;
			if (m_CurrentTrackIndex >= m_TrackList.size())
				m_CurrentTrackIndex = 0;
		}

		PlayTrack();
	}

	// Retrieves the list of playable music tracks on device
	private void getTrackListFromDevice() {
		// Retrieve track info
		ContentResolver musicResolver = m_ApplicationContext.getContentResolver();
		Uri musicUri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
		Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

		if (musicCursor != null && musicCursor.moveToFirst()) {
			// Get columns
			int titleColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.TITLE);
			int idColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media._ID);
			int artistColumn = musicCursor.getColumnIndex(android.provider.MediaStore.Audio.Media.ARTIST);

			// Add tracks to list
			do {
				long thisId = musicCursor.getLong(idColumn);
				String thisTitle = musicCursor.getString(titleColumn);
				String thisArtist = musicCursor.getString(artistColumn);
				m_TrackList.add(new MusicTrack(thisId, thisTitle, thisArtist));
			} while (musicCursor.moveToNext());
		}

		// Alphabetical sort
		Collections.sort(m_TrackList, new Comparator<MusicTrack>() {
			public int compare(MusicTrack a, MusicTrack b) {
				return a.getTitle().compareTo(b.getTitle());
			}
		});
	}
}
