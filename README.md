**Project**:
Video Processing Application




**Description**:

Core Features:
1. HLS Transcoding: Converts source videos into multiple resolutions (240p, 360p, etc);
2. Asynchronous Workflow: Uses AWS SQS to trigger processing jobs when a file is uploaded;
3. HLS Master Playlist: Generates a .m3u8 file that links video, audio, and subtitles;

How it Works (Pipeline):
1. Upload: User uploads a file via front-end by presigned url into S3 bucket under temp/uploads/;
2. Notification: S3 triggers an event to AWS SQS;
3. Processing: The Worker module picks up the message, runs FFmpeg for transcoding, and uploads streamable HLS segments back to S3;
4. Metadata: Final file paths and track info are stored in PostgreSQL;



**Components:**

1. videoapp-web(Boot module) — REST API
2. videoapp-worker(Boot module) — video processing (FFmpeg)
3. PostgreSQL — video meta
4. S3 — storage
5. SQS — processing trigger




**Setting up**:
1. Create an S3 instance at AWS. Define an SQS queue and set an event-notification to the S3 with type 'Multipart upload completed', destination type 'SQS queue' destination as your queue and filter 'temp/uploads/'.

Manage CORS rules in order to access your bucket. Initially your JSON can look like this:

[
       {
           "AllowedHeaders": [
                "*"
           ],
           "AllowedMethods": [
                "GET",
                "PUT",
                "POST",
                "DELETE",
                "HEAD"
           ],
           "AllowedOrigins": [
                "*"
           ],
           "ExposeHeaders": [
                "ETag"
           ],
           "MaxAgeSeconds": 3000
       }
   ]
   
2. Create a file named .env for sensitive environment variables and fill it with following properties:

    WEB_SERVER_PORT=
    AWS_S3_BUCKET=
    AWS_ACCESS_KEY_ID=
    AWS_SQS_QUEUE_NAME=
    AWS_SECRET_ACCESS_KEY=
    AWS_REGION= 
    IS_S3_MODE_ENABLED= 
    DB_NAME= 
    DB_HOST=
    DB_PORT=
    DB_USER=
    DB_PASSWORD=

3. Set up the Docker
4. mvn clean install -> docker compose build -> docker compose up

http://localhost:5433/ - local access to the DB

