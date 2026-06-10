variable "aws_region" {
  type        = string
  description = "AWS Region to deploy resource"
  default     = "ap-southeast-1" # Singapore is closest to Vietnam
}

variable "project_name" {
  type        = string
  description = "Name of the project"
  default     = "bizflow"
}

variable "environment" {
  type        = string
  description = "Deployment environment"
  default     = "production"
}

variable "vpc_cidr" {
  type        = string
  description = "CIDR block for the VPC"
  default     = "10.0.0.0/16"
}

variable "db_password" {
  type        = string
  description = "MySQL database root password"
  default     = "bizflowprod123"
  sensitive   = true
}

variable "jwt_secret" {
  type        = string
  description = "JWT Secret for user authentication (Min 32 characters)"
  default     = "super-secure-bizflow-jwt-secret-key-32-characters"
  sensitive   = true
}
