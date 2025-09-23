Setting up:
1. Create an S3 instance at AWS, manage CORS rules in order to access your bucket. Initially your JSON can look like this:
   
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

