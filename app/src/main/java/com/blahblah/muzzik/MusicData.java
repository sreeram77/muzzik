package com.blahblah.muzzik;

public class MusicData {

    public long getSongId() {
        return songId;
    }

    public void setSongId(long songId) {
        this.songId = songId;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongArtist() {
        return songArtist;
    }

    public void setSongArtist(String songArtist) {
        this.songArtist = songArtist;
    }

    private long songId;
    private String songTitle;
    private String songArtist;

    public MusicData(long songId, String songTitle, String songArtist) {
        this.songId = songId;
        this.songTitle = songTitle;
        this.songArtist = songArtist;
    }
}
