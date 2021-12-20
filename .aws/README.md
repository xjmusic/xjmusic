[![Apply](https://github.com/xjmusic/terraform/actions/workflows/apply.yml/badge.svg?branch=main)](https://github.com/xjmusic/terraform/actions/workflows/apply.yml)

# XJ service deployment on AWS

## Terraform

**the XJ Music Inc computing cluster on Amazon Web Services**

## Kubernetes

Configured the EKS cluster according to [this hashicorp tutorial](https://learn.hashicorp.com/tutorials/terraform/eks)

After following all those instructions, first proxy from the cluster:

```shell
kubectl proxy
```

Then, to go through the steps of updating the dashboard kube internal, getting a token and dashboard URL:

```shell
bin/kube/dashboard.sh
```

### Configure kubectl

Run the following command to retrieve the access credentials for your cluster and automatically configure kubectl.

```shell
aws eks --region us-east-1 update-kubeconfig --name xj-prod-6VxY2MG3
```

The Kubernetes cluster name and region correspond to the output variables showed after the successful Terraform run.

### EBS as persistent volume for Kubernetes

Configured EKS with EBS according
to [this AWS tutorial](https://aws.amazon.com/premiumsupport/knowledge-center/eks-persistent-storage/)

### AWS Kubernetes drivers

See also https://github.com/aws/amazon-vpc-cni-k8s

In order to expose service with load balancers:
https://kubernetes-sigs.github.io/aws-load-balancer-controller/v2.2/deploy/installation/

