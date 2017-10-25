package net.baruchans.hizen;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class SensorClient {
    private String host;
    private int port;

    public SensorClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect(final HizenApplication hizenApplication) {
        AsyncTask<HizenApplication,String,String> task = new AsyncTask<HizenApplication, String, String>() {
            private Socket client;
            private InputStream in;

            @Override
            protected String doInBackground(HizenApplication... params) {
                BufferedReader reader;

                try {
                    client = new Socket(host, port);
                    in = client.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(in));
                    String response = "";

                    while (true) {
                        if (isCancelled()) {
                            break;
                        }

                        response = reader.readLine();

                        if (response == null) {
                            response = "";
                        }

                        if (response.equals("")) {
                            // do nothing
                        } else {
                            // response format: <pressure>@<ch>
                            String[] responses = response.split("@");
                            String state = responses[0];
                            int channel    = Integer.parseInt(responses[1]);
                            float pressure = Float.parseFloat(responses[2]);

                            //Log.i(this.getClass().getName(), "pressure: " + pressure + ", channel" + channel);

                            if (state.equals("in")) {
                                hizenApplication.updatePressure(channel, state, pressure);
                            }
                        }

                        response = "";
                        Thread.sleep(100);
                    }

                    in.close();
                    client.close();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                }

                return "";
            }
        };

        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, hizenApplication);
    }

}
