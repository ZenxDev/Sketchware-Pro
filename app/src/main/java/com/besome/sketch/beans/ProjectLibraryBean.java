package com.besome.sketch.beans;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class ProjectLibraryBean implements Parcelable {
    public static final Parcelable.Creator<ProjectLibraryBean> CREATOR = new Parcelable.Creator<ProjectLibraryBean>() {
        @Override
        public ProjectLibraryBean createFromParcel(Parcel parcel) {
            return new ProjectLibraryBean(parcel);
        }

        @Override
        public ProjectLibraryBean[] newArray(int i) {
            return new ProjectLibraryBean[i];
        }
    };
    public static final String LIB_USE_N = "N";
    public static final String LIB_USE_Y = "Y";
    public static final int PROJECT_LIB_TYPE_ADMOB = 2;
    public static final int PROJECT_LIB_TYPE_COMPAT = 1;
    public static final int PROJECT_LIB_TYPE_FIREBASE = 0;
    public static final int PROJECT_LIB_TYPE_GOOGLE_MAP = 3;
    @Expose
    public ArrayList<AdUnitBean> adUnits;
    @Expose
    public String data;
    @Expose
    public int libType;
    @Expose
    public String reserved1;
    @Expose
    public String reserved2;
    @Expose
    public String reserved3;
    @Expose
    public ArrayList<AdTestDeviceBean> testDevices;
    @Expose
    public String useYn;

    public ProjectLibraryBean(int i) {
        libType = i;
        useYn = LIB_USE_N;
        data = "";
        reserved1 = "";
        reserved2 = "";
        reserved3 = "";
        adUnits = new ArrayList<>();
        testDevices = new ArrayList<>();
    }

    public ProjectLibraryBean(Parcel parcel) {
        libType = parcel.readInt();
        useYn = parcel.readString();
        data = parcel.readString();
        reserved1 = parcel.readString();
        reserved2 = parcel.readString();
        reserved3 = parcel.readString();
        adUnits = new ArrayList<>();
        parcel.readTypedList(adUnits, AdUnitBean.getCreator());
        testDevices = new ArrayList<>();
        parcel.readTypedList(testDevices, AdTestDeviceBean.getCreator());
    }

    public static Parcelable.Creator<ProjectLibraryBean> getCreator() {
        return CREATOR;
    }

    public static int getLibraryIcon(int i) {
        switch (i) {
            case 0:
                return 2131166245;
            case 1:
                return 2131165505;
            case 2:
                return 2131166234;
            case 3:
                return 2131166247;
            default:
                return 0;
        }
    }

    public static int getLibraryResDesc(int i) {
        switch (i) {
            case 0:
                return 2131625204;
            case 1:
                return 2131625203;
            case 2:
                return 2131625202;
            case 3:
                return 2131625205;
            default:
                return 0;
        }
    }

    public static int getLibraryResName(int i) {
        switch (i) {
            case 0:
                return 2131625234;
            case 1:
                return 2131625251;
            case 2:
                return 2131625194;
            case 3:
                return 2131625241;
            default:
                return 0;

        }
    }

    public void copy(ProjectLibraryBean projectLibraryBean) {
        libType = projectLibraryBean.libType;
        useYn = projectLibraryBean.useYn;
        data = projectLibraryBean.data;
        reserved1 = projectLibraryBean.reserved1;
        reserved2 = projectLibraryBean.reserved2;
        reserved3 = projectLibraryBean.reserved3;
        adUnits = new ArrayList<>();
        for (AdUnitBean adUnitBean : projectLibraryBean.adUnits) {
            adUnits.add(adUnitBean.clone());
        }
        testDevices = new ArrayList<>();
        if (projectLibraryBean.testDevices != null) {
            for (AdTestDeviceBean adTestDeviceBean : projectLibraryBean.testDevices) {
                testDevices.add(adTestDeviceBean.clone());
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public boolean isEnabled() {
        return useYn != null && !useYn.isEmpty() && useYn.equals(LIB_USE_Y);
    }

    public void print() {
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(libType);
        parcel.writeString(useYn);
        parcel.writeString(data);
        parcel.writeString(reserved1);
        parcel.writeString(reserved2);
        parcel.writeString(reserved3);
        parcel.writeTypedList(adUnits);
        parcel.writeTypedList(testDevices);
    }

    @Override
    public ProjectLibraryBean clone() {
        ProjectLibraryBean projectLibraryBean = new ProjectLibraryBean(this.libType);
        projectLibraryBean.copy(this);
        return projectLibraryBean;
    }
}
