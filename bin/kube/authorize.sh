#!/usr/bin/env bash

aws eks --region $(terraform output -raw region) update-kubeconfig --name $(terraform output -raw xj_prod_cluster_name)
