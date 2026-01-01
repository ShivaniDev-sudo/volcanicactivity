package com.griddb.volcanic.model;

import lombok.Data;

import java.time.Instant;

@Data
public class VolcanoData {
    public Instant timestamp;
    public String name;
    public String alertLevel;
    public String colorCode;
    public double latitude;
    public double longitude;
}
