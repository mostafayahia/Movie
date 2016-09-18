package com.android.example.movie;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by yahia on 9/10/16.
 */
public class MoreMovieInfo implements Parcelable {
    public final int runningTime;
    // NOTE: we only retrieve 2 trailers as max
    public final String trailer1;
    public final String trailer2;
    public final String reviews;

    public MoreMovieInfo(int runningTime, String trailer1, String trailer2, String reviews) {
        this.reviews = reviews;
        this.runningTime = runningTime;
        this.trailer1 = trailer1;
        this.trailer2 = trailer2;
    }

    protected MoreMovieInfo(Parcel in) {
        runningTime = in.readInt();
        trailer1 = in.readString();
        trailer2 = in.readString();
        reviews = in.readString();
    }

    public static final Creator<MoreMovieInfo> CREATOR = new Creator<MoreMovieInfo>() {
        @Override
        public MoreMovieInfo createFromParcel(Parcel in) {
            return new MoreMovieInfo(in);
        }

        @Override
        public MoreMovieInfo[] newArray(int size) {
            return new MoreMovieInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(runningTime);
        dest.writeString(trailer1);
        dest.writeString(trailer2);
        dest.writeString(reviews);
    }
}