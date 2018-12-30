package org.schabi.newpipe.player.helper;

import android.os.Environment;

import com.github.se_bastiaan.torrentstream.StreamStatus;
import com.github.se_bastiaan.torrentstream.Torrent;
import com.github.se_bastiaan.torrentstream.TorrentOptions;
import com.github.se_bastiaan.torrentstreamserver.TorrentServerListener;
import com.github.se_bastiaan.torrentstreamserver.TorrentStreamNotInitializedException;
import com.github.se_bastiaan.torrentstreamserver.TorrentStreamServer;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class TorrentHelper {

    private static final String HOST_IP_ADDRESS = "127.0.0.1";
    private static final int HOST_PORT = 8080;
    private static final String TORRENTS_SAVE_DIR = "NewPipeTorrents";

    // i am doubtful
    private static final AtomicBoolean done = new AtomicBoolean(false);
    private static final AtomicReference<String> videoUrl = new AtomicReference<>();


    private static TorrentStreamServer torrentStreamServer;

    synchronized public static TorrentStreamServer initTorrentStreamServer() {
        if (null == torrentStreamServer) {
            torrentStreamServer = TorrentStreamServer.getInstance();
            TorrentOptions torrentOptions = new TorrentOptions.Builder()
                    .saveLocation(getLocation())
                    .removeFilesAfterStop(true)
                    .build();
            torrentStreamServer.setTorrentOptions(torrentOptions);
            torrentStreamServer.setServerHost(HOST_IP_ADDRESS);
            torrentStreamServer.setServerPort(HOST_PORT);
            torrentStreamServer.addListener(torrentServerListener);
            torrentStreamServer.startTorrentStream();
        }
        return torrentStreamServer;
    }

    private static File getLocation() {
        File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), TORRENTS_SAVE_DIR);
        if (!f.exists()) {
            f.mkdirs();
        }
        return f;
    }

    public static String startStream(String torrentUrl) throws IOException, TorrentStreamNotInitializedException, InterruptedException {
        initTorrentStreamServer();
        synchronized (videoUrl) {
            if (torrentStreamServer.isStreaming()) {
                torrentStreamServer.stopStream();
            }
            done.set(false);
            torrentStreamServer.startStream(torrentUrl);
            synchronized (done){
                while(!done.get()){
                    done.wait();
                }
            }
            return videoUrl.get();
        }
    }

    public static void stopStream() {
        if (null != torrentStreamServer) torrentStreamServer.stopStream();
    }

    public static void destroy() {
        if (null != torrentStreamServer) {
            torrentStreamServer.stopStream();
            torrentStreamServer.stopTorrentStream();
        }
    }


    private static TorrentServerListener torrentServerListener = new TorrentServerListener() {

        @Override
        public void onServerReady(String url) {
            synchronized (done) {
                videoUrl.set(url);
                done.set(true);
                done.notifyAll();
            }
        }

        @Override
        public void onStreamPrepared(Torrent torrent) {

        }

        @Override
        public void onStreamStarted(Torrent torrent) {

        }

        @Override
        public void onStreamError(Torrent torrent, Exception e) {

        }

        @Override
        public void onStreamReady(Torrent torrent) {

        }

        @Override
        public void onStreamProgress(Torrent torrent, StreamStatus status) {

        }

        @Override
        public void onStreamStopped() {

        }
    };

}
