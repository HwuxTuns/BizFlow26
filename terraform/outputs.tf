output "alb_dns_name" {
  description = "The public DNS name of the Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "rds_endpoint" {
  description = "The connection endpoint for RDS MySQL"
  value       = aws_db_instance.mysql.endpoint
}

output "elasticache_primary_endpoint" {
  description = "The connection endpoint for Redis cache"
  value       = aws_elasticache_replication_group.redis.primary_endpoint_address
}

output "rabbitmq_endpoint" {
  description = "The connection endpoint for Amazon MQ RabbitMQ"
  value       = aws_mq_broker.rabbitmq.instances[0].endpoints[0]
}

output "s3_bucket_name" {
  description = "The name of the S3 Bucket for static files"
  value       = aws_s3_bucket.frontend_static.id
}
