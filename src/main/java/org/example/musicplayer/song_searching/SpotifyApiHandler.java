package org.example.musicplayer.song_searching;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;

import com.google.gson.*;

public class SpotifyApiHandler {

    private static final String CLIENT_ID = "a861ad43a377467b9417c975c1b8332b"; // Your Spotify API client ID
    private static final String CLIENT_SECRET = "65fa32ed7e2f4527ae20a6126a142746"; // Your Spotify API client secret

    private final String accessToken;

    private String foundSongName;
    private String foundArtistName;
    private String albumName;
    private String albumCoverUrl;

    private JsonArray items;

    private JsonObject firstTrack;

    public SpotifyApiHandler(String accessToken) {
        this.accessToken = accessToken;
    }

    public static String getAccessToken() {
        try {
            String endpoint = "https://accounts.spotify.com/api/token";
            String grantType = "client_credentials";
            String requestBody = "grant_type=" + grantType;

            String clientIdSecret = CLIENT_ID + ":" + CLIENT_SECRET;
            String accessToken = java.util.Base64.getEncoder().encodeToString(clientIdSecret.getBytes());

            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Basic " + accessToken);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);

            connection.getOutputStream().write(requestBody.getBytes());

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = reader.readLine();
                reader.close();
                return response.split("\"")[3];
            } else {
                System.out.println("HTTP request failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void getTrackInfo(String accessToken, String songName, String artistName) {
        try {

            String trackName = removeMp3Extension(songName);
            String formattedTrackName = removeSpaces(trackName);
            String formattedArtistName = removeSpaces(artistName);

            String endpoint = "https://api.spotify.com/v1/search";
            String query = "q=" + formattedTrackName + "%20" + formattedArtistName + "&type=track&limit=1";
            String url = endpoint + "?" + query;

            System.out.println("URL: " + url);

            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", "Bearer " + accessToken);

            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                System.out.println("Complete JSON response: " + response);


                JsonParser parser = new JsonParser();
                JsonObject jsonResponse = parser.parse(response.toString()).getAsJsonObject();
                System.out.println("Track Info JSON: " + jsonResponse);

                // Parse the JSON response to get the track information

                //JsonObject jsonResponse = parser.parse(response.toString()).getAsJsonObject();
                JsonObject tracks = jsonResponse.getAsJsonObject("tracks");
                JsonArray items = tracks.getAsJsonArray("items");

                if(!items.isJsonNull() && !items.isEmpty()) {
                    JsonObject firstTrack = items.get(0).getAsJsonObject();

                    String foundSongName = firstTrack.get("name").getAsString();
                    String foundArtistName = firstTrack.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString();
                    String albumName = firstTrack.getAsJsonObject("album").get("name").getAsString();
                    albumCoverUrl = firstTrack.getAsJsonObject("album").getAsJsonArray("images").get(0).getAsJsonObject().get("url").getAsString();
                    int durationMs = firstTrack.get("duration_ms").getAsInt();

                    // Example: Display the information in the console
                    System.out.println("Song Name: " + songName);
                    System.out.println("Artist Name: " + foundArtistName);
                    System.out.println("Album Name: " + albumName);
                    System.out.println("Album Cover URL: " + albumCoverUrl);
                    System.out.println("Duration (ms): " + durationMs);
                } else {
                    System.out.println("No track found");
                }

                // Perform other actions to display this information in your JavaFX application
            } else {
                System.out.println("HTTP request failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String removeMp3Extension(String songName) {
        if (songName.endsWith(".mp3")) {
            return songName.substring(0, songName.length() - 4);
        }
        else {
            return songName;
        }
    }





    private String removeSpaces(String input) {
        return input.replaceAll(" ", "%20");
    }

    public String getFoundSongName() {
        return foundSongName;
    }

    public String getFoundArtistName(String songName) {
        if (this.items != null && !this.items.isJsonNull() && !this.items.isEmpty()) {
            for (JsonElement track : this.items) {
                JsonObject trackObj = track.getAsJsonObject();
                if (trackObj.get("name").getAsString().equals(songName)) {
                    return trackObj.getAsJsonArray("artists").get(0).getAsJsonObject().get("name").getAsString();
                }
            }
        }
        return null;
    }



    public String getAlbumName(String songName) {
        if (this.items != null && !this.items.isJsonNull() && !this.items.isEmpty()) {
            for (JsonElement track : this.items) {
                JsonObject trackObj = track.getAsJsonObject();
                if (trackObj.get("name").getAsString().equals(songName)) {
                    return trackObj.getAsJsonObject("album").get("name").getAsString();
                }
            }
        }
        return null;
    }

    public String getAlbumCoverUrl() {
        System.out.println("Obtained album cover url: " + albumCoverUrl);
        return albumCoverUrl;
    }

    public Duration getDuration(String songName) {
        if (this.items != null && !this.items.isJsonNull() && !this.items.isEmpty()) {
            for (JsonElement track : this.items) {
                JsonObject trackObj = track.getAsJsonObject();
                if (trackObj.get("name").getAsString().equals(songName)) {
                    return Duration.ofMillis(trackObj.get("duration_ms").getAsLong());
                }
            }
        }
        return Duration.ZERO;
    }

    public JsonArray getItems() {
        return items;
    }

    public JsonObject getFirstTrack() {
        return firstTrack;
    }

}
