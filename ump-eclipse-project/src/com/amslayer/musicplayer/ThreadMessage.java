package com.amslayer.musicplayer;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.util.Log;

public class ThreadMessage {
	private ReentrantReadWriteLock	m_Lock				= new ReentrantReadWriteLock(true);
	private int						m_Message_Numeric	= MusicPlayerService.MC_TERMINATE;
	// private String m_Message_String = "";
	private boolean					m_Changed			= false;

	public ThreadMessage() {
	}

	public boolean isChanged() {
		return m_Changed;
	}

	public void setChanged(boolean flag) {
		try {
			m_Lock.writeLock().lock();
		} catch (Exception exp) {
			exp.printStackTrace();
		}

		m_Changed = flag;
		m_Lock.writeLock().unlock();
	}

	public int getMessageNumeric() {
		return m_Message_Numeric;
	}

	public void setMessage(int msg) {
		try {
			m_Lock.writeLock().lock();
		} catch (Exception exp) {
			exp.printStackTrace();
		}

		m_Message_Numeric = msg;
		m_Changed = true;

		Log.e("com.amslayer.musicplayer", "Set Thread Message: " + msg);

		m_Lock.writeLock().unlock();
	}
	/*
	public String getMessageString() {
		return m_Message_String;
	}

	public void setMessage(String msg) {
		try {
			m_Lock.writeLock().lock();
		} catch (Exception exp) {
			exp.printStackTrace();
		}

		m_Message_String = msg;
		m_Changed = true;
		m_Lock.writeLock().unlock();
	}
	*/
}
