# XJ services deployment on GCP

See Pivotal Tracker: [Deploy k8s cluster on GCP](https://www.pivotaltracker.com/story/show/180684409)

Our costs on AWS for production template fabrication are getting away from us. We need to explore more cost-effective means here.


### Getting Started

See GCP docs: [Installing Cloud SDK](https://cloud.google.com/sdk/docs/install#linux)

Note: be sure to install using the manual method (downloading the .tgz and running ./install.sh) in order to have
a version of `gcloud` which can install components, as in:

```shell
gcloud components install docker-credential-gcr
```

We're using the multi-region US container registry:

```shell
gcloud auth configure-docker us-docker.pkg.dev
```

Run the following command to retrieve the access credentials for your cluster and automatically configure kubectl.

```shell
gcloud container clusters get-credentials xj-prod-us-west-1 --region us-west1
```
