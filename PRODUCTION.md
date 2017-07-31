# XJ Production Deployment

Here is the master checklist:

  1. Ensure you are not running any other build processes in the `xj` project, such as:
    * `ember`, e.g. `ember build --watch`
    * `mvn`
    * `docker-compose`
  
  2. `bin/release` the platform. This will automatically:
    * Git check for no uncommitted changes.
    * Maven compilation, unit test, integration test (verify), and production release of back-end targets.
    * Ember compilation, test, and production release of front-end targets.

  3. Upload and deploy the new `target/xj-release-*.zip` to AWS Elastic Beanstalk. PLEASE BE ADVISED:
    * If the release includes migrations, be sure to pick a single machine to run the **Hub** deployment on first, and confirm OK before proceeding with the full deployment.
    * The **Hub** app runs migrations, whereas the **Worker** app only validates that it is operating on a migrated database!
    
  4. Login to the production application at https://xj.io as an admin user. Confirm the ability to:
    * View Ideas and Instruments in a Library.
    * Add a new Audio (in an Instrument) and successfully upload the .wav file.
    * Create a new Preview and Production Chain bound to a Library, Idea or Instrument.
    
     
