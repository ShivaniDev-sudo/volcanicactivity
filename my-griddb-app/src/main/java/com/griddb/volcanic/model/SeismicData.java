package com.griddb.volcanic.model;

import lombok.Data;

import java.time.Instant;

@Data
public class SeismicData {
    public Instant time;
    public double latitude;
    public double longitude;
    public double depth;
    public double mag;
    public String place;
}
