package edu.ucsd.cse110.sharednotes.model;

import android.util.Log;

import androidx.annotation.AnyThread;
import androidx.annotation.MainThread;
import androidx.annotation.WorkerThread;

import com.google.gson.Gson;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NoteAPI {
    // TODO: Implement the API using OkHttp!
    // TODO: - getNote (maybe getNoteAsync)
    // TODO: - putNote (don't need putNotAsync, probably)
    // TODO: Read the docs: https://square.github.io/okhttp/
    // TODO: Read the docs: https://sharednotes.goto.ucsd.edu/docs

    private volatile static NoteAPI instance = null;

    private OkHttpClient client;
    private static final MediaType JSON = MediaType.get("application/json;charset=utf-8");

    private Gson gson ;

    public NoteAPI() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public static NoteAPI provide() {
        if (instance == null) {
            instance = new NoteAPI();
        }
        return instance;
    }

    /**
     * An example of sending a GET request to the server.
     *
     * The /echo/{msg} endpoint always just returns {"message": msg}.
     *
     * This method should can be called on a background thread (Android
     * disallows network requests on the main thread).
     */
    @WorkerThread
    public String echo(String msg) {
        // URLs cannot contain spaces, so we replace them with %20.
        String encodedMsg = msg.replace(" ", "%20");

        var request = new Request.Builder()
                .url("https://sharednotes.goto.ucsd.edu/echo/" + encodedMsg)
                .method("GET", null)
                .build();

        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var body = response.body().string();
            Log.i("ECHO", body);
            return body;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    @WorkerThread
    public Note getNote(String title){
        var request = new Request.Builder().url("https://sharednotes.goto.ucsd.edu/notes/" + title)
                .method("GET",null)
                .build();
        try (var response = client.newCall(request).execute()) {
            assert response.body() != null;
            var responseJson = response.body().string();
            return gson.fromJson(responseJson,Note.class);



        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @WorkerThread
    public boolean putNote(Note note) {

            // Serialize note object to JSON
            var noteJson = gson.toJson(note);
            var body = RequestBody.create(noteJson,JSON);
            String temp = note.title.replace(" ","%20");

            // Build request
            Request request = new Request.Builder()
                    .header("Content-type","application/json")
                    .url("https://sharednotes.goto.ucsd.edu/notes/" + temp)
                    .method("PUT",body)
                    .build();

            // Execute request
           try(var response = client.newCall(request).execute()){
               int code = response.code();
               String message = response.message();
               return response.isSuccessful();
           }catch (Exception e){
               e.printStackTrace();

               return false;
           }

            // Check response code


    }

    @AnyThread
    public Future<String> echoAsync(String msg) {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(() -> echo(msg));

        // We can use future.get(1, SECONDS) to wait for the result.
        return future;
    }
}
