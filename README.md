# Retail Banking Application Platform | DevOps CI/CD on AWS

A hands-on DevOps project demonstrating how to package a Spring Boot banking application, build and publish a Docker image, and deploy it to Amazon EKS using Jenkins, Amazon ECR, Kubernetes, and Helm.

> **Project focus:** repeatable application delivery, containerized deployment, infrastructure automation, and operational visibility.

## Project Overview

This repository contains a demo retail banking application and the DevOps workflow used to build and deploy it. The application exposes simple HTTP endpoints for a home/status response, a sample account response, and health checking. It uses sample data only; it is **not** a production banking system and does not process real customer information or transactions.

### Key capabilities

- Build and package the Java application with Maven.
- Build a Docker image and run the container locally.
- Publish the versioned image to Amazon Elastic Container Registry (ECR).
- Run a Jenkins pipeline that checks out source code, builds the application, builds the image, authenticates to ECR, pushes the image, and deploys to Amazon EKS with Helm.
- Deploy the application as Kubernetes pods and expose it through a Kubernetes Service.
- Use health checks and container/application logs to validate and troubleshoot deployments.

## Architecture

```text
Developer
   |
   v
GitHub Repository
   |
   v
Jenkins Pipeline
   |-- Checkout source
   |-- Maven build/package
   |-- Build Docker image
   |-- Authenticate to Amazon ECR
   |-- Push versioned image
   `-- Deploy/upgrade with Helm
                 |
                 v
          Amazon EKS Cluster
                 |
           Kubernetes Deployment
              /          \
           Pod 1          Pod 2
              \          /
             Kubernetes Service
                    |
                    v
             Spring Boot API

Supporting AWS components: IAM, VPC, public/private subnets, Amazon ECR,
Amazon EKS, EC2 worker nodes, Amazon S3, Amazon RDS (where configured),
and Amazon CloudWatch.
```

The Jenkins pipeline screenshot shows these stages completing successfully: **Declarative: Checkout SCM, Checkout, Build Application, Build Docker Image, Login to ECR, Push Image to ECR, Deploy to EKS with Helm, and Declarative: Post Actions.**

## Technology Stack

| Area | Tools / Services |
|---|---|
| Application | Java, Spring Boot |
| Build | Maven |
| Source control | Git, GitHub |
| CI/CD | Jenkins, Jenkinsfile |
| Containerization | Docker |
| Container registry | Amazon ECR |
| Orchestration | Kubernetes, Amazon EKS |
| Deployment packaging | Helm |
| Infrastructure as Code | Terraform |
| Configuration automation | Ansible (where configured) |
| Cloud | AWS IAM, VPC, EC2/EKS nodes, ECR, S3, RDS and CloudWatch (as configured) |
| Monitoring / troubleshooting | CloudWatch, application logs, Kubernetes events and health probes |

## CI/CD Pipeline

1. **Checkout SCM** — Jenkins retrieves the source from GitHub.
2. **Build Application** — Maven compiles and packages the Spring Boot application into a JAR.
3. **Build Docker Image** — Docker packages the JAR and runtime into an image.
4. **Login to ECR** — Jenkins authenticates to Amazon ECR using AWS credentials configured outside source control.
5. **Push Image to ECR** — the built image is tagged and pushed to the registry.
6. **Deploy to EKS with Helm** — Helm installs or upgrades the release using the image tag produced by the pipeline.
7. **Post Actions** — Jenkins completes the pipeline's post-build actions.

The image tag should be unique per build (for example, the Jenkins build number) so a deployment can be traced back to the pipeline run.

## Application Endpoints

With the application running locally on port `8080`:

| Endpoint | Purpose |
|---|---|
| `/` | Returns a basic application-running message |
| `/api/account` | Returns demo account information |
| `/health` | Simple application health response |
| `/actuator/health` | Spring Boot Actuator health endpoint, if enabled in configuration |

Example local checks:

```bash
curl http://localhost:8080/
curl http://localhost:8080/api/account
curl http://localhost:8080/health
```

Example response from `/health`:

```text
UP
```

The `/api/account` endpoint uses hard-coded demo data. Do not add real account numbers, credentials, or customer information.

## Run Locally

Prerequisites: a compatible JDK, Maven, and Git.

```bash
git clone <YOUR_GITHUB_REPOSITORY_URL>
cd retail-banking-devops/application
mvn clean package
java -jar target/retail-banking-app-1.0.0.jar
```

In another terminal:

```bash
curl http://localhost:8080/health
```

## Build and Run with Docker

Run these commands from the `application/` directory after building the JAR:

```bash
docker build -t retail-banking-app:1.0 .
docker run --rm --name retail-banking-container -p 8080:8080 retail-banking-app:1.0
```

In another terminal, validate the endpoint:

```bash
curl http://localhost:8080/health
```

If port `8080` is already in use, stop the process/container using it or map another host port, for example `-p 8082:8080`, then test `http://localhost:8082/health`.

