package net.baruchans.hizen;

import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class JuliusClient {
    private static final String TAG = JuliusClient.class.getSimpleName();

    private String host;
    private int port;

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
            4,
            8,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<Runnable>()
    );

    public JuliusClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() {
        (new AsyncTask<Void, String, String>() {
            private String resultTagBegin = "<RECOGOUT>";
            private String resultTagEnd = "</RECOGOUT>";
            private String wordTag = "WHYPO";

            @Override
            protected String doInBackground(Void... params) {
                InputStream in;
                String response = "";
                Socket socket;

                try {
                    Log.i(TAG, "connect julius - port: " + port);

                    socket = new Socket(host, port);
                    in = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                    while (true) {
                        if (isCancelled()) {
                            break;
                        }

                        if (response.indexOf(resultTagEnd) == -1) {
                            response += reader.readLine();
                        } else {
                            XmlPullParser result = extractResultElement(response);

                            if (result != null) {
                                Float score = getScore(result);
                                if (score > 0.8) {
                                    String recognizedWord = getWord(result);

                                    Log.i(TAG, "score: " + score);
                                    Log.i(TAG, "voice: " + recognizedWord);

                                    HizenApplication.getApp().updateVoice(recognizedWord, port);
                                }

                            }

                            response =  "";
                        }

                        Thread.sleep(100);
                    }

                    in.close();

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }

                return "";
            }

            private XmlPullParser extractResultElement(String content) throws XmlPullParserException, IOException {
                XmlPullParser xml = toXml(content);

                int eventType = xml.getEventType();
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        String tag = xml.getName();
                        if (tag.equals(wordTag)) {
                            return xml;
                        }
                    }

                    eventType = xml.next();
                }

                return null;
            }

            private XmlPullParser toXml(String content) throws XmlPullParserException {
                XmlPullParser parser = Xml.newPullParser();

                int begin = content.indexOf(resultTagBegin);
                int end   = content.indexOf(resultTagEnd) + 11;

                parser.setInput(new StringReader(content.substring(begin, end)));

                return parser;
            }

            private float getScore(XmlPullParser element) {
                return Float.parseFloat(element.getAttributeValue(3));

            }

            private String getWord(XmlPullParser element) {
                return element.getAttributeValue(0);
            }

        }).executeOnExecutor(JuliusClient.threadPoolExecutor);
    }
}
