# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_user_policy
resource "aws_iam_user_policy" "xj-infra-terraform" {
  name = "xj-infra-terraform"
  user = aws_iam_user.xj-ci.name

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Sid    = "TerraformPlanReadOnly",
        Effect = "Allow",
        Action = [
          "acm:Describe*",
          "acm:Get*",
          "acm:List*",
          "autoscaling:Describe*",
          "cloudfront:Get*",
          "cloudfront:List*",
          "ec2:Describe*",
          "ec2:Get*",
          "ecr:Describe*",
          "ecr:List*",
          "eks:*",
          "elasticfilesystem:Describe*",
          "elasticfilesystem:Get*",
          "elasticfilesystem:List*",
          "iam:Get*",
          "iam:List*",
          "lambda:Describe*",
          "lambda:Get*",
          "lambda:List*",
          "route53:Get*",
          "route53:List*",
          "s3:Describe*",
          "s3:Get*",
          "s3:List*",
          "secretsmanager:Describe*",
          "secretsmanager:GetResourcePolicy*",
        ],
        Resource = "*"
      },
      {
        Sid      = "TerraformStateList",
        Effect   = "Allow",
        Action   = "s3:ListBucket",
        Resource = "arn:aws:s3:::xj-terraform-state"
      },
      {
        Sid    = "TerraformStateModify",
        Effect = "Allow",
        Action = [
          "s3:PutObject",
          "s3:GetObject"
        ],
        Resource = "arn:aws:s3:::xj-terraform-state/*"
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_user_policy_attachment" "xj-infra-terraform-AmazonEKSClusterPolicy" {
  user       = aws_iam_user.xj-ci.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_user_policy_attachment" "xj-infra-terraform-AmazonEKSWorkerNodePolicy" {
  user       = aws_iam_user.xj-ci.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_user_policy_attachment" "xj-infra-terraform-AmazonEC2ContainerRegistryReadOnly" {
  user       = aws_iam_user.xj-ci.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_user_policy_attachment" "xj-infra-terraform-AmazonEKS_CNI_Policy" {
  user       = aws_iam_user.xj-ci.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_user
resource "aws_iam_user" "xj-ci" {
  name = "xj-ci"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_access_key
resource "aws_iam_access_key" "xj-ci" {
  user = aws_iam_user.xj-ci.name
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_user_policy
resource "aws_iam_user_policy" "xj-ci" {
  name = "xj-ci"
  user = aws_iam_user.xj-ci.name

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Sid    = "PublishStaticSites",
        Effect = "Allow",
        Action = [
          "s3:Get*",
          "s3:List*",
          "s3:Put*"
        ],
        Resource = [
          # Production
          aws_s3_bucket.xj-io.arn,
          "${aws_s3_bucket.xj-io.arn}/*",
          aws_s3_bucket.xj-play.arn,
          "${aws_s3_bucket.xj-play.arn}/*",
          aws_s3_bucket.xj-prod-stream.arn,
          "${aws_s3_bucket.xj-prod-stream.arn}/*",
          aws_s3_bucket.xj-help.arn,
          "${aws_s3_bucket.xj-help.arn}/*",
          aws_s3_bucket.xj-status.arn,
          "${aws_s3_bucket.xj-status.arn}/*",
          aws_s3_bucket.xj-lab.arn,
          "${aws_s3_bucket.xj-lab.arn}/*",
          # Development
          aws_s3_bucket.xj-dev.arn,
          "${aws_s3_bucket.xj-dev.arn}/*",
          aws_s3_bucket.xj-dev-lab.arn,
          "${aws_s3_bucket.xj-dev-lab.arn}/*",
          aws_s3_bucket.xj-dev-help.arn,
          "${aws_s3_bucket.xj-dev-help.arn}/*",
          aws_s3_bucket.xj-dev-status.arn,
          "${aws_s3_bucket.xj-dev-status.arn}/*",
          aws_s3_bucket.xj-dev-stream.arn,
          "${aws_s3_bucket.xj-dev-stream.arn}/*",
        ]
      },
      {
        Sid    = "InvalidateCDN",
        Effect = "Allow",
        Action = [
          "cloudfront:CreateInvalidation",
        ],
        Resource = [
          "*",
        ]
      },
      {
        Sid    = "ContainerRegistry",
        Effect = "Allow",
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:CompleteLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:InitiateLayerUpload",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage"
        ],
        Resource = [
          "*"
        ],
      },
      {
        Sid    = "EKS",
        Effect = "Allow",
        Action = [
          "eks:*",
          "sts:GetCallerIdentity",
        ],
        Resource = [
          "*"
        ],
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_user_policy
resource "aws_iam_user_policy" "xj-ci-extended" {
  name = "xj-ci"
  user = aws_iam_user.xj-ci.name

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Sid    = "PublishStaticSites",
        Effect = "Allow",
        Action = [
          "s3:Get*",
          "s3:List*",
          "s3:Put*"
        ],
        Resource = [
          # Production
          aws_s3_bucket.uxrg-prod.arn,
          "${aws_s3_bucket.uxrg-prod.arn}/*",
          # Development
          aws_s3_bucket.uxrg-dev.arn,
          "${aws_s3_bucket.uxrg-dev.arn}/*",
        ]
      },
      {
        Sid    = "InvalidateCDN",
        Effect = "Allow",
        Action = [
          "cloudfront:CreateInvalidation",
        ],
        Resource = [
          "*",
        ]
      },
      {
        Sid    = "ContainerRegistry",
        Effect = "Allow",
        Action = [
          "ecr:GetAuthorizationToken",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:CompleteLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:InitiateLayerUpload",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage"
        ],
        Resource = [
          "*"
        ],
      },
      {
        Sid    = "EKS",
        Effect = "Allow",
        Action = [
          "eks:*",
          "sts:GetCallerIdentity",
        ],
        Resource = [
          "*"
        ],
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role
resource "aws_iam_role" "xj-eks" {
  name = "xj-eks"

  assume_role_policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Effect = "Allow",
        Principal = {
          "Service" : "eks.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      },
      {
        Effect = "Allow",
        Principal = {
          "Service" : "ec2.amazonaws.com"
        },
        Action = "sts:AssumeRole"
      },
      {
        Effect = "Allow",
        Principal = {
          "Federated" : "arn:aws:iam::${local.aws-account-id}}:oidc-provider/oidc.eks.${local.aws-region}.amazonaws.com/id/xj-prod-6VxY2MG3"
        },
        Action = "sts:AssumeRoleWithWebIdentity",
        Condition = {
          "StringEquals" : {
            "oidc.eks.${local.aws-region}.amazonaws.com/id/xj-prod-6VxY2MG3:sub" : "system:serviceaccount:kube-system:efs-csi-controller-sa"
          }
        }
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "xj-eks-AmazonEKSClusterPolicy" {
  role       = aws_iam_role.xj-eks.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSClusterPolicy"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "xj-eks-AmazonEKSWorkerNodePolicy" {
  role       = aws_iam_role.xj-eks.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKSWorkerNodePolicy"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "xj-eks-AmazonEC2ContainerRegistryReadOnly" {
  role       = aws_iam_role.xj-eks.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy_attachment" "xj-eks-AmazonEKS_CNI_Policy" {
  role       = aws_iam_role.xj-eks.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEKS_CNI_Policy"
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy" "xj-eks-AmazonEKS_EFS_CSI_Driver_Policy" {
  role = aws_iam_role.xj-eks.name
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Effect = "Allow",
        Action = [
          "elasticfilesystem:DescribeAccessPoints",
          "elasticfilesystem:DescribeFileSystems"
        ],
        Resource = "*"
      },
      {
        Effect = "Allow",
        Action = [
          "elasticfilesystem:CreateAccessPoint"
        ],
        Resource = "*",
        Condition = {
          "StringLike" : {
            "aws:RequestTag/efs.csi.aws.com/cluster" : "true"
          }
        }
      },
      {
        Effect   = "Allow",
        Action   = "elasticfilesystem:DeleteAccessPoint",
        Resource = "*",
        Condition = {
          "StringEquals" : {
            "aws:ResourceTag/efs.csi.aws.com/cluster" : "true"
          }
        }
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_user
resource "aws_iam_user" "xj-prod" {
  name = "xj-prod"

  tags = {
    Environment = "prod"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_access_key
resource "aws_iam_access_key" "xj-prod" {
  user = aws_iam_user.xj-prod.name
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_user_policy
resource "aws_iam_user_policy" "xj-prod" {
  name = "xj-prod"
  user = aws_iam_user.xj-prod.name

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Sid    = "Metrics",
        Effect = "Allow",
        Action = [
          "cloudwatch:PutMetricData",
        ],
        Resource = "*"
      },
      {
        Sid    = "Notifications",
        Effect = "Allow",
        Action = [
          "sns:Publish",
        ],
        Resource = [
          "arn:aws:sns:${local.aws-region}:${local.aws-account-id}:xj-prod-chain-fabrication",
        ]
      },
      {
        Sid    = "S3",
        Effect = "Allow",
        Action = [
          "s3:DeleteObject",
          "s3:Get*",
          "s3:List*",
          "s3:Put*"
        ],
        Resource = [
          "arn:aws:s3:::xj-prod-audio",
          "arn:aws:s3:::xj-prod-ship",
          "arn:aws:s3:::xj-prod-audio",
          "arn:aws:s3:::xj-prod-ship",
        ]
      }
    ]
  })
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_user
resource "aws_iam_user" "xj-stage" {
  name = "xj-stage"

  tags = {
    Environment = "stage"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_access_key
resource "aws_iam_access_key" "xj-stage" {
  user = aws_iam_user.xj-stage.name
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_user
resource "aws_iam_user" "xj-dev" {
  name = "xj-dev"

  tags = {
    Environment = "dev"
  }
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_access_key
resource "aws_iam_access_key" "xj-dev" {
  user = aws_iam_user.xj-dev.name
}

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_user_policy
resource "aws_iam_user_policy" "xj-dev" {
  name = "xj-dev"
  user = aws_iam_user.xj-dev.name

  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        Sid    = "VisualEditor0",
        Effect = "Allow",
        Action = [
          "cloudwatch:PutMetricData",
          "ecr:GetAuthorizationToken"
        ],
        Resource = "*"
      },
      {
        Sid    = "VisualEditor1",
        Effect = "Allow",
        Action = [
          "sns:Publish",
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:CompleteLayerUpload",
          "ecr:UploadLayerPart",
          "s3:DeleteObject",
          "ecr:InitiateLayerUpload",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage"
        ],
        Resource = [
          "arn:aws:sns:${local.aws-region}:${local.aws-account-id}:xj-dev-chain-fabrication",
          "arn:aws:ecr:${local.aws-region}:${local.aws-account-id}:repository/*",
          "arn:aws:s3:::xj-dev-audio",
          "arn:aws:s3:::xj-dev-ship"
        ]
      },
      {
        Sid    = "VisualEditor2",
        Effect = "Allow",
        Action = [
          "s3:Get*",
          "s3:List*",
          "s3:Put*"
        ],
        Resource = [
          "arn:aws:s3:::xj-dev-audio",
          "arn:aws:s3:::xj-dev-ship"
        ]
      }
    ]
  })
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

# https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/iam_role_policy_attachment
resource "aws_iam_role_policy" "xj-eks-SecretsManager" {
  role = "xj-prod-6VxY2MG320210617212643701200000001"
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