## Kubernetes / Helm Operations

After configuring `kubectl` for the target EKS cluster and ensuring the image exists in ECR:

```bash
kubectl get nodes
helm lint helm/banking-app
helm upgrade --install banking-app helm/banking-app
kubectl get deployments
kubectl get pods
kubectl get services
```

Useful troubleshooting commands:

```bash
kubectl describe pod <pod-name>
kubectl logs <pod-name>
kubectl get events --sort-by=.metadata.creationTimestamp
kubectl rollout status deployment/<deployment-name>
helm history banking-app
helm rollback banking-app <revision>
```

Make sure the Helm values point to the correct ECR repository and image tag before deploying.

## Infrastructure and Security Notes

- Terraform is used for infrastructure provisioning where the Terraform configuration is included and applied.
- Keep AWS access keys, Jenkins secrets, kubeconfig files, Terraform state files, and environment files out of Git.
- Prefer an IAM role for Jenkins (or short-lived credentials) with least-privilege permissions for ECR and EKS.
- Restrict Kubernetes and AWS access to authorized identities.
- Use private subnets for worker nodes where appropriate and control ingress through security groups and Kubernetes/AWS networking.
- This is a learning/demo project. The sample endpoint and static balance are not suitable for real banking use.
- Review AWS costs before creating resources. EKS worker nodes, load balancers, NAT gateways, RDS, and storage can incur charges.

## Screenshots

### Jenkins pipeline — successful run

<img width="796" height="221" alt="jenkins-pipeline-stages" src="https://github.com/user-attachments/assets/befadc1b-9964-4da9-a2b5-1fe3e008ca9b" />


### Jenkins job — all stages green

<img width="1348" height="649" alt="jenkins-successful-build" src="https://github.com/user-attachments/assets/584756ac-d5cc-45bf-a208-13c871c9d6cb" />


### Spring Boot application running in Docker

<img width="1354" height="630" alt="spring-boot-container-logs" src="https://github.com/user-attachments/assets/9a192029-512f-4454-a4b5-8941c5590dd8" />


### Docker image and ECR tag

<img width="1351" height="123" alt="docker-images" src="https://github.com/user-attachments/assets/aefb880b-a76a-4e76-92e9-9f1d3901c135" />


### AWS architecture overview

<img width="374" height="413" alt="aws-architecture" src="https://github.com/user-attachments/assets/ab73e93e-2e68-4462-96b5-9d19620e193f" />


## Repository Structure

```text
retail-banking-devops/
├── application/       # Spring Boot source, Maven POM and Dockerfile
├── terraform/         # Terraform infrastructure configuration
├── ansible/           # Ansible inventory and playbooks (if configured)
├── k8s/               # Kubernetes manifests
├── helm/
│   └── banking-app/   # Helm chart and templates
├── Jenkinsfile        # Jenkins CI/CD pipeline
└── README.md
```

## Resume Project Description

**Retail Banking Application Platform — DevOps Engineer**

Built and practiced an end-to-end CI/CD workflow for a Spring Boot application using Jenkins, Maven, Docker, Amazon ECR, Amazon EKS, Kubernetes, and Helm. Automated application build and container-image publishing, and deployed application releases to EKS through a Jenkins pipeline. Used Terraform for infrastructure as code and practiced application health validation and troubleshooting with container logs, Kubernetes resources, and cloud monitoring tools.

> **Resume accuracy:** Keep only the technologies and responsibilities you can explain and demonstrate in an interview. Do not describe RDS, Ansible, Terraform-managed resources, or monitoring dashboards as implemented unless you have actually configured and tested them in this repository.

## Future Improvements

- Add automated unit/integration tests and publish test results from Jenkins.
- Add SonarQube quality gates and artifact management where configured.
- Add image vulnerability scanning and dependency checks.
- Add deployment approvals, rollback handling, and notifications.
- Add dashboards and alerts for application and infrastructure metrics.

---

**Author:** Vaibhav Surase  
**Project:** Retail Banking Application Platform  
