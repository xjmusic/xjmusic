# XJ services deployment on GCP

See Pivotal Tracker: [Deploy k8s cluster on GCP](https://www.pivotaltracker.com/story/show/180684409)

Our costs on AWS for production template fabrication are getting away from us. We need to explore more cost-effective means here.


### Authentication Scopes

Since we are not using Workload Identity, the containers are running on GCP using the authentication scopes of the nodes.

Therefore, if you see an error like this coming from a container:

```
PERMISSION_DENIED: Request had insufficient authentication scopes.
```

The real culprit is the node pool, specifically its lack of authentication scopes.  When a node pool is created, it must have the following authentication scopes:

![Node Pool needs to have these GCP authentication scopes](README-authentication-scopes.png)


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
