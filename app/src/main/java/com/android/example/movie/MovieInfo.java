package com.android.example.movie;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yahia on 8/13/16.
 */
public class MovieInfo implements Parcelable {

    private String originalTitle;
    private String posterUrl;
    private String overview;
    private double voteAverage;
    private String releaseDate;
    private int id;

    private MovieInfo(Parcel in) {
        id = in.readInt();
        originalTitle = in.readString();
        posterUrl = in.readString();
        overview = in.readString();
        voteAverage = in.readDouble();
        releaseDate = in.readString();
    }

    public MovieInfo(int id, String originalTitle, String posterUrl, String overview,
                     double voteAverage, String releaseDate) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.posterUrl = posterUrl;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
    }

    public static final Creator<MovieInfo> CREATOR = new Creator<MovieInfo>() {
        @Override
        public MovieInfo createFromParcel(Parcel parcel) {
            return new MovieInfo(parcel);
        }

        @Override
        public MovieInfo[] newArray(int size) {
            return new MovieInfo[size];
        }
    };

    /**
     * we don't use this method so we return arbitrary value
     * @return arbitrary integer
     */
    @Override
    public int describeContents() {
        return 0;
    }


    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(id);
        parcel.writeString(originalTitle);
        parcel.writeString(posterUrl);
        parcel.writeString(overview);
        parcel.writeDouble(voteAverage);
        parcel.writeString(releaseDate);
    }

    // getter methods to get members' values of this object
    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getOverview() {
        return overview;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public int getId() {return id;}

}
