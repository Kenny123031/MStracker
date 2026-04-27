package com.example.mstracker.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CreditsResponse {
    @SerializedName("crew")
    private List<Crew> crew;

    public static class Crew {
        @SerializedName("name")
        private String name;
        @SerializedName("job")
        private String job;

        public String getName() { return name; }
        public String getJob() { return job; }
    }

    public List<Crew> getCrew() { return crew; }
}
