# Amazon MQ RabbitMQ Broker Configuration
resource "aws_mq_broker" "rabbitmq" {
  broker_name        = "${var.project_name}-rabbitmq"
  engine_type        = "RabbitMQ"
  engine_version     = "3.11.28" # Select an active engine version
  host_instance_type = "mq.t3.micro" # Smallest size for cost efficiency
  deployment_mode    = "SINGLE_INSTANCE"

  user {
    username = "bizflow"
    password = var.db_password # Re-use secure password variable
  }

  subnet_ids         = [aws_subnet.database[0].id]
  security_groups    = [aws_security_group.mq.id]
  publicly_accessible = false

  tags = {
    Name = "${var.project_name}-rabbitmq"
  }
}
