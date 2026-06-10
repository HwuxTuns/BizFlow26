# Security Group for MSK Kafka
resource "aws_security_group" "kafka" {
  name        = "${var.project_name}-kafka-sg"
  description = "Security group for MSK Serverless Kafka"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "Allow Kafka SASL IAM access from ECS tasks"
    from_port       = 9098 # Serverless IAM standard port
    to_port         = 9098
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
  }

  ingress {
    description     = "Allow Kafka plaintext access from ECS tasks"
    from_port       = 9092 # Standard port if enabled
    to_port         = 9092
    protocol        = "tcp"
    security_groups = [aws_security_group.ecs.id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-kafka-sg"
  }
}

# AWS MSK Serverless Cluster (Saves cost compared to provisioned brokers for testing)
resource "aws_msk_serverless_cluster" "kafka" {
  cluster_name = "${var.project_name}-kafka"

  vpc_config {
    subnet_ids         = aws_subnet.database[*].id
    security_groups    = [aws_security_group.kafka.id]
  }

  client_authentication {
    sasl {
      iam {
        enabled = true
      }
    }
  }

  tags = {
    Name = "${var.project_name}-kafka-serverless"
  }
}
