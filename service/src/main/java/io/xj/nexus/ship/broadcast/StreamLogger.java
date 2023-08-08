//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package io.xj.nexus.ship.broadcast;

import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public class StreamLogger implements Runnable {
    static final String THREAD_NAME = "ffmpeg";
    final Logger LOG;
    final InputStream _inputStream;
    final String name;

    public StreamLogger(Logger LOG, InputStream is, String name) {
        this.LOG = LOG;
        this._inputStream = is;
        this.name = name;
    }

    public static void close(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException var2) {
            var2.printStackTrace();
        }

    }

    public void run() {
        InputStreamReader isr = null;
        BufferedReader br = null;
        Thread currentThread = Thread.currentThread();
        String oldName = currentThread.getName();

        try {
            currentThread.setName("ffmpeg");
            isr = new InputStreamReader(this._inputStream);
            br = new BufferedReader(isr);

            String line;
            while((line = br.readLine()) != null) {
                this.LOG.debug("[{}] {}", this.name, line);
            }
        } catch (IOException var9) {
            this.LOG.error("[{}}] Failed to read from stream!", this.name, var9);
        } finally {
            this.LOG.debug("[{}}] Done reading stream", this.name);
            close(isr);
            close(br);
            currentThread.setName(oldName);
        }

    }
}
