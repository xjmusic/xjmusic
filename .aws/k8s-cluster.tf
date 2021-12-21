// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

data "aws_eks_cluster" "xj-prod" {
  name = module.xj-prod-eks.cluster_id
}

data "aws_eks_cluster_auth" "xj-prod" {
  name = module.xj-prod-eks.cluster_id
}

provider "kubernetes" {
  host                   = data.aws_eks_cluster.xj-prod.endpoint
  cluster_ca_certificate = base64decode(data.aws_eks_cluster.xj-prod.certificate_authority[0].data)
  token                  = data.aws_eks_cluster_auth.xj-prod.token
}

# https://registry.terraform.io/modules/terraform-aws-modules/eks/aws/latest
module "xj-prod-eks" {
  source          = "terraform-aws-modules/eks/aws"
  cluster_name    = local.xj-prod-cluster-name
  cluster_version = "1.20"
  subnets         = module.xj-prod-vpc.private_subnets

  tags = {
    Environment = "training"
    GithubRepo  = "terraform-aws-eks"
    GithubOrg   = "terraform-aws-modules"
  }

  vpc_id = module.xj-prod-vpc.vpc_id

  workers_group_defaults = {
    root_volume_type  = "gp2"
    health_check_type = "ELB"
  }

  map_users = [
    {
      userarn  = aws_iam_user.xj-ci.arn
      username = aws_iam_user.xj-ci.name
      groups = [
        "system:masters"
      ]
    }
  ]

  worker_groups = [
    {
      name          = "general"
      instance_type = "m5a.xlarge"
      additional_security_group_ids = [
        aws_security_group.xj-prod-worker-mgmt.id,
      ]
      asg_desired_capacity = 1
    },
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/security_group_rule
resource "aws_security_group_rule" "xj-prod-eks-efs" {
  type              = "ingress"
  from_port         = 2049
  to_port           = 2049
  protocol          = "tcp"
  cidr_blocks       = module.xj-prod-vpc.private_subnets_cidr_blocks
  ipv6_cidr_blocks  = []
  security_group_id = module.xj-prod-vpc.default_security_group_id
}

# https://registry.terraform.io/providers/hashicorp/kubernetes/latest/docs/resources/storage_class
resource "kubernetes_storage_class" "efs-sc" {
  metadata {
    name = "efs-sc"
  }
  storage_provisioner = "efs.csi.aws.com"
  reclaim_policy      = "Retain"
  parameters = {
    type = "standard"
  }
  mount_options = [
    "file_mode=0700",
    "dir_mode=0777",
    "mfsymlinks",
    "uid=1000",
    "gid=1000",
    "nobrl",
    "cache=none",
  ]
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy" "xj-eks-SecretsManager" {
  role = module.xj-prod-eks.worker_iam_role_name
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Sid    = "GetSecretValue",
        Effect = "Allow",
        Action = [
          "secretsmanager:GetSecretValue",
        ],
        Resource = "*"
      },
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/secretsmanager_secret
resource "aws_secretsmanager_secret" "xj-prod-env" {
  name        = "xj-prod-env"
  description = "Name of AWS secret comprising environment KEY=VALUE lines, for production"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/secretsmanager_secret
resource "aws_secretsmanager_secret" "xj-dev-env" {
  name        = "xj-dev-env"
  description = "Name of AWS secret comprising environment KEY=VALUE lines, for development"
}
