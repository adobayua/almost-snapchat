# Almost Snapchat

A snapchat-like application made using AWS S3 as a backend. This was made as a learning-experience to learn 
how Amazon web services work. 

## Usage

1. Create a identity pool
   * Go to https://console.aws.amazon.com/cognito/ and create a new identity pool. Make sure to enable access to unauthenticated identities and use the default roles.
   * Download the starter code at the last step of the wizard.
   * The starter code, has your Identity Pool ID. Keep this, you will need to add it to the sample later.

2. Set up permissions
   * Go to https://console.aws.amazon.com/iam/ and select "Roles".
   * Select the unauthenticated role you just created in step 1.
   * Select "Attach Policy", then find "AmazonS3FullAccess" and attach it it to the role.
   * Note:  This will grant users in the identity pool full access to all buckets and operations in S3.  In a real app, you should restrict users to only have access to the resources they need.
   
3. Create a bucket
   * Go to https://console.aws.amazon.com/s3/home
   * Create a bucket with a name you want.
   
4. Import the sample project
   * Import the sample as Android project into Android Studio.
   * Open com.ratik.alomostsnapchat.util.Constants.java.
   * Update "MY_POOL_ID" with the value you got from step 1.
   * Update "MY_BUCKET_ID" with the value in step 3;
   
5. Run the app!

## License
MIT
