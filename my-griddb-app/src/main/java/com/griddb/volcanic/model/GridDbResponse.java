package com.griddb.volcanic.model;

import java.util.List;
import java.util.Map;

public class GridDbResponse {
    public List<Map<String, String>> columns;
    public List<List<Object>> rows;
    public int offset;
    public int limit;
    public int total;
}
