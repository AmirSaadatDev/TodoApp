package common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;

/**
 * Single place that configures Gson for the whole app, so the network layer
 * and the storage layer never drift into inconsistent serialization rules.
 */
public final class GsonFactory {
    private GsonFactory() {}

    /** Compact JSON — required because the wire protocol is one JSON object per line. */
    public static Gson forNetwork() {
        return new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .create();
    }

    /** Pretty-printed JSON for the on-disk storage file (human-readable, safe to inspect). */
    public static Gson forStorage() {
        return new GsonBuilder()
                .registerTypeAdapter(Instant.class, new InstantTypeAdapter())
                .setPrettyPrinting()
                .create();
    }
}
