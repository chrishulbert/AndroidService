package au.com.splinter.mymedia;

import android.util.Log;

import org.json.JSONArray;

import java.io.IOException;
import java.io.StringWriter;
import java.net.MalformedURLException;

import fi.iki.elonen.NanoHTTPD;
import jcifs.smb.SmbFile;

/**
 * Created by chris on 27/11/2015.
 */
public class MyHTTPD extends NanoHTTPD {

    public MyHTTPD() throws IOException {
        super(30000);
        Log.d("MyHTTPD", "Created MyHTTPD");
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d("MyHTTPD", "Serve called...");

        String uri = session.getUri();
        if (uri.equals("/api/movies")) {
            try {
                SmbFile smbFile = new SmbFile("smb://admin:admin@10.1.1.1/usb1_1/");
                JSONArray list = new JSONArray();
                for (String file: smbFile.list()) {
                    list.put(file);
                }
                String json = list.toString(1);
                return newFixedLengthResponse(json);
            } catch (Exception e) {
                return newFixedLengthResponse("<html><body>Error: " + e.toString());
            }
        } else {

            // Browse.
            try {
                SmbFile smbFile = new SmbFile("smb://admin:admin@10.1.1.1/usb1_1" + uri);
                if (smbFile.isDirectory()) {
                    String html = "<html><body>";
                    // ListFiles seems to be really slow - does that matter once we're up and running?
                    for (SmbFile file : smbFile.listFiles()) {
                        html += "<p><a href='" + file.getName() + "'>" + file.getName() + "</a>";
                        if (file.isDirectory()) {
                            html += "<a href='" + file.getName() + "hls.m3u8'>HLS</a>";
                        }
                        html += "</p>";
                    }
                    return newFixedLengthResponse(html);

                } else if (smbFile.isFile()) {
                    String mime = "application/octet-stream";
                    String name = smbFile.getName();
                    if (name.endsWith(".m3u8")) {
                        mime = "application/x-mpegURL";
                    } else if (name.endsWith(".ts")) {
                        mime = "video/MP2T";
                    }
                    Log.d("MyMedia", "serving: " + name + "; mime: " + mime);
                    return newChunkedResponse(Response.Status.OK, mime, smbFile.getInputStream());
                } else {
                    return newFixedLengthResponse("<html><body>Not a file or dir, uri: " + uri);
                }
            } catch (Exception e) {
                return newFixedLengthResponse("<html><body>Error: " + e.toString());
            }

        }

    }
}
