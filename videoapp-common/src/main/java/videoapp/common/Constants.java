package videoapp.common;

public class Constants {

    // http parameters
    public static final String APPLICATION_X_MPEGURL = "application/x-mpegURL";

    // file extensions
    public static final String TMP_EXTENSION = ".tmp";
    public static final String M4A_EXTENSION = ".m4a";
    public static final String MP3_EXTENSION = ".mp3";
    public static final String M3U8_EXTENSION = ".m3u8";

    // main file names
    public static final String PLAYLIST_FILENAME = "playlist";
    public static final String MASTER_FILENAME = "master";


    // metadata parameters
    public static final String PUBLIC_ID = "publicId";
    public static final String FILE_NAME = "fileName";
    public static final String ORIGIN_VIDEO_KEY = "originVideoKey";
    public static final String TARGET_RESOLUTIONS = "targetResolutions";
    public static final String MUTED = "muted";
    public static final String AI_SUBS = "aiSubs";
    public static final String LABEL = "label";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String BITRATE = "bitrate";
    public static final String ORIGINAL = "original";


    // quality labels
    public static final String QUALITY_LABEL = "%sp";
    public static final String QUALITY_1080P = "1080p";
    public static final String QUALITY_720P = "720p";
    public static final String QUALITY_480P = "480p";
    public static final String QUALITY_360P = "360p";
    public static final String QUALITY_240P = "240p";


    public static final String BASIC_UPLOAD_KEY_REGEXP = "^temp/uploads/([^/]+)/([^/]+)\\.[^.]+$";}
