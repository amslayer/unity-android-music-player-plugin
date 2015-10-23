package com.amslayer.musicplayer;

public class MusicTrack {
	private long	id;
	private String	title;
	private String	artist;

	public MusicTrack(long trackID, String trackTitle, String trackArtist) {
		id = trackID;
		title = trackTitle;
		artist = trackArtist;
	}

	public long getID() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getArtist() {
		return artist;
	}
}
