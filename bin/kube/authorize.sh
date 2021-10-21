#!/usr/bin/env bash

# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

aws eks --region $(terraform output -raw region) update-kubeconfig --name $(terraform output -raw xj_prod_cluster_name)
