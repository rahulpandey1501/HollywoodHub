package com.rahul.hollywoodhub;

import java.util.ArrayList;

/**
 * Created by finomena on 10/11/16.
 */

public class DownloadModel {
    ArrayList<DownloadPlaylist> playlist;

    public class DownloadPlaylist {
        ArrayList<DownloadInfo> sources;
        ArrayList<Subtitle> tracks;

        public class DownloadInfo {
            String file;
            String type;
            String label;

            public String getFile() {
                return file;
            }

            public String getType() {
                return type;
            }

            public String getLabel() {
                return label;
            }
        }

        public class Subtitle {
            String file;
            String kind;
            String label;

            public String getFile() {
                return file;
            }

            public String getKind() {
                return kind;
            }

            public String getLabel() {
                return label;
            }
        }

        public ArrayList<DownloadInfo> getSources() {
            return sources;
        }

        public ArrayList<Subtitle> getTracks() {
            return tracks;
        }
    }

    public ArrayList<DownloadPlaylist> getPlaylist() {
        return playlist;
    }
}
